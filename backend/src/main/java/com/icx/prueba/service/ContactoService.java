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

/* Servicio para la gestión de contactos. */
/*
 * Esta capa contiene la lógica de negocio. El controlador recibe una petición,
 * pero no sabe cómo hablar con Oracle ni cómo preparar una consulta ROQL.
 * El servicio recibe los datos, prepara las operaciones y coordina Oracle + H2.
 */
@Service
public class ContactoService {

    /* Límite de resultados para la consulta. */
    private static final int LIMITE = 50;

    /* Tipo 0 = el principal tanto para correo como para teléfono (el que probaste en Postman). */
    private static final int TIPO_PRINCIPAL = 0;

    private static final Logger log = LoggerFactory.getLogger(ContactoService.class);

    /* Cliente para interactuar con Oracle. */
    private final OracleClient oracleClient;

    /* Base de datos de respaldo (H2) para cuando Oracle no responde. */
    private final RespaldoService respaldoService;

    /* Constructor del servicio. Spring pasa los dos beans automáticamente. */
    public ContactoService(OracleClient oracleClient, RespaldoService respaldoService) {
        /*
         * Inyección de dependencias: Spring creó OracleClient y RespaldoService
         * antes que esta clase y los entrega aquí sin que nosotros hagamos new().
         */
        this.oracleClient = oracleClient;
        this.respaldoService = respaldoService;
    }

    /* ===================== BUSCAR ===================== */

    /*
     * Busca contactos en Oracle.
     *  - Si Oracle responde: se guarda el resultado en H2 (respaldo) y se devuelve.
     *  - Si Oracle NO responde: se devuelve la última consulta guardada en H2.
     *  - Si tampoco hay respaldo: se lanza el error original (el ApiExceptionHandler lo convierte en 503).
     */
    public ResultadoBusqueda buscar(String ciudad, String nombre, String correo, String telefono) {
        /*
         * Primero se transforma la petición HTTP en una consulta ROQL que Oracle
         * pueda entender. La parte de filtros se construye en construirConsulta().
         */
        String roql = construirConsulta(ciudad, nombre, correo, telefono);

        List<ContactoDto> contactos;
        try {
            contactos = convertir(oracleClient.consultar(roql));
        } catch (RestClientException e) {
            if (!esFallaDeConexion(e)) {
                throw e; // Ej: 400 por datos mal enviados → no es una desconexión, se reporta tal cual.
            }
            log.warn("Oracle no disponible ({}). Se intenta usar el respaldo.", e.getMessage());
            /*
             * read() devuelve Optional. Si existe respaldo se lee; si nunca hubo
             * una consulta exitosa, orElseThrow() vuelve a lanzar el error original.
             */
            return respaldoService.leer().orElseThrow(() -> e);
        }

        guardarRespaldo(contactos, describirFiltros(ciudad, nombre, correo, telefono));
        return ResultadoBusqueda.desdeOracle(contactos);
    }

    /* ===================== CREAR ===================== */

    /* Crea un contacto (con correo y teléfono si vienen) en una sola petición a Oracle. */
    public ContactoDto crear(ContactoRequest datos) {
        /*
         * LinkedHashMap conserva el orden de las claves. No afecta el resultado,
         * pero facilita leer el JSON que se enviará y revisar logs o pruebas.
         */
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("name", Map.of("first", datos.nombre().trim(), "last", datos.apellido().trim()));

        if (tieneTexto(datos.ciudad())) {
            cuerpo.put("address", Map.of("city", datos.ciudad().trim()));
        }
                agregarCorreoYTelefono(cuerpo, datos.correo(), datos.telefono());

        Map<String, Object> respuesta = oracleClient.crear(cuerpo);

        /* Oracle devuelve el contacto completo; de ahí sacamos el id nuevo. */
        Object id = respuesta == null ? null : respuesta.get("id");
        return new ContactoDto(
                id == null ? null : String.valueOf(id),
                datos.nombre().trim(),
                datos.apellido().trim(),
                vacioANulo(datos.ciudad()),
                vacioANulo(datos.correo()),
                vacioANulo(datos.telefono()));
    }

    /* ===================== ACTUALIZAR ===================== */

    /* Actualiza nombre, apellido, ciudad y (si vienen) correo y teléfono principales. */
    public void actualizar(long id, ContactoUpdate datos) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("name", Map.of("first", datos.nombre().trim(), "last", datos.apellido().trim()));

        /*
         * La ciudad se envía siempre que venga en la petición:
         *  - con texto → se guarda esa ciudad.
         *  - vacía ("") → se envía null para que Oracle la BORRE.
         * HashMap y no Map.of, porque Map.of no acepta valores null.
         */
        if (datos.ciudad() != null) {
            Map<String, Object> direccion = new HashMap<>();
            direccion.put("city", vacioANulo(datos.ciudad()));
            cuerpo.put("address", direccion);
        }
        agregarCorreoYTelefono(cuerpo, datos.correo(), datos.telefono());
        try {
            oracleClient.actualizar(id, cuerpo);
        } catch (RestClientResponseException e) {
            throw siNoExiste(e, id);
        }
    }

    /* ===================== ELIMINAR ===================== */

    /* Elimina un contacto por su id. */
    /* Elimina un contacto por su id. */
    public void eliminar(long id) {
        try {
            oracleClient.eliminar(id);
        } catch (RestClientResponseException e) {
            throw siNoExiste(e, id);
        }
    }

    /*
     * Si Oracle respondió 404 al pedir ESTE contacto, el contacto no existe.
     * Cualquier otro error se devuelve tal cual para que lo maneje el ApiExceptionHandler.
     */
    private RuntimeException siNoExiste(RestClientResponseException e, long id) {
        if (e.getStatusCode().value() == 404) {
            return new ContactoNoEncontradoException(id);
        }
        return e;
    }

    /* ===================== RESPALDO ===================== */
    /*
     * ¿El error significa que "no podemos hablar con Oracle"?
     *  - UnknownContentTypeException: Oracle respondió una página web (HTML) en vez de JSON.
     *  - ResourceAccessException: no hubo respuesta (sin internet, servidor caído, timeout).
     *  - 5xx: Oracle está caído o con problemas internos.
     *  - 401/403: Oracle no nos deja entrar (por ejemplo, el acceso está deshabilitado).
     * Un 400 o 404 NO cuenta: Oracle sí respondió, el problema es de los datos.
     */
    private boolean esFallaDeConexion(RestClientException e) {
        if (e instanceof UnknownContentTypeException) {
            return true; // Oracle respondió algo que no es JSON (ej. página HTML de sitio deshabilitado).
        }
        /*
         * instanceof permite comprobar primero qué clase de excepción llegó.
         * Así se decide si el problema permite usar respaldo o debe propagarse.
         */
        if (e instanceof ResourceAccessException) {
            return true;
        }
        if (e instanceof RestClientResponseException respuesta) {
            int codigo = respuesta.getStatusCode().value();
            return codigo >= 500 || codigo == 401 || codigo == 403;
        }
        return false;
    }

    /* Si falla guardar en H2, el usuario igual recibe los datos de Oracle: solo se registra en el log. */
    private void guardarRespaldo(List<ContactoDto> contactos, String filtros) {
        try {
            respaldoService.guardar(contactos, filtros);
        } catch (RuntimeException e) {
            log.error("No se pudo actualizar el respaldo en H2", e);
        }
    }

    /* Texto legible de los filtros usados: "ciudad: Bogotá, nombre: Ana" o "sin filtros". */
    private String describirFiltros(String ciudad, String nombre, String correo, String telefono) {
        List<String> partes = new ArrayList<>();
        if (tieneTexto(ciudad)) partes.add("ciudad: " + ciudad.trim());
        if (tieneTexto(nombre)) partes.add("nombre: " + nombre.trim());
        if (tieneTexto(correo)) partes.add("correo: " + correo.trim());
        if (tieneTexto(telefono)) partes.add("teléfono: " + telefono.trim());
        return partes.isEmpty() ? "sin filtros" : String.join(", ", partes);
    }

    /* ===================== AUXILIARES ===================== */



    /*
     * Agrega el correo y el teléfono PRINCIPALES (tipo 0) al cuerpo que se envía a Oracle.
     * Lo usan crear y actualizar. Si un dato viene vacío, no se envía (Oracle conserva el que tenga).
     * El tipo es la "llave" de la lista: enviar addressType 0 reemplaza el correo principal.
     */
    private void agregarCorreoYTelefono(Map<String, Object> cuerpo, String correo, String telefono) {
        if (tieneTexto(correo)) {
            cuerpo.put("emails", List.of(Map.of(
                    "address", correo.trim(),
                    "addressType", Map.of("id", TIPO_PRINCIPAL))));
        }
        if (tieneTexto(telefono)) {
            cuerpo.put("phones", List.of(Map.of(
                    "number", telefono.trim(),
                    "phoneType", Map.of("id", TIPO_PRINCIPAL))));
        }
    }

    /* Construye la consulta ROQL según los criterios proporcionados. */
    private String construirConsulta(String ciudad, String nombre, String correo, String telefono) {
        List<String> filtros = new ArrayList<>();

        /*
         * Cada filtro crea una condición ROQL.
         * LIKE con % busca coincidencias parciales, parecido a contains().
         */
        /* Filtrar por ciudad */
        if (tieneTexto(ciudad)) {
            filtros.add("Address.City LIKE '%" + limpiar(ciudad) + "%'");
        }

        /* Filtrar por nombre */
        if (tieneTexto(nombre)) {
            String n = limpiar(nombre);
            filtros.add("(Name.First LIKE '%" + n + "%' OR Name.Last LIKE '%" + n + "%')");
        }

        /* Filtrar por correo */
        if (tieneTexto(correo)) {
            filtros.add("Emails.Address LIKE '%" + limpiar(correo) + "%'");
        }
        /* Filtrar por teléfono */
        if (tieneTexto(telefono)) {
            filtros.add("Phones.Number LIKE '%" + limpiar(telefono) + "%'");
        }

        /* Construir la consulta final */
        String consulta = "SELECT ID, Name.First, Name.Last, Address.City, "
                + "Emails.Address, Phones.Number FROM Contacts";
        if (!filtros.isEmpty()) {
            consulta += " WHERE " + String.join(" AND ", filtros);
        }

        /*
         * AND hace que deban cumplirse todos los filtros seleccionados.
         * LIMIT evita descargar una cantidad excesiva de contactos.
         */
        /* Aplicar límite de resultados */
        return consulta + " LIMIT " + LIMITE;
    }

    /* Verifica si una cadena tiene texto. */
    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    /* Devuelve el texto sin espacios sobrantes, o null si está vacío. */
    private String vacioANulo(String valor) {
        return tieneTexto(valor) ? valor.trim() : null;
    }

    /** Duplica las comillas simples para que el texto del usuario no rompa la consulta. */
    private String limpiar(String valor) {
        return valor.trim().replace("'", "''");
    }

    /* Convierte los resultados de la consulta en una lista de DTOs. */
    private List<ContactoDto> convertir(QueryResultsResponse respuesta) {
        if (respuesta == null || respuesta.items() == null || respuesta.items().isEmpty()) {
            // Oracle puede responder sin datos; en ese caso se representa como lista vacía.
            return List.of();
        }

        /* Procesar los resultados de la consulta */
        QueryResultsResponse.Item item = respuesta.items().get(0);
        if (item.rows() == null) {
            return List.of();
        }

        /* Obtener los nombres de las columnas */
        List<String> columnas = item.columnNames();
        // Un contacto con varios correos o teléfonos puede salir en varias filas: se deja la primera.
        Map<String, ContactoDto> porId = new LinkedHashMap<>();

        /* Recorrer las filas de resultados */
        for (List<Object> fila : item.rows()) {
            Map<String, String> valores = new HashMap<>();
            /*
             * QueryResults devuelve una fila como lista: primero viene el valor,
             * luego el siguiente, según el orden de columnNames.
             * Este ciclo convierte posiciones en pares nombre -> valor.
             */
            for (int i = 0; i < columnas.size(); i++) {
                Object valor = fila.get(i);
                valores.put(columnas.get(i), valor == null ? null : String.valueOf(valor));
            }

            /* Crear un DTO para cada contacto */
            String id = valores.get("id");
            porId.putIfAbsent(id, new ContactoDto(
                    id,
                    valores.get("first"),
                    valores.get("last"),
                    valores.get("city"),
                    valores.get("address"),
                    valores.get("number")));
        }

        /* Devolver la lista de DTOs */
        return new ArrayList<>(porId.values());
    }
}
