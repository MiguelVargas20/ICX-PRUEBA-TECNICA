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

import com.icx.prueba.dto.ErrorApi;

/**
 * Atrapa los errores de TODA la aplicación y los convierte en una respuesta
 * con la misma forma: { estado, mensaje, codigoOracle }.
 * Así el frontend siempre sabe dónde leer el mensaje.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /* Los códigos de error de Oracle tienen esta forma: OSC-CREST-00013 */
    private static final Pattern CODIGO_ORACLE = Pattern.compile("OSC-CREST-\\d+");

    /* 1. Oracle no responde (caído, sin internet o tardó demasiado). */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorApi> sinConexion(ResourceAccessException e) {
        log.warn("Sin conexión con Oracle: {}", e.getMessage());
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE,
                "No hay conexión con Oracle Service Cloud. Intenta de nuevo en un momento.", null);
    }

    /* 2. Oracle respondió, pero con un error (400, 401, 404...). */
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ErrorApi> errorDeOracle(RestClientResponseException e) {
        int codigo = e.getStatusCode().value();
        String codigoOracle = extraerCodigo(e.getResponseBodyAsString());

        /* El detalle completo solo va al log, no se le muestra al usuario. */
        log.warn("Oracle respondió {} ({}): {}", codigo, codigoOracle, e.getResponseBodyAsString());

        if (codigo == 404) {
            return respuesta(HttpStatus.NOT_FOUND, "El contacto no existe.", codigoOracle);
        }
        if (codigo == 400 && "OSC-CREST-00025".equals(codigoOracle)) {
            /* Falta el encabezado de contexto: es un error nuestro, no del usuario. */
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

    /* 3. El formulario trae datos inválidos (falló alguna regla de @Valid). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorApi> datosInvalidos(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(" "));
        return respuesta(HttpStatus.BAD_REQUEST, mensaje, null);
    }

    /* 4. El cuerpo de la petición no es un JSON válido. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorApi> jsonInvalido(HttpMessageNotReadableException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "Los datos enviados no tienen un formato válido.", null);
    }

    /* 5. El id de la URL no es un número (ej. /api/contactos/abc). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorApi> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "El valor de '" + e.getName() + "' no es válido.", null);
    }

    /* 6. Red de seguridad: cualquier otro error que no previmos. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApi> inesperado(Exception e) {
        /* Spring ya sabe el estado de errores como "ruta no encontrada" (404) o "método no permitido" (405). */
        if (e instanceof ErrorResponse errorSpring) {
            HttpStatusCode estado = errorSpring.getStatusCode();
            return ResponseEntity.status(estado)
                    .body(new ErrorApi(estado.value(), "No se pudo atender la solicitud.", null));
        }
        log.error("Error inesperado", e);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado en el servidor.", null);
    }

    /* Busca el código OSC-CREST-xxxxx dentro de la respuesta de Oracle (o null si no está). */
    private String extraerCodigo(String cuerpo) {
        if (cuerpo == null) {
            return null;
        }
        Matcher m = CODIGO_ORACLE.matcher(cuerpo);
        return m.find() ? m.group() : null;
    }

    /* Arma la respuesta de error con la forma única. */
    private ResponseEntity<ErrorApi> respuesta(HttpStatus estado, String mensaje, String codigoOracle) {
        return ResponseEntity.status(estado).body(new ErrorApi(estado.value(), mensaje, codigoOracle));
    }
}
