package com.icx.prueba.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Molde para leer lo que responde Oracle en /queryResults.
// Oracle devuelve algo así:
// { "items": [ { "columnNames": ["id","first",...], "rows": [["205","Paula",...], ...] } ] }
// Oracle manda más campos (tableName, count, links) que no uso,
// por eso ignoreUnknown = true, para que no falle al leerlos.
@JsonIgnoreProperties(ignoreUnknown = true)
public record QueryResultsResponse(List<Item> items) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(List<String> columnNames, List<List<Object>> rows) {
    }
}
