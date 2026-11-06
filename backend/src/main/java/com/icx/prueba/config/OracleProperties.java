package com.icx.prueba.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Aquí quedan guardados los datos de conexión con Oracle.
// Spring llena este record con las propiedades que empiezan por "oracle."
// en application.properties (por ejemplo oracle.base-url pasa a baseUrl).
// Así no tengo la URL ni la clave regadas por todo el código.
@ConfigurationProperties(prefix = "oracle")
public record OracleProperties(
        String baseUrl,       // URL del sitio de Oracle (viene del .env)
        String user,          // usuario para la autenticación básica
        String password,      // clave (viene del .env, nunca va en el código)
        String apiVersion,    // versión de la API REST, en este caso v1.4
        String contextHeader) // valor del encabezado OSvC-CREST-Application-Context
{
}
