package com.icx.prueba.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

// Aquí se configura el RestClient, que es el objeto que hace las peticiones a Oracle.
// Lo dejo armado una sola vez para no repetir la URL, la clave y los encabezados
// en cada método de OracleClient.
@Configuration
public class RestClientConfig {

    @Bean
    RestClient oracleRestClient(OracleProperties props) {
        // Si en 5 segundos no logra conectarse con Oracle, se rinde
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        // Si Oracle se conecta pero tarda más de 10 segundos en responder, también se cancela
        factory.setReadTimeout(Duration.ofSeconds(10));

        return RestClient.builder()
                .baseUrl(props.baseUrl())
                // Ojo: sin el Buffering, el cuerpo se manda "por partes" (chunked) y Oracle
                // no lo lee, responde "No POST data found". Con esto se manda completo
                // y con su tamaño (Content-Length).
                .requestFactory(new BufferingClientHttpRequestFactory(factory))
                // Estos encabezados van en todas las peticiones:
                // usuario y clave (Basic Auth) y el contexto que pide Oracle
                .defaultHeaders(h -> {
                    h.setBasicAuth(props.user(), props.password());
                    h.set("OSvC-CREST-Application-Context", props.contextHeader());
                })
                .build();
    }
}
