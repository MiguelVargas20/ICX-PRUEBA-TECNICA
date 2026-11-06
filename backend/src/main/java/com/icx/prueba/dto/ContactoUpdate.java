package com.icx.prueba.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Datos que llegan del formulario para EDITAR un contacto.
// Lo dejé separado de ContactoRequest porque al editar la ciudad se maneja distinto
// (si llega vacía se borra en Oracle).
public record ContactoUpdate(

        // Nombre y apellido siguen siendo obligatorios al editar
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 80, message = "El nombre no puede pasar de 80 caracteres.")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio.")
        @Size(max = 80, message = "El apellido no puede pasar de 80 caracteres.")
        String apellido,

        // null = no se toca la ciudad. Vacía ("") = se borra en Oracle.
        @Size(max = 80, message = "La ciudad no puede pasar de 80 caracteres.")
        String ciudad,

        // Mismas reglas que al crear. Si llegan vacíos no se cambian en Oracle.
        @Email(message = "El correo no tiene un formato válido.")
        String correo,

        @Pattern(regexp = "^$|^[0-9+()\\-\\s]{7,20}$",
                message = "El teléfono solo puede tener números, espacios, + - ( ) y entre 7 y 20 caracteres.")
        String telefono) {
}
