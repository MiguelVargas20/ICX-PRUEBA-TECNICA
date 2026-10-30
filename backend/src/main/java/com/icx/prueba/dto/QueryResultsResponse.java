package com.icx.prueba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/* DTO para representar la respuesta de los resultados de la consulta. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record QueryResultsResponse(List<Item> items) {

    /* DTO para representar un elemento de la respuesta de la consulta. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(List<String> columnNames, List<List<Object>> rows) {
    }
}
