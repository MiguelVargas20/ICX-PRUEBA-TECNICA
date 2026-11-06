package com.icx.prueba.controller;

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
import com.icx.prueba.dto.ResultadoBusqueda;
import com.icx.prueba.service.ContactoService;

import jakarta.validation.Valid;

// Endpoints que usa el frontend. Todos empiezan por /api/contactos.
// El controller solo recibe la petición y se la pasa al service,
// aquí no va lógica.
@RestController
@RequestMapping("/api/contactos")
public class ContactoController {

    private final ContactoService service;

    public ContactoController(ContactoService service) {
        this.service = service;
    }

    // GET /api/contactos?ciudad=&nombre=&correo=&telefono=
    // Los filtros son opcionales: si no llegan quedan en null.
    // Responde la lista y si viene de Oracle o del respaldo.
    @GetMapping
    public ResultadoBusqueda buscar(
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String telefono) {
        return service.buscar(ciudad, nombre, correo, telefono);
    }

    // POST /api/contactos -> crea un contacto.
    // @RequestBody convierte el JSON en ContactoRequest y @Valid revisa sus reglas.
    // Si algo está mal, no entra al método y responde el ApiExceptionHandler.
    @PostMapping
    public ResponseEntity<ContactoDto> crear(@Valid @RequestBody ContactoRequest datos) {
        // 201 = creado
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(datos));
    }

    // PUT /api/contactos/{id} -> edita nombre, apellido, ciudad, correo y teléfono.
    // Responde 204: salió bien pero no devuelve nada.
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void actualizar(@PathVariable long id, @Valid @RequestBody ContactoUpdate datos) {
        service.actualizar(id, datos);
    }

    // DELETE /api/contactos/{id} -> elimina el contacto. También responde 204.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long id) {
        service.eliminar(id);
    }
}
