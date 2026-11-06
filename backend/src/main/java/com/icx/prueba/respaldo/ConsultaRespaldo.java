package com.icx.prueba.respaldo;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Tabla consulta_respaldo en H2. Siempre tiene una sola fila (id = 1).
// Guarda cuándo fue la última consulta y con qué filtros, para mostrarlo en el aviso
// cuando Oracle está caído.
// La dejé aparte de los contactos para no perder la fecha si la consulta trajo 0 contactos.
@Entity
@Table(name = "consulta_respaldo")
public class ConsultaRespaldo {

    // Id fijo porque solo me interesa la última consulta, no un historial
    public static final long ID_UNICO = 1L;

    @Id
    private Long id;

    @Column(nullable = false)
    private Instant fecha;

    // Texto de los filtros, ej: "ciudad: Bogotá, nombre: Ana" o "sin filtros"
    @Column(length = 400)
    private String filtros;

    // JPA necesita un constructor vacío para armar el objeto cuando lee de la base
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
