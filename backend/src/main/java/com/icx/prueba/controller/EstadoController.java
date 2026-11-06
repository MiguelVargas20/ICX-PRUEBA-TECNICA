package com.icx.prueba.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Endpoint para revisar si el backend está prendido.
// Responde aunque Oracle esté caído porque no lo llama.
@RestController
@RequestMapping("/api/estado")
public class EstadoController {

    @GetMapping
    public Map<String, String> estado() {
        return Map.of("estado", "ok");
    }
}
