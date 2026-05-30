package com.finale.controller;

import com.finale.entity.Presenza;
import com.finale.service.PresenzaService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/presenze")
public class PresenzaController {

    private final PresenzaService service;

    public PresenzaController(PresenzaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Crea una nuova presenza")
    public Presenza crea(@RequestBody Presenza presenza) {
        return service.salva(presenza);
    }

    @GetMapping
    public List<Presenza> tutte() {
        return service.tutte();
    }

    @GetMapping("/{id}")
    public Presenza findById(@PathVariable Long id) {
        return service.findById(id);
    }
}