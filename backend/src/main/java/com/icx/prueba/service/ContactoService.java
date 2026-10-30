package com.icx.prueba.service;

import com.icx.prueba.client.OracleClient;
import com.icx.prueba.dto.ContactoDto;
import com.icx.prueba.dto.ContactoRequest;
import com.icx.prueba.dto.ContactoUpdate;
import com.icx.prueba.dto.QueryResultsResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/* Servicio para la gestión de contactos. */
@Service
public class ContactoService {

    /* Límite de resultados para la consulta. */
    private static final int LIMITE = 50;

    /* Tipo 0 = el principal tanto para correo como para teléfono (el que probaste en Postman). */
    private static final int TIPO_PRINCIPAL = 0;

    /* Cliente para interactuar con Oracle. */
    private final OracleClient oracleClient;

    /* Constructor del servicio. */
    public ContactoService(OracleClient oracleClient) {
        this.oracleClient = oracleClient;
    }

    /* ===================== BUSCAR ===================== */

    /* Busca contactos según los criterios proporcionados. */
    public List<ContactoDto> buscar(String ciudad, String nombre, String correo, String telefono) {
        String roql = construirConsulta(ciudad, nombre, correo, telefono);
        QueryResultsResponse respuesta = oracleClient.consultar(roql);
        return convertir(respuesta);
    }

    /* ===================== CREAR ===================== */

    /* Crea un contacto (con correo y teléfono si vienen) en una sola petición a Oracle. */
    public ContactoDto crear(ContactoRequest datos) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("name", Map.of("first", datos.nombre().trim(), "last", datos.apellido().trim()));

        if (tieneTexto(datos.ciudad())) {
            cuerpo.put("address", Map.of("city", datos.ciudad().trim()));
        }
        if (tieneTexto(datos.correo())) {
            cuerpo.put("emails", List.of(Map.of(
                    "address", datos.correo().trim(),
                    "addressType", Map.of("id", TIPO_PRINCIPAL))));
        }
        if (tieneTexto(datos.telefono())) {
            cuerpo.put("phones", List.of(Map.of(
                    "number", datos.telefono().trim(),
                    "phoneType", Map.of("id", TIPO_PRINCIPAL))));
        }

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

    /* Actualiza nombre, apellido y (si viene) ciudad. Solo se envían los campos que cambian. */
    public void actualizar(long id, ContactoUpdate datos) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("name", Map.of("first", datos.nombre().trim(), "last", datos.apellido().trim()));

        if (tieneTexto(datos.ciudad())) {
            cuerpo.put("address", Map.of("city", datos.ciudad().trim()));
        }
        oracleClient.actualizar(id, cuerpo);
    }

    /* ===================== ELIMINAR ===================== */

    /* Elimina un contacto por su id. */
    public void eliminar(long id) {
        oracleClient.eliminar(id);
    }

    /* ===================== AUXILIARES ===================== */

    /* Construye la consulta ROQL según los criterios proporcionados. */
    private String construirConsulta(String ciudad, String nombre, String correo, String telefono) {
        List<String> filtros = new ArrayList<>();

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
