package com.icx.prueba.respaldo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.icx.prueba.dto.ContactoDto;
import com.icx.prueba.dto.ResultadoBusqueda;

// Todo lo de la base de respaldo (H2) está aquí.
// ContactoService solo llama guardar() y leer(), no sabe cómo se guarda.
@Service
public class RespaldoService {

    private final ContactoRespaldoRepository contactos;
    private final ConsultaRespaldoRepository consulta;

    public RespaldoService(ContactoRespaldoRepository contactos, ConsultaRespaldoRepository consulta) {
        this.contactos = contactos;
        this.consulta = consulta;
    }

    // Cambia el respaldo por el resultado de la consulta que acaba de salir bien.
    // @Transactional: borrar lo viejo y guardar lo nuevo pasa todo junto.
    // Si algo falla a la mitad se deshace todo y queda el respaldo anterior,
    // así nunca queda la tabla vacía o a medias.
    @Transactional
    public void guardar(List<ContactoDto> resultado, String filtros) {
        contactos.deleteAllInBatch();

        List<ContactoRespaldo> filas = new ArrayList<>();
        for (int i = 0; i < resultado.size(); i++) {
            filas.add(ContactoRespaldo.desde(resultado.get(i), i));
        }
        contactos.saveAll(filas);

        // Como el id siempre es 1, save() actualiza esa fila en vez de crear otra
        consulta.save(new ConsultaRespaldo(Instant.now(), filtros));
    }

    // Devuelve la última consulta guardada.
    // Uso Optional porque si nunca se ha guardado nada, viene vacío.
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
