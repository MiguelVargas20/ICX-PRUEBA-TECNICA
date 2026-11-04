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
        /* El HttpClient controla la espera máxima para establecer la conexión. */
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        /* RestClient usa esta fábrica para traducir las peticiones a llamadas HTTP Java. */
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        /* Si Oracle tarda más de 10 segundos, la petición se cancela por timeout. */
        factory.setReadTimeout(Duration.ofSeconds(10));

        /*
         * baseUrl fija la raíz de Oracle y defaultHeaders agrega en cada petición
         * usuario, contraseña y el contexto requerido por la API.
         */
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
