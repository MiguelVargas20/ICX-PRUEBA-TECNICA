package com.icx.prueba.dto;

/* DTO para representar un contacto. */
public record ContactoDto(
        String id,
        String nombre,
        String apellido,
        String ciudad,
        String correo,
        String telefono) {
}
