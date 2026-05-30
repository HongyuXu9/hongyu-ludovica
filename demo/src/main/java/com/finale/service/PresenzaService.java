package com.finale.service;

import com.finale.entity.Presenza;
import com.finale.repository.PresenzaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PresenzaService {

    private final PresenzaRepository repository;

    public PresenzaService(PresenzaRepository repository) {
        this.repository = repository;
    }

    public Presenza salva(Presenza presenza) {
        return repository.save(presenza);
    }

    public List<Presenza> tutte() {
        return repository.findAll();
    }

    public Presenza findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presenza non trovata con id: " + id));
    }
}