package com.icx.prueba.dto;

import java.time.Instant;
import java.util.List;

/*
 * Lo que devuelve GET /api/contactos.
 * Además de la lista, le dice al frontend DE DÓNDE salieron los datos:
 *  - desdeRespaldo = false → vienen de Oracle (datos en vivo).
 *  - desdeRespaldo = true  → Oracle no respondió y se muestra la última consulta guardada en H2.
 */
public record ResultadoBusqueda(
        List<ContactoDto> contactos,
        boolean desdeRespaldo,
        Instant fechaRespaldo,
        String filtrosRespaldo) {

    /*
     * Fábrica para una respuesta normal. Los otros tres campos quedan null
     * porque no se está mostrando una copia de la base de respaldo.
     */
    public static ResultadoBusqueda desdeOracle(List<ContactoDto> contactos) {
        return new ResultadoBusqueda(contactos, false, null, null);
    }

    /*
     * Fábrica para modo de contingencia. Include fecha y filtros para que el
     * frontend explique claramente qué consulta se pudo recuperar.
     */
    public static ResultadoBusqueda desdeRespaldo(List<ContactoDto> contactos, Instant fecha, String filtros) {
        return new ResultadoBusqueda(contactos, true, fecha, filtros);
    }
}
