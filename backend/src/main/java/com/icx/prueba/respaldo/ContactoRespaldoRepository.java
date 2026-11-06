package com.icx.prueba.respaldo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

// Repositorio de la tabla contacto_respaldo.
// JpaRepository ya trae saveAll, deleteAllInBatch, findAll, etc., no hay que escribir SQL.
public interface ContactoRespaldoRepository extends JpaRepository<ContactoRespaldo, String> {

    // Spring arma la consulta a partir del nombre del método:
    // trae todos ordenados por la columna "orden" (SELECT ... ORDER BY orden ASC)
    List<ContactoRespaldo> findAllByOrderByOrdenAsc();
}
