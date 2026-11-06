package com.icx.prueba.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.UnknownContentTypeException;

import com.icx.prueba.client.OracleClient;
import com.icx.prueba.dto.ContactoDto;
import com.icx.prueba.dto.ContactoRequest;
import com.icx.prueba.dto.ContactoUpdate;
import com.icx.prueba.dto.QueryResultsResponse;
import com.icx.prueba.dto.ResultadoBusqueda;
import com.icx.prueba.exception.ContactoNoEncontradoException;
import com.icx.prueba.respaldo.RespaldoService;

// Aquí está la lógica de los contactos.
// - Arma la consulta ROQL y los JSON que se mandan a Oracle.
// - Convierte lo que responde Oracle en ContactoDto.
// - Decide cuándo usar el respaldo de H2.
// Usa OracleClient para hablar con Oracle y RespaldoService para H2.
@Service
public class ContactoService {

    // Máximo de resultados por búsqueda (en el frontend está el mismo número)
    private static final int LIMITE = 50;

    // En Oracle el tipo 0 es el correo y el teléfono principal
    private static final int TIPO_PRINCIPAL = 0;

    private static final Logger log = LoggerFactory.getLogger(ContactoService.class);

    private final OracleClient oracleClient;
    private final RespaldoService respaldoService;

    // Spring me pasa OracleClient y RespaldoService ya creados (inyección de dependencias),
    // por eso no hago new en ningún lado
    public ContactoService(OracleClient oracleClient, RespaldoService respaldoService) {
        this.oracleClient = oracleClient;
        this.respaldoService = respaldoService;
    }

    /* ===================== BUSCAR ===================== */

    // Busca contactos en Oracle:
    // - Si Oracle responde: guardo el resultado en H2 y lo devuelvo.
    // - Si Oracle no responde: devuelvo la última consulta guardada en H2.
    // - Si tampoco hay nada guardado: lanzo el error y el ApiExceptionHandler responde 503.
    public ResultadoBusqueda buscar(String ciudad, String nombre, String correo, String telefono) {
        String roql = construirConsulta(ciudad, nombre, correo, telefono);

        List<ContactoDto> contactos;
        try {
            contactos = convertirRespuestaOracle(oracleClient.consultar(roql));
        } catch (RestClientException e) {
            if (!permiteUsarRespaldo(e)) {
                throw e; // ej: un 400 no es desconexión, se reporta tal cual
            }
            log.warn("Oracle no disponible ({}). Se intenta usar el respaldo.", e.getMessage());
            // leer() devuelve Optional: si hay respaldo lo devuelvo, si no, vuelvo a lanzar el error
            return respaldoService.leer().orElseThrow(() -> e);
        }

        guardarRespaldo(contactos, crearTextoFiltros(ciudad, nombre, correo, telefono));
        return ResultadoBusqueda.desdeOracle(contactos);
    }

    /* ===================== CREAR ===================== */

    // Crea el contacto en Oracle en una sola petición (con correo y teléfono si vienen)
    public ContactoDto crear(ContactoRequest datos) {
        // Armo el JSON como Oracle lo pide. LinkedHashMap para que quede en orden si lo imprimo.
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("name", Map.of("first", datos.nombre().trim(), "last", datos.apellido().trim()));

        if (hayTexto(datos.ciudad())) {
            cuerpo.put("address", Map.of("city", datos.ciudad().trim()));
        }
        agregarCorreoTelefonoPrincipal(cuerpo, datos.correo(), datos.telefono());

        Map<String, Object> respuesta = oracleClient.crear(cuerpo);

        // Oracle devuelve el contacto creado, de ahí saco el id nuevo
        Object id = respuesta == null ? null : respuesta.get("id");
        return new ContactoDto(
                id == null ? null : String.valueOf(id),
                datos.nombre().trim(),
                datos.apellido().trim(),
                valorONulo(datos.ciudad()),
                valorONulo(datos.correo()),
                valorONulo(datos.telefono()));
    }

    /* ===================== ACTUALIZAR ===================== */

    // Edita nombre, apellido, ciudad y, si vienen, el correo y el teléfono principal
    public void actualizar(long id, ContactoUpdate datos) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("name", Map.of("first", datos.nombre().trim(), "last", datos.apellido().trim()));

        // La ciudad se manda siempre que venga:
        // con texto se guarda, y vacía se manda null para que Oracle la borre.
        // Uso HashMap porque Map.of no deja poner null.
        if (datos.ciudad() != null) {
            Map<String, Object> direccion = new HashMap<>();
            direccion.put("city", valorONulo(datos.ciudad()));
            cuerpo.put("address", direccion);
        }
        agregarCorreoTelefonoPrincipal(cuerpo, datos.correo(), datos.telefono());
        try {
            oracleClient.actualizar(id, cuerpo);
        } catch (RestClientResponseException e) {
            throw convertir404DeContacto(e, id);
        }
    }

    /* ===================== ELIMINAR ===================== */

    public void eliminar(long id) {
        try {
            oracleClient.eliminar(id);
        } catch (RestClientResponseException e) {
            throw convertir404DeContacto(e, id);
        }
    }

    // Si Oracle responde 404 al pedir este id, es porque el contacto no existe.
    // Cualquier otro error lo devuelvo igual para que lo maneje el ApiExceptionHandler.
    // Devuelvo el error en vez de lanzarlo, el throw lo hace quien lo llama.
    private RuntimeException convertir404DeContacto(RestClientResponseException e, long id) {
        if (e.getStatusCode().value() == 404) {
            return new ContactoNoEncontradoException(id);
        }
        return e;
    }

    /* ===================== RESPALDO ===================== */

    // Revisa si el error es porque no se pudo hablar con Oracle. Solo en ese caso uso H2:
    // - UnknownContentTypeException: Oracle respondió HTML en vez de JSON (sitio deshabilitado)
    // - ResourceAccessException: no hubo respuesta (sin internet, Oracle caído o timeout)
    // - 5xx: error interno de Oracle
    // - 401/403: Oracle no nos deja entrar
    // Un 400 o 404 no cuenta: Oracle sí respondió, el problema son los datos.
    private boolean permiteUsarRespaldo(RestClientException e) {
        if (e instanceof UnknownContentTypeException) {
            return true;
        }
        if (e instanceof ResourceAccessException) {
            return true;
        }
        if (e instanceof RestClientResponseException respuesta) {
            int codigo = respuesta.getStatusCode().value();
            return codigo >= 500 || codigo == 401 || codigo == 403;
        }
        return false;
    }

    // Si falla H2 no quiero que falle la búsqueda: el usuario igual recibe los datos de Oracle
    // y el error queda en el log
    private void guardarRespaldo(List<ContactoDto> contactos, String filtros) {
        try {
            respaldoService.guardar(contactos, filtros);
        } catch (RuntimeException e) {
            log.error("No se pudo actualizar el respaldo en H2", e);
        }
    }

    // Arma el texto de los filtros para el aviso: "ciudad: Bogotá, nombre: Ana" o "sin filtros"
    private String crearTextoFiltros(String ciudad, String nombre, String correo, String telefono) {
        List<String> partes = new ArrayList<>();
        if (hayTexto(ciudad)) partes.add("ciudad: " + ciudad.trim());
        if (hayTexto(nombre)) partes.add("nombre: " + nombre.trim());
        if (hayTexto(correo)) partes.add("correo: " + correo.trim());
        if (hayTexto(telefono)) partes.add("teléfono: " + telefono.trim());
        return partes.isEmpty() ? "sin filtros" : String.join(", ", partes);
    }

    /* ===================== AUXILIARES ===================== */

    // Agrega el correo y el teléfono principal (tipo 0) al JSON. Lo usan crear y actualizar.
    // En Oracle emails y phones son listas y el tipo funciona como llave:
    // si mando addressType 0, Oracle cambia el correo principal.
    // Si un dato viene vacío no lo mando y Oracle deja el que tenía.
    private void agregarCorreoTelefonoPrincipal(Map<String, Object> cuerpo, String correo, String telefono) {
        if (hayTexto(correo)) {
            cuerpo.put("emails", List.of(Map.of(
                    "address", correo.trim(),
                    "addressType", Map.of("id", TIPO_PRINCIPAL))));
        }
        if (hayTexto(telefono)) {
            cuerpo.put("phones", List.of(Map.of(
                    "number", telefono.trim(),
                    "phoneType", Map.of("id", TIPO_PRINCIPAL))));
        }
    }

    // Arma la consulta ROQL con los filtros que lleguen. Ejemplo con ciudad = Cali:
    // SELECT ID, ... FROM Contacts WHERE Address.City LIKE '%Cali%' LIMIT 50
    private String construirConsulta(String ciudad, String nombre, String correo, String telefono) {
        List<String> filtros = new ArrayList<>();

        // LIKE con % busca que "contenga" el texto
        if (hayTexto(ciudad)) {
            filtros.add("Address.City LIKE '%" + escaparComillasROQL(ciudad) + "%'");
        }

        // El nombre se busca en nombre o en apellido
        if (hayTexto(nombre)) {
            String n = escaparComillasROQL(nombre);
            filtros.add("(Name.First LIKE '%" + n + "%' OR Name.Last LIKE '%" + n + "%')");
        }

        if (hayTexto(correo)) {
            filtros.add("Emails.Address LIKE '%" + escaparComillasROQL(correo) + "%'");
        }

        if (hayTexto(telefono)) {
            filtros.add("Phones.Number LIKE '%" + escaparComillasROQL(telefono) + "%'");
        }

        String consulta = "SELECT ID, Name.First, Name.Last, Address.City, "
                + "Emails.Address, Phones.Number FROM Contacts";
        // Los filtros se unen con AND: se tienen que cumplir todos
        if (!filtros.isEmpty()) {
            consulta += " WHERE " + String.join(" AND ", filtros);
        }

        // LIMIT para no traer miles de contactos
        return consulta + " LIMIT " + LIMITE;
    }

    private boolean hayTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    // Quita los espacios de los lados, y si queda vacío devuelve null
    private String valorONulo(String valor) {
        return hayTexto(valor) ? valor.trim() : null;
    }

    // Duplica las comillas simples (' -> '') para que un texto como O'Brien
    // no rompa la consulta ni deje meter condiciones extra
    private String escaparComillasROQL(String valor) {
        return valor.trim().replace("'", "''");
    }

    // Pasa la respuesta de Oracle (columnas + filas) a una lista de ContactoDto
    private List<ContactoDto> convertirRespuestaOracle(QueryResultsResponse respuesta) {
        if (respuesta == null || respuesta.items() == null || respuesta.items().isEmpty()) {
            return List.of();
        }

        QueryResultsResponse.Item item = respuesta.items().get(0);
        if (item.rows() == null) {
            return List.of();
        }

        List<String> columnas = item.columnNames();
        // Oracle devuelve una fila por cada combinación de correo y teléfono,
        // así que un mismo contacto puede salir varias veces (el 205 salía 15 veces).
        // Los agrupo por id en este mapa. LinkedHashMap para mantener el orden.
        Map<String, ContactoDto> porId = new LinkedHashMap<>();

        for (List<Object> fila : item.rows()) {
            // Armo un mapa columna -> valor, ej: "first" -> "Paula"
            Map<String, String> valores = new HashMap<>();
            for (int i = 0; i < columnas.size(); i++) {
                Object valor = fila.get(i);
                valores.put(columnas.get(i), valor == null ? null : String.valueOf(valor));
            }

            // Estos nombres ("first", "city", "number"...) son los que manda Oracle en columnNames
            String id = valores.get("id");
            ContactoDto nuevo = new ContactoDto(
                    id,
                    valores.get("first"),
                    valores.get("last"),
                    valores.get("city"),
                    valores.get("address"),
                    valores.get("number"));

            // Si el contacto ya estaba, junto las dos filas para no perder el correo o el teléfono
            ContactoDto anterior = porId.get(id);
            porId.put(id, anterior == null ? nuevo : combinarFilasDelContacto(anterior, nuevo));
        }

        return new ArrayList<>(porId.values());
    }

    // Junta dos filas del mismo contacto: si a la primera le falta el correo o el teléfono,
    // lo toma de la otra fila
    private ContactoDto combinarFilasDelContacto(ContactoDto filaAnterior, ContactoDto filaNueva) {
        return new ContactoDto(
                filaAnterior.id(), filaAnterior.nombre(), filaAnterior.apellido(), filaAnterior.ciudad(),
                filaAnterior.correo() != null ? filaAnterior.correo() : filaNueva.correo(),
                filaAnterior.telefono() != null ? filaAnterior.telefono() : filaNueva.telefono());
    }
}
