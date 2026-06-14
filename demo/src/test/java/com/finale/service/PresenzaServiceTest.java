package com.finale.service;

import com.finale.entity.Presenza;
import com.finale.repository.PresenzaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PresenzaServiceTest {
    @Mock
    private PresenzaRepository presenzaRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private PresenzaService presenzaService;

    private Presenza presenzaTest;

    @BeforeEach
    void setUp() {
        presenzaTest = new Presenza();
        presenzaTest.setNomeCognome("Marco Rossi");
        presenzaTest.setEmail("marco.rossi@example.com");
        presenzaTest.setNadulti(2);
        presenzaTest.setNbambini(0);
        presenzaTest.setAlloggio(true);
        presenzaTest.setArrivo(LocalDate.parse("2026-10-24"));
        presenzaTest.setPartenza(LocalDate.of(2026, 10, 26));
        presenzaTest.setNote("Nessuna allergia");
    }

    @Test
    void testSalvaEInviaNotifiche_success() {
        when(presenzaRepository.save(any(Presenza.class))).thenReturn(presenzaTest);
        presenzaService.salvaEInviaNotifiche(presenzaTest);

        verify(presenzaRepository, times(1)).save(presenzaTest);
    }
}
