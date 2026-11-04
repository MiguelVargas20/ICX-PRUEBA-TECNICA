package com.icx.prueba.respaldo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.icx.prueba.dto.ContactoDto;
import com.icx.prueba.dto.ResultadoBusqueda;

/*
 * Todo lo que tiene que ver con la base de datos de respaldo (H2) está aquí.
 * ContactoService no sabe cómo se guarda: solo llama a guardar() y leer().
 */
@Service
public class RespaldoService {

    private final ContactoRespaldoRepository contactos;
    private final ConsultaRespaldoRepository consulta;

    public RespaldoService(ContactoRespaldoRepository contactos, ConsultaRespaldoRepository consulta) {
        this.contactos = contactos;
        this.consulta = consulta;
    }

    /*
     * Reemplaza el respaldo por el resultado de la consulta que acaba de salir bien.
     * @Transactional: se borra lo viejo y se guarda lo nuevo en UN solo paso.
     * Si algo falla a la mitad, se deshace todo y el respaldo anterior queda intacto.
     */
    @Transactional
    public void guardar(List<ContactoDto> resultado, String filtros) {
        /*
         * Primero se elimina el respaldo anterior completo.
         * Luego se guarda la lista nueva y la información de su consulta.
         */
        contactos.deleteAllInBatch();

        List<ContactoRespaldo> filas = new ArrayList<>();
        for (int i = 0; i < resultado.size(); i++) {
            filas.add(ContactoRespaldo.desde(resultado.get(i), i));
        }
        contactos.saveAll(filas);

        /* save() con el mismo id (1) actualiza la fila en vez de crear otra. */
        consulta.save(new ConsultaRespaldo(Instant.now(), filtros));
    }

    /* Devuelve la última consulta guardada, o vacío si nunca se ha guardado ninguna. */
    @Transactional(readOnly = true)
    public Optional<ResultadoBusqueda> leer() {
        return consulta.findById(ConsultaRespaldo.ID_UNICO).map(info -> {
            List<ContactoDto> lista = contactos.findAllByOrderByOrdenAsc().stream()
                    .map(ContactoRespaldo::aDto)
                    .toList();
            return ResultadoBusqueda.desdeRespaldo(lista, info.getFecha(), info.getFiltros());
        });
    }
}
