package com.icx.prueba.service;

import com.icx.prueba.client.OracleClient;
import com.icx.prueba.dto.ContactoDto;
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

    /* Cliente para interactuar con Oracle. */
    private final OracleClient oracleClient;

    /* Constructor del servicio. */
    public ContactoService(OracleClient oracleClient) {
        this.oracleClient = oracleClient;
    }


    /* Busca contactos según los criterios proporcionados. */
    public List<ContactoDto> buscar(String ciudad, String nombre, String correo, String telefono) {
        String roql = construirConsulta(ciudad, nombre, correo, telefono);
        QueryResultsResponse respuesta = oracleClient.consultar(roql);
        return convertir(respuesta);
    }

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
