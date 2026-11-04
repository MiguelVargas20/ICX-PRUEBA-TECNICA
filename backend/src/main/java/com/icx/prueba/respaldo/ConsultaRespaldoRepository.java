package com.icx.prueba.respaldo;

import org.springframework.data.jpa.repository.JpaRepository;

/* Acceso a la fila única con la fecha y los filtros de la última consulta. */
public interface ConsultaRespaldoRepository extends JpaRepository<ConsultaRespaldo, Long> {
}
