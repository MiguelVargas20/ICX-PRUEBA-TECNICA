package com.icx.prueba.respaldo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/*
 * Spring Data crea la implementación sola: no hay que escribir SQL.
 * JpaRepository ya trae saveAll, deleteAllInBatch, count, etc.
 */
public interface ContactoRespaldoRepository extends JpaRepository<ContactoRespaldo, String> {

    /* Spring entiende el nombre del método y genera: SELECT ... ORDER BY orden ASC */
    /*
     * findAll... es una convención de Spring Data.
     * Por OrderByOrdenAsc garantiza que se devuelva el respaldo en el mismo
     * orden en que aparecieron los contactos en la consulta de Oracle.
     */
    List<ContactoRespaldo> findAllByOrderByOrdenAsc();
}
