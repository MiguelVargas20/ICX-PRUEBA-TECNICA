package com.icx.prueba.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.icx.prueba.dto.ContactoDto;
import com.icx.prueba.dto.ContactoRequest;
import com.icx.prueba.dto.ContactoUpdate;
import com.icx.prueba.service.ContactoService;

import jakarta.validation.Valid;

/* Controlador para gestionar las operaciones relacionadas con los contactos. */
@RestController
@RequestMapping("/api/contactos")
public class ContactoController {

    /* Servicio para gestionar las operaciones relacionadas con los contactos. */
    private final ContactoService service;

    /* Constructor para inicializar el servicio de contactos. */
    public ContactoController(ContactoService service) {
        this.service = service;
    }

    /* GET /api/contactos?ciudad=&nombre=&correo=&telefono= → lista filtrada. */
    @GetMapping
    public List<ContactoDto> buscar(
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String telefono) {
        return service.buscar(ciudad, nombre, correo, telefono);
    }

    /* POST /api/contactos → crea un contacto. @Valid revisa las reglas del ContactoRequest. */
    @PostMapping
    public ResponseEntity<ContactoDto> crear(@Valid @RequestBody ContactoRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(datos));
    }

    /* PUT /api/contactos/{id} → actualiza nombre, apellido y ciudad. */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void actualizar(@PathVariable long id, @Valid @RequestBody ContactoUpdate datos) {
        service.actualizar(id, datos);
    }

    /* DELETE /api/contactos/{id} → elimina el contacto. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long id) {
        service.eliminar(id);
    }
}
