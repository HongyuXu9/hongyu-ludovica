package com.finale.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "presenze")
public class Presenza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_cognome", nullable = false)
    private String nomeCognome;

    @Column(nullable = false)
    private String email;

    @Column(name = "n_adulti")
    private int nadulti;

    @Column(name = "n_bambini")
    private int nbambini;

    @Column(nullable = false)
    private boolean alloggio;

    @Column(name = "n_alloggio_adulti")
    private int nalloggioadulti;

    @Column(name = "n_alloggio_bambini")
    private int nalloggiobambini;

    @Column(name = "arrivo")
    private LocalDate arrivo;

    @Column(name = "partenza")
    private LocalDate partenza;

    @Column(length = 1000)
    private String note;
}
