package com.finale.controller;

import com.finale.entity.Presenza;
import com.finale.service.PresenzaService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> salvaPresenza(@RequestBody Presenza presenza) {
        service.salvaEInviaNotifiche(presenza);
        return ResponseEntity.ok("Presenza registrata con successo!");
    }

    @GetMapping
    @Operation(summary = "Recupera tutte le presenze")
    public ResponseEntity<List<Presenza>> tutte() {
        List<Presenza> lista = service.tutte();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recupera una presenza tramite ID")
    public ResponseEntity<Presenza> findById(@PathVariable Long id) {
        Presenza presenza = service.findById(id);

        if (presenza == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(presenza);
    }
}