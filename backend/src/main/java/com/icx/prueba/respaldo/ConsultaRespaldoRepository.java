package com.icx.prueba.respaldo;

import org.springframework.data.jpa.repository.JpaRepository;

// Repositorio de la tabla consulta_respaldo.
// No tiene código porque JpaRepository ya trae save, findById, etc.
public interface ConsultaRespaldoRepository extends JpaRepository<ConsultaRespaldo, Long> {
}
