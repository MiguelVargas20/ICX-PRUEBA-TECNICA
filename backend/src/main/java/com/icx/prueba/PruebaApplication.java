package com.icx.prueba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

// Clase principal del backend. Desde aquí arranca todo.
// @ConfigurationPropertiesScan hace que Spring lea las propiedades "oracle.*"
// y las guarde en OracleProperties.
@SpringBootApplication
@ConfigurationPropertiesScan
public class PruebaApplication {

	public static void main(String[] args) {
		// Spring lee application.properties (y el .env), crea los controllers,
		// services y repositorios, y levanta el servidor en el puerto 8080.
		SpringApplication.run(PruebaApplication.class, args);
	}

}
