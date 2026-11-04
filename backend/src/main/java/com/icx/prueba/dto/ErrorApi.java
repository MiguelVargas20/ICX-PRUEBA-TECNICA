package com.icx.prueba.dto;

/* Forma única de TODOS los errores que el backend le devuelve al frontend. */
public record ErrorApi(
        int estado,
        String mensaje,
        String codigoOracle) {
}
