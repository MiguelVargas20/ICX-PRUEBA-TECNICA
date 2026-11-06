package com.icx.prueba.dto;

// Así responde el backend cuando hay un error, siempre con esta misma forma.
// estado: el código HTTP (400, 404, 503...)
// mensaje: el texto que se le muestra al usuario
// codigoOracle: el código de error de Oracle (OSC-CREST-...) si lo hay, si no null
public record ErrorApi(
        int estado,
        String mensaje,
        String codigoOracle) {
}
