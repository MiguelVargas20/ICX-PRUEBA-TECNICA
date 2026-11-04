package com.icx.prueba.client;

import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.icx.prueba.config.OracleProperties;
import com.icx.prueba.dto.QueryResultsResponse;

/**
 * Cliente para conectarse a la API de Oracle.
 * Aquí SOLO se hacen llamadas HTTP: no hay lógica de negocio.
 */
@Component
public class OracleClient {

    private static final String CONTACTOS = "/services/rest/connect/{version}/contacts";

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

    /** POST /contacts: crea el contacto. Oracle responde 201 con el contacto completo (incluye el id). */
    public Map<String, Object> crear(Map<String, Object> cuerpo) {
        return restClient.post()
                .uri(CONTACTOS, props.apiVersion())
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() { });
    }

    /** PATCH /contacts/{id}: cambia solo los campos enviados. Oracle responde 200 sin cuerpo. */
    public void actualizar(long id, Map<String, Object> cuerpo) {
        restClient.patch()
                .uri(CONTACTOS + "/{id}", props.apiVersion(), id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .toBodilessEntity();
    }

    /** DELETE /contacts/{id}: elimina el contacto. */
    public void eliminar(long id) {
        restClient.delete()
                .uri(CONTACTOS + "/{id}", props.apiVersion(), id)
                .retrieve()
                .toBodilessEntity();
    }
}
