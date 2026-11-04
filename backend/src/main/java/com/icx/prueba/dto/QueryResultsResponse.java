package com.icx.prueba.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/* DTO para representar la respuesta de los resultados de la consulta. */
/*
 * Un DTO también sirve como contrato de lectura: le dice a Jackson qué
 * campos esperar en el JSON de Oracle y cómo ubicarlos dentro de la aplicación.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record QueryResultsResponse(List<Item> items) {

    /* DTO para representar un elemento de la respuesta de la consulta. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(List<String> columnNames, List<List<Object>> rows) {
    }
}
