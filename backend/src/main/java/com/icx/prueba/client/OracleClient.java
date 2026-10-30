package com.icx.prueba.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.icx.prueba.config.OracleProperties;
import com.icx.prueba.dto.QueryResultsResponse;


/**
 * Cliente para conectarse a la API de Oracle.
 */
@Component
public class OracleClient {

    /** Cliente REST para conectarse a la API de Oracle. */
    private final RestClient restClient;
    private final OracleProperties props;

    public OracleClient(RestClient oracleRestClient, OracleProperties props) {
        this.restClient = oracleRestClient;
        this.props = props;
    }

    /** Ejecuta una consulta ROQL y devuelve la respuesta tal como la entrega Oracle. */
    public QueryResultsResponse consultar(String roql) {
        return restClient.get()
                .uri("/services/rest/connect/{version}/queryResults?query={q}",
                        props.apiVersion(), roql)
                .retrieve()
                .body(QueryResultsResponse.class);
    }
}
