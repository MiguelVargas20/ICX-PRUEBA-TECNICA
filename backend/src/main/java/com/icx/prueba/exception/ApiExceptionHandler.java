package com.icx.prueba.exception;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.client.UnknownContentTypeException;

import com.icx.prueba.dto.ErrorApi;

// Aquí se manejan todos los errores del backend.
// @RestControllerAdvice hace que esta clase atrape las excepciones de cualquier controller.
// Cada método con @ExceptionHandler atiende un tipo de error y responde con ErrorApi,
// así el frontend siempre lee el mensaje en el mismo campo.
// Regla: el detalle técnico va al log y al usuario solo le llega un mensaje claro.
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    // Los códigos de error de Oracle son así: OSC-CREST-00013
    private static final Pattern CODIGO_ORACLE = Pattern.compile("OSC-CREST-\\d+");

    // 1. No hay conexión con Oracle (caído, sin internet o pasó el timeout)
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorApi> sinConexion(ResourceAccessException e) {
        log.warn("Sin conexión con Oracle: {}", e.getMessage());
        // 503 = el servicio del que dependo no está disponible
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE,
                "No hay conexión con Oracle Service Cloud. Intenta de nuevo en un momento.", null);
    }

    // Oracle respondió algo que no es JSON. Me pasó con el sitio de pruebas deshabilitado:
    // devolvía una página HTML en vez de la respuesta de la API.
    @ExceptionHandler(UnknownContentTypeException.class)
    public ResponseEntity<ErrorApi> respuestaNoJson(UnknownContentTypeException e) {
        log.warn("Oracle respondió un contenido que no es JSON: {}", e.getContentType());
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE,
                "Oracle Service Cloud no está disponible en este momento. Intenta de nuevo más tarde.", null);
    }


    // 2. Oracle sí respondió pero con un código de error (400, 401, 404, 500...)
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ErrorApi> errorDeOracle(RestClientResponseException e) {
        int codigo = e.getStatusCode().value();
        String codigoOracle = extraerCodigo(e.getResponseBodyAsString());

        // La respuesta completa de Oracle solo va al log, para poder revisar qué pasó
        log.warn("Oracle respondió {} ({}): {}", codigo, codigoOracle, e.getResponseBodyAsString());

        // Uso 502 cuando el problema es de Oracle o de la configuración hacia Oracle, no del usuario
        if (codigo == 404) {
            // Los 404 de "contacto no existe" ya los cambia el service a ContactoNoEncontradoException.
            // Si llega uno aquí es porque la URL o la versión de la API están mal.
            return respuesta(HttpStatus.BAD_GATEWAY,
                    "El servidor está mal configurado para conectarse con Oracle.", codigoOracle);
        }

        if (codigo == 400 && "OSC-CREST-00025".equals(codigoOracle)) {
            // Este código sale cuando falta el encabezado de contexto: es error de configuración
            return respuesta(HttpStatus.BAD_GATEWAY,
                    "El servidor está mal configurado para conectarse con Oracle.", codigoOracle);
        }
        if (codigo == 400) {
            return respuesta(HttpStatus.BAD_REQUEST, "Oracle rechazó los datos enviados.", codigoOracle);
        }
        if (codigo == 401 || codigo == 403) {
            return respuesta(HttpStatus.BAD_GATEWAY,
                    "El servidor no pudo autenticarse con Oracle.", codigoOracle);
        }
        return respuesta(HttpStatus.BAD_GATEWAY, "Oracle presentó un error.", codigoOracle);
    }

    // Se quiso editar o eliminar un contacto que no existe
    @ExceptionHandler(ContactoNoEncontradoException.class)
    public ResponseEntity<ErrorApi> contactoNoEncontrado(ContactoNoEncontradoException e) {
        log.info(e.getMessage());
        return respuesta(HttpStatus.NOT_FOUND,
                "El contacto no existe o ya fue eliminado. Actualiza la lista.", null);
    }

    // 3. Falló alguna validación de @Valid (nombre vacío, correo mal escrito...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorApi> datosInvalidos(MethodArgumentNotValidException e) {
        // Junto los mensajes de todos los campos que fallaron en un solo texto
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(" "));
        return respuesta(HttpStatus.BAD_REQUEST, mensaje, null);
    }

    // 4. El cuerpo de la petición no es un JSON válido
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorApi> jsonInvalido(HttpMessageNotReadableException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "Los datos enviados no tienen un formato válido.", null);
    }

    // 5. El id de la URL no es un número (ej: /api/contactos/abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorApi> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "El valor de '" + e.getName() + "' no es válido.", null);
    }

    // 6. Cualquier otro error que no tenga su propio método cae aquí
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApi> inesperado(Exception e) {
        // Algunos errores ya traen su código de Spring (ruta que no existe = 404,
        // método no permitido = 405). En ese caso respeto ese código.
        if (e instanceof ErrorResponse errorSpring) {
            HttpStatusCode estado = errorSpring.getStatusCode();
            return ResponseEntity.status(estado)
                    .body(new ErrorApi(estado.value(), "No se pudo atender la solicitud.", null));
        }
        // Si no, es un error que no tenía previsto: guardo todo el detalle en el log
        log.error("Error inesperado", e);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado en el servidor.", null);
    }

    // Busca el código OSC-CREST-xxxxx dentro de la respuesta de Oracle. Si no hay, devuelve null.
    private String extraerCodigo(String cuerpo) {
        if (cuerpo == null) {
            return null;
        }
        Matcher m = CODIGO_ORACLE.matcher(cuerpo);
        return m.find() ? m.group() : null;
    }

    // Para no repetir en cada método cómo se arma la respuesta de error
    private ResponseEntity<ErrorApi> respuesta(HttpStatus estado, String mensaje, String codigoOracle) {
        return ResponseEntity.status(estado).body(new ErrorApi(estado.value(), mensaje, codigoOracle));
    }
}
