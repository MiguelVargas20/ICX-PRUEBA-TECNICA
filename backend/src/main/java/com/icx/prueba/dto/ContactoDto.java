package com.icx.prueba.dto;

// Un contacto como se lo mando al frontend.
// Es igual venga de Oracle o del respaldo en H2.
// Ojo: estos nombres son los mismos que lee el frontend (contacto.nombre, contacto.correo...),
// si se cambian aquí hay que cambiarlos allá también.
public record ContactoDto(
        String id,
        String nombre,
        String apellido,
        String ciudad,
        String correo,
        String telefono) {
}
