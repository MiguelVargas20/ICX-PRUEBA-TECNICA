package com.icx.prueba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/* @ConfigurationPropertiesScan registra OracleProperties como bean (lee las propiedades "oracle.*"). */
@SpringBootApplication
@ConfigurationPropertiesScan
public class PruebaApplication {

	/* Punto de entrada: al ejecutar la clase, Spring arranca y levanta el servidor web. */
	public static void main(String[] args) {
		/*
		 * Spring busca componentes, crea los servicios, controladores, clientes
		 * y repositorios, lee application.properties y levanta el servidor.
		 */
		SpringApplication.run(PruebaApplication.class, args);
	}

}
