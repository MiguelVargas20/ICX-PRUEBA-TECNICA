package com.icx.prueba.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/* Endpoint simple para saber si el servidor está vivo (útil luego en AWS). */
@RestController
@RequestMapping("/api/estado")
public class EstadoController {

    /* Devuelve un JSON simple; responde aunque Oracle esté caído. */
    @GetMapping
    public Map<String, String> estado() {
        return Map.of("estado", "ok");
    }
}
