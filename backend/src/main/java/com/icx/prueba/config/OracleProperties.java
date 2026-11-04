package com.icx.prueba.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oracle")

/* Propiedades para la configuración del cliente de Oracle. */
public record OracleProperties(
        String baseUrl,
        String user,
        String password,
        String apiVersion,
        String contextHeader) {
}
