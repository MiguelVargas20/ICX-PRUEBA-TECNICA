package com.icx.prueba.client;

import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.icx.prueba.config.OracleProperties;
import com.icx.prueba.dto.QueryResultsResponse;

// Esta clase solo hace las llamadas HTTP a la API de Oracle.
// No decide nada: eso lo hace ContactoService.
// Si Oracle responde con error, el RestClient lanza una excepción y
// la atrapa el service o el ApiExceptionHandler.
@Component
public class OracleClient {

    // Ruta base de contactos. {version} se cambia por v1.4
    private static final String CONTACTOS = "/services/rest/connect/{version}/contacts";

    private final RestClient restClient;
    private final OracleProperties props;

    // Spring me pasa el RestClient que armé en RestClientConfig
    public OracleClient(RestClient oracleRestClient, OracleProperties props) {
        this.restClient = oracleRestClient;
        this.props = props;
    }

    // Manda una consulta ROQL a Oracle (el "SQL" de Oracle Service Cloud)
    public QueryResultsResponse consultar(String roql) {
        // Los {} se llenan en orden: {version} = v1.4 y {q} = la consulta.
        // Spring se encarga de codificar los espacios y comillas en la URL.
        return restClient.get()
                .uri("/services/rest/connect/{version}/queryResults?query={q}",
                        props.apiVersion(), roql)
                .retrieve()
                .body(QueryResultsResponse.class);
    }

    // POST: crea el contacto. Oracle devuelve el contacto creado con su id.
    public Map<String, Object> crear(Map<String, Object> cuerpo) {
        // body() convierte el Map en JSON antes de enviarlo
        return restClient.post()
                .uri(CONTACTOS, props.apiVersion())
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() { });
    }

    // PATCH: cambia solo los campos que se envían. Oracle no devuelve cuerpo.
    public void actualizar(long id, Map<String, Object> cuerpo) {
        restClient.patch()
                .uri(CONTACTOS + "/{id}", props.apiVersion(), id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(cuerpo)
                .retrieve()
                .toBodilessEntity(); // no espero JSON en la respuesta
    }

    // DELETE: borra el contacto
    public void eliminar(long id) {
        restClient.delete()
                .uri(CONTACTOS + "/{id}", props.apiVersion(), id)
                .retrieve()
                .toBodilessEntity();
    }
}
