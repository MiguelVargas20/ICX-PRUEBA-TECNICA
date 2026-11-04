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

/* Controlador para gestionar las operaciones relacionadas con los contactos. */
/*
 * @RestController significa dos cosas:
 *  1. Esta clase recibe peticiones HTTP.
 *  2. Los objetos que devuelve se convierten automáticamente a JSON.
 */
@RestController
/*
 * @RequestMapping coloca la parte común de todas las rutas delante.
 * Así, crear() puede responder en /api/contactos y buscar() también.
 */
@RequestMapping("/api/contactos")
public class ContactoController {

    /* Servicio para gestionar las operaciones relacionadas con los contactos. */
    private final ContactoService service;

    /* Constructor para inicializar el servicio de contactos. */
    public ContactoController(ContactoService service) {
        this.service = service;
    }

    /*
     * GET /api/contactos?ciudad=&nombre=&correo=&telefono=
     * → { contactos: [...], desdeRespaldo, fechaRespaldo, filtrosRespaldo }
     */
    /*
     * @GetMapping sin texto extra usa la ruta base configurada arriba:
     * /api/contactos.
     *
     * @RequestParam(required = false) indica que cada filtro puede llegar o no.
     * Un filtro que no llega queda en Java como null.
     */
    @GetMapping
    public ResultadoBusqueda buscar(
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String telefono) {
        // El controlador no construye la consulta: delega toda la lógica al servicio.
        return service.buscar(ciudad, nombre, correo, telefono);
    }

    /* POST /api/contactos → crea un contacto. @Valid revisa las reglas del ContactoRequest. */
    /*
     * POST representa una operación de creación.
     *
     * @RequestBody toma el JSON enviado por el frontend y lo convierte en
     * un objeto ContactoRequest.
     *
     * @Valid revisa las anotaciones del DTO antes de llamar al servicio.
     * Si hay un campo inválido, Spring detiene la operación y el
     * ApiExceptionHandler devuelve el mensaje de error.
     */
    @PostMapping
    public ResponseEntity<ContactoDto> crear(@Valid @RequestBody ContactoRequest datos) {
        // HttpStatus.CREATED indica 201: recurso creado correctamente.
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(datos));
    }

    /* PUT /api/contactos/{id} → actualiza nombre, apellido y ciudad. */
    /*
     * @PathVariable toma el valor que aparece en la URL.
     * Ejemplo: en /api/contactos/123, el parámetro id recibe 123 como long.
     *
     * @ResponseStatus(NO_CONTENT) cambia la respuesta exitosa a 204.
     * Esto significa que la operación salió bien, pero no devuelve un cuerpo JSON.
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void actualizar(@PathVariable long id, @Valid @RequestBody ContactoUpdate datos) {
        service.actualizar(id, datos);
    }

    /* DELETE /api/contactos/{id} → elimina el contacto. */
    /*
     * El controlador pasa solamente el identificador al servicio.
     * El servicio decide cómo comunicar la eliminación a Oracle.
     *
     * Una respuesta 204 indica éxito sin devolver información adicional.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long id) {
        service.eliminar(id);
    }
}
