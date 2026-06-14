package com.finale.service;

import com.finale.entity.Presenza;
import com.finale.repository.PresenzaRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PresenzaService {

    private final PresenzaRepository repository;
    private final JavaMailSender emailSender;

    public PresenzaService(PresenzaRepository repository, JavaMailSender emailSender) {
        this.repository = repository;
        this.emailSender = emailSender;
    }

    public void inviaEmailConferma(Presenza presenza) {
        SimpleMailMessage message = new SimpleMailMessage();

        // Imposta la tua email mittente
        message.setFrom("xupetrella@gmail.com");

        // Invia l'email all'invitato che ha compilato il form
        message.setTo(presenza.getEmail());

        // Manda una Copia Nascosta (BCC)
//        message.setBcc("xupetrella@gmail.com");

        // Oggetto dell'email
        message.setSubject("Conferma presenza matrimonio: Ludovica & Hongyu! ❤️");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Costruzione del testo dell'email
        StringBuilder testo = new StringBuilder();
        testo.append("Ciao ").append(presenza.getNomeCognome()).append(",\n\n")
                .append("Grazie mille! Abbiamo ricevuto la tua conferma per il nostro grande giorno.\n\n")
                .append("Ecco il riepilogo dei dettagli registrati:\n")
                .append("- Adulti: ").append(presenza.getNadulti()).append("\n")
                .append("- Bambini: ").append(presenza.getNbambini()).append("\n");

        if (presenza.isAlloggio()) {
            testo.append("- Richiesta Alloggio: Sì\n")
                    .append("- Persone in alloggio (Adulti): ").append(presenza.getNalloggioadulti()).append("\n")
                    .append("- Persone in alloggio (Bambini): ").append(presenza.getNalloggiobambini()).append("\n");

            if (presenza.getArrivo() != null && presenza.getPartenza() != null) {
                testo.append("- Data Arrivo: ").append(presenza.getArrivo().format(formatter)).append("\n")
                        .append("- Data Partenza: ").append(presenza.getPartenza().format(formatter)).append("\n");
            }
        } else {
            testo.append("- Richiesta Alloggio: No (Autonomo)\n");
        }

        if (presenza.getNote() != null && !presenza.getNote().trim().isEmpty()) {
            testo.append("- Note speciali / Allergie: \"").append(presenza.getNote()).append("\"\n");
        }

        testo.append("\n📍 Ti ricordiamo l'appuntamento:\n")
                .append("Domenica 25 Ottobre 2026 alle ore 11:00\n")
                .append("Presso: A.Roma Lifestyle Hotel (Via Giorgio Zoega, 59, Roma)\n\n")
                .append("Non vediamo l'ora di festeggiare insieme a voi! 🎉\n\n")
                .append("Un grande abbraccio,\n")
                .append("Ludovica & Hongyu ✨");

        message.setText(testo.toString());

        // Spedisce l'email
        emailSender.send(message);
        System.out.println("Email di conferma inviata con successo a: " + presenza.getEmail());
    }

    public void inviaEmailFallimentoNotifica(Presenza presenza, String erroreDettaglio) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("xupetrella@gmail.com");
        message.setTo("xupetrella@gmail.com");
        message.setSubject("⚠️ ATTENZIONE: Invio Email Fallito per " + presenza.getNomeCognome());

        String testo = "Ciao Hongyu & Ludovica,\n\n" +
                "La presenza di " + presenza.getNomeCognome() + " (" + presenza.getEmail() + ") " +
                "è stata SALVATA CORRETTAMENTE nel database, ma il server non è riuscito a inviargli l'email di conferma automatica.\n\n" +
                "Dovrete mandargli una conferma manuale.\n\n" +
                "Dettaglio dell'errore tecnico:\n" + erroreDettaglio;

        message.setText(testo);
        emailSender.send(message);
    }

    public Presenza salvaEInviaNotifiche(Presenza presenza) {
        Presenza presenzaSalvata = repository.save(presenza);
        System.out.println("Presenza salvata con successo sul DB per: " + presenzaSalvata.getNomeCognome());

        try {
            inviaEmailConferma(presenzaSalvata);
            System.out.println("Email inviata correttamente all'invitato: " + presenzaSalvata.getEmail());
        } catch (Exception e) {
            System.err.println("🚨 ERRORE: Invio email fallito per " + presenzaSalvata.getNomeCognome() + ". Errore: " + e.getMessage());

            try {
                inviaEmailFallimentoNotifica(presenzaSalvata, e.toString());
                System.out.println("Email di allerta fallimento recapitata agli sposi.");
            } catch (Exception mailSposiException) {
                System.err.println("❌ Impossibile inviare anche l'email di allerta agli sposi: " + mailSposiException.getMessage());
            }
        }
        return presenzaSalvata;
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