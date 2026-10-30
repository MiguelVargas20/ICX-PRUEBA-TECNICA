package com.icx.prueba.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.icx.prueba.dto.ContactoDto;
import com.icx.prueba.service.ContactoService;

@RestController

/* Controlador para gestionar las operaciones relacionadas con los contactos. */
@RequestMapping("/api/contactos")
public class ContactoController {

    /* Servicio para gestionar las operaciones relacionadas con los contactos. */
    private final ContactoService service;

    /* Constructor para inicializar el servicio de contactos. */
    public ContactoController(ContactoService service) {
        this.service = service;
    }

    /* Método para buscar contactos según los criterios especificados. */
    @GetMapping
    public List<ContactoDto> buscar(
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String telefono) {
        return service.buscar(ciudad, nombre, correo, telefono);
    }
}
