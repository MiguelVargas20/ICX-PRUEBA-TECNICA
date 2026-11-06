package com.icx.prueba.respaldo;

import com.icx.prueba.dto.ContactoDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Tabla contacto_respaldo en H2.
// Cada fila es un contacto de la última consulta que salió bien en Oracle.
// Si Oracle se cae, el backend lee esta tabla.
@Entity
@Table(name = "contacto_respaldo")
public class ContactoRespaldo {

    // Uso el mismo id que tiene el contacto en Oracle
    @Id
    private String id;

    private String nombre;
    private String apellido;
    private String ciudad;
    private String correo;
    private String telefono;

    // Posición del contacto en la consulta, para mostrarlos en el mismo orden
    @Column(nullable = false)
    private int orden;

    // JPA necesita un constructor vacío para armar el objeto cuando lee de la base
    protected ContactoRespaldo() {
    }

    // Pasa un ContactoDto (lo que llegó de Oracle) a una fila para guardar
    public static ContactoRespaldo desde(ContactoDto dto, int orden) {
        ContactoRespaldo fila = new ContactoRespaldo();
        fila.id = dto.id();
        fila.nombre = dto.nombre();
        fila.apellido = dto.apellido();
        fila.ciudad = dto.ciudad();
        fila.correo = dto.correo();
        fila.telefono = dto.telefono();
        fila.orden = orden;
        return fila;
    }

    // Lo contrario: pasa la fila guardada a ContactoDto para mandarla al frontend
    public ContactoDto aDto() {
        return new ContactoDto(id, nombre, apellido, ciudad, correo, telefono);
    }
}
