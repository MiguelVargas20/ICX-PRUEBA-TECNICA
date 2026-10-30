package com.icx.prueba.exception;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;


/* Manejador de excepciones para la API de contactos. */
@RestControllerAdvice
public class ApiExceptionHandler {

    /* Logger para registrar los eventos de la aplicación. */
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /* Manejador para excepciones de acceso a recursos. */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, String>> sinConexion(ResourceAccessException e) {
        log.warn("Sin conexión con Oracle: {}", e.getMessage());
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, "No hay conexión con Oracle Service Cloud.");
    }

    /* Manejador para excepciones de la API de Oracle. */
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Map<String, String>> errorDeOracle(RestClientResponseException e) {
        int codigo = e.getStatusCode().value();
        log.warn("Oracle respondió {}: {}", codigo, e.getResponseBodyAsString());

        /* Manejo de los diferentes códigos de estado de Oracle */
        if (codigo == 404) {
            return respuesta(HttpStatus.NOT_FOUND, "El contacto no existe.");
        }

        /* Manejo de errores de solicitud incorrecta */
        if (codigo == 400) {
            return respuesta(HttpStatus.BAD_REQUEST, "Oracle rechazó los datos enviados.");
        }

        /* Manejo de errores de autenticación */
        if (codigo == 401 || codigo == 403) {
            return respuesta(HttpStatus.BAD_GATEWAY, "El servidor no pudo autenticarse con Oracle.");
        }

        /* Manejo de otros errores de Oracle */
        return respuesta(HttpStatus.BAD_GATEWAY, "Oracle presentó un error.");
    }

    /* Método para crear una respuesta con un estado y un mensaje. */
    private ResponseEntity<Map<String, String>> respuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("mensaje", mensaje));
    }
}
