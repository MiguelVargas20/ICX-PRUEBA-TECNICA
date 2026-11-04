package com.icx.prueba.respaldo;

import com.icx.prueba.dto.ContactoDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * Tabla CONTACTO_RESPALDO en H2.
 * Cada fila es un contacto que llegó en la ÚLTIMA consulta exitosa a Oracle.
 * Cuando Oracle no responde, el backend lee esta tabla en lugar de Oracle.
 */
@Entity
@Table(name = "contacto_respaldo")
public class ContactoRespaldo {

    /* Usamos el mismo id que tiene el contacto en Oracle. */
    @Id
    private String id;

    private String nombre;
    private String apellido;
    private String ciudad;
    private String correo;
    private String telefono;

    /* Posición en la que venía en la consulta, para mostrarlos en el mismo orden. */
    @Column(nullable = false)
    private int orden;

    /* JPA necesita un constructor vacío para poder crear el objeto al leer de la BD. */
    protected ContactoRespaldo() {
    }

    /* Convierte un ContactoDto (lo que devolvió Oracle) en una fila para guardar. */
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

    /* Convierte la fila guardada otra vez en ContactoDto (lo que entiende el frontend). */
    public ContactoDto aDto() {
        return new ContactoDto(id, nombre, apellido, ciudad, correo, telefono);
    }
}
