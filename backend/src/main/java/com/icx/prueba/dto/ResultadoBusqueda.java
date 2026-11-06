package com.icx.prueba.dto;

import java.time.Instant;
import java.util.List;

// Lo que responde GET /api/contactos.
// Además de la lista le dice al frontend de dónde salieron los datos:
//   desdeRespaldo = false -> vienen de Oracle
//   desdeRespaldo = true  -> Oracle no respondió y son los de la última consulta guardada en H2
// En ese caso también van la fecha y los filtros de esa consulta para mostrarlos en el aviso.
public record ResultadoBusqueda(
        List<ContactoDto> contactos,
        boolean desdeRespaldo,
        Instant fechaRespaldo,
        String filtrosRespaldo) {

    // Respuesta normal con datos de Oracle (lo del respaldo queda en null)
    public static ResultadoBusqueda desdeOracle(List<ContactoDto> contactos) {
        return new ResultadoBusqueda(contactos, false, null, null);
    }

    // Respuesta con los datos guardados en H2
    public static ResultadoBusqueda desdeRespaldo(List<ContactoDto> contactos, Instant fecha, String filtros) {
        return new ResultadoBusqueda(contactos, true, fecha, filtros);
    }
}
