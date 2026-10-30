package com.icx.prueba.config;

import java.net.http.HttpClient;
import java.time.Duration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/* Configuración del cliente REST para conectarse a la API de Oracle. */
@Configuration

public class RestClientConfig {

    /* Bean para crear el cliente REST para conectarse a la API de Oracle. */
    @Bean
    RestClient oracleRestClient(OracleProperties props) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(10));

        /* Configuración del cliente REST para conectarse a la API de Oracle. */
        return RestClient.builder()
                .baseUrl(props.baseUrl())
                .requestFactory(factory)
                .defaultHeaders(h -> {
                    h.setBasicAuth(props.user(), props.password());
                    h.set("OSvC-CREST-Application-Context", props.contextHeader());
                })
                .build();
    }
}
