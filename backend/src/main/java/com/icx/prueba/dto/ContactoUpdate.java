package com.icx.prueba.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

/* Datos que llegan del formulario para ACTUALIZAR un contacto. */
/*
 * Separar ContactoRequest y ContactoUpdate ayuda a expresar las diferencias
 * entre crear y editar sin mezclar reglas de ambos procesos.
 */
public record ContactoUpdate(

        /* Nombre y apellido siguen siendo obligatorios al editar. */
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 80, message = "El nombre no puede pasar de 80 caracteres.")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio.")
        @Size(max = 80, message = "El apellido no puede pasar de 80 caracteres.")
        String apellido,

        /* null significa que ciudad no llegó; texto vacío significa solicitar su borrado. */
        String ciudad,

        /* Mismas reglas que al crear (ContactoRequest). Vacío = no se cambia. */
        @Email(message = "El correo no tiene un formato válido.")
        String correo,

        @Pattern(regexp = "^$|^[0-9+()\\-\\s]{7,20}$",
                message = "El teléfono solo puede tener números, espacios, + - ( ) y entre 7 y 20 caracteres.")
        String telefono) {
}
