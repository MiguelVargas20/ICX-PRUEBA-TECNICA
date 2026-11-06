package com.icx.prueba.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Datos que llegan del formulario para CREAR un contacto.
// Las anotaciones son las reglas de validación. Se revisan cuando el
// controller tiene @Valid, y si alguna falla se devuelve el mensaje.
// Uso record porque Java crea solo el constructor y los getters (nombre(), correo()...).
public record ContactoRequest(

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 80, message = "El nombre no puede pasar de 80 caracteres.")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio.")
        @Size(max = 80, message = "El apellido no puede pasar de 80 caracteres.")
        String apellido,

        @Size(max = 80, message = "La ciudad no puede pasar de 80 caracteres.")
        String ciudad,

        // El correo es opcional, pero si viene tiene que tener formato válido
        @Email(message = "El correo no tiene un formato válido.")
        String correo,

        // "^$|" al inicio significa "o vacío", por eso el teléfono también es opcional
        @Pattern(regexp = "^$|^[0-9+()\\-\\s]{7,20}$",
                message = "El teléfono solo puede tener números, espacios, + - ( ) y entre 7 y 20 caracteres.")
        String telefono) {
}
