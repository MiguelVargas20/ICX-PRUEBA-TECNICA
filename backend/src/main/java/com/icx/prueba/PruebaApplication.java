package com.icx.prueba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/* @ConfigurationPropertiesScan registra OracleProperties como bean (lee las propiedades "oracle.*"). */
@SpringBootApplication
@ConfigurationPropertiesScan
public class PruebaApplication {

	public static void main(String[] args) {
		SpringApplication.run(PruebaApplication.class, args);
	}

}
