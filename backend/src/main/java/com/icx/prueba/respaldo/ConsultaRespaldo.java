package com.icx.prueba.respaldo;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * Tabla CONSULTA_RESPALDO en H2. Siempre tiene UNA sola fila (id = 1).
 * Guarda CUÁNDO se hizo la última consulta y CON QUÉ filtros,
 * así el usuario sabe qué está viendo cuando Oracle está caído.
 * (Se guarda aparte para no perder la fecha aunque la consulta haya traído 0 contactos.)
 */
@Entity
@Table(name = "consulta_respaldo")
public class ConsultaRespaldo {

    /* Id fijo: solo nos interesa la última consulta, no el historial. */
    public static final long ID_UNICO = 1L;

    @Id
    private Long id;

    @Column(nullable = false)
    private Instant fecha;

    /* Texto legible de los filtros, ej: "ciudad: Bogotá, nombre: Ana" o "sin filtros". */
    @Column(length = 400)
    private String filtros;

    protected ConsultaRespaldo() {
    }

    public ConsultaRespaldo(Instant fecha, String filtros) {
        this.id = ID_UNICO;
        this.fecha = fecha;
        this.filtros = filtros;
    }

    public Instant getFecha() {
        return fecha;
    }

    public String getFiltros() {
        return filtros;
    }
}
