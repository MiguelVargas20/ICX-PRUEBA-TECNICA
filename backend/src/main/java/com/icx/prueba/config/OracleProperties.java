package com.icx.prueba.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oracle")

/*
 * Record que representa las propiedades oracle.* de application.properties.
 * Spring crea uno y lo llena automáticamente durante el arranque.
 */
public record OracleProperties(
        /* Dirección base de la API REST de Oracle. */
        String baseUrl,
        /* Usuario y contraseña usados para autenticación básica. */
        String user,
        String password,
        /* Versión REST y encabezado que identifica el contexto de la prueba. */
        String apiVersion,
        String contextHeader) {
}
