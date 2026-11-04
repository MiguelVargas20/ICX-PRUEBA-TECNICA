package com.icx.prueba.dto;

/* DTO para representar un contacto. */
/*
 * Este objeto es lo que se devuelve al frontend, independientemente de si
 * los datos vinieron de Oracle o de la base H2 de respaldo.
 * Los campos conservan los nombres esperados por las tablas y formularios.
 */
public record ContactoDto(
        String id,
        String nombre,
        String apellido,
        String ciudad,
        String correo,
        String telefono) {
}
