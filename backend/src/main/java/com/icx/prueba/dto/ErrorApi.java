package com.icx.prueba.dto;

/* Forma única de TODOS los errores que el backend le devuelve al frontend. */
/*
 * estado contiene el mismo código HTTP que retorna la respuesta.
 * mensaje explica el problema de forma entendible.
 * codigoOracle permite mostrar o registrar el identificador específico de Oracle.
 */
public record ErrorApi(
        int estado,
        String mensaje,
        String codigoOracle) {
}
