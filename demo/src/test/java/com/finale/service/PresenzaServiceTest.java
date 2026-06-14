package com.finale.service;

import com.finale.entity.Presenza;
import com.finale.repository.PresenzaRepository;
import org.junit.jupiter.api.BeforeEach;
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
        Presenza presenzaSalvata = presenzaTest;
        presenzaSalvata.setId(1L);
        when(presenzaRepository.save(any(Presenza.class))).thenReturn(presenzaSalvata);

        Presenza risultato = presenzaService.salvaEInviaNotifiche(presenzaTest);

        assertNotNull(risultato, "Il risultato non dovrebbe essere null");
        assertEquals(1L, risultato.getId());
        assertEquals("Marco Rossi", risultato.getNomeCognome());

        // Verifichiamo che abbia scritto sul DB una volta
        verify(presenzaRepository, times(1)).save(presenzaTest);

        // Verifichiamo che mailSender abbia inviato l'email di conferma (1 sola chiamata)
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSalvaEInviaNotifiche_fail() {
        Presenza presenzaSalvata = presenzaTest;
        presenzaSalvata.setId(1L);
        when(presenzaRepository.save(any(Presenza.class))).thenReturn(presenzaSalvata);

        // Simuliamo il crash del server mail al primo invio
        doThrow(new RuntimeException("SMTP Connection timeout")).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> {
            Presenza risultato = presenzaService.salvaEInviaNotifiche(presenzaTest);
            assertNotNull(risultato);
            assertEquals(1L, risultato.getId());
        }, "Il servizio non deve lanciare eccezioni se l'email fallisce");

        verify(presenzaRepository, times(1)).save(presenzaTest);

        // IMPORTANTE: mailSender viene chiamato 2 volte!
        // 1a chiamata: tentativo per l'invitato (che fallisce lanciando l'eccezione)
        // 2a chiamata: catch del blocco che invia l'email di allerta a xupetrella@gmail.com
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }
}
