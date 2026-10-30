package com.icx.prueba.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/* Datos que llegan del formulario para ACTUALIZAR un contacto. */
public record ContactoUpdate(

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 80, message = "El nombre no puede pasar de 80 caracteres.")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio.")
        @Size(max = 80, message = "El apellido no puede pasar de 80 caracteres.")
        String apellido,

        @Size(max = 80, message = "La ciudad no puede pasar de 80 caracteres.")
        String ciudad) {
}
