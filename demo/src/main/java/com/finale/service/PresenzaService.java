package com.finale.service;

import com.finale.entity.Presenza;
import com.finale.repository.PresenzaRepository;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
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

    public void inviaEmailConResend(Presenza presenza) {
        String apiKey = System.getenv("RESEND_API_KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("🚨 ERRORE: La variabile d'ambiente RESEND_API_KEY non è impostata su Render!");
            return;
        }

        Resend resend = new Resend(apiKey);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<div style='font-family: Arial, sans-serif; font-size: 16px; color: #333; line-height: 1.6;'>")
                .append("<p>Ciao <strong>").append(presenza.getNomeCognome()).append("</strong>,</p>")
                .append("<p>Grazie mille! Abbiamo ricevuto la tua conferma per il nostro grande giorno. ❤️</p>")
                .append("<p><strong>Ecco il riepilogo dei dettagli registrati:</strong></p>")
                .append("<ul>")
                .append("<li><strong>Adulti:</strong> ").append(presenza.getNadulti()).append("</li>")
                .append("<li><strong>Bambini:</strong> ").append(presenza.getNbambini()).append("</li>");

        if (presenza.isAlloggio()) {
            htmlContent.append("<li><strong>Richiesta Alloggio:</strong> Sì</li>")
                    .append("<li><strong>Persone in alloggio (Adulti):</strong> ").append(presenza.getNalloggioadulti()).append("</li>")
                    .append("<li><strong>Persone in alloggio (Bambini):</strong> ").append(presenza.getNalloggiobambini()).append("</li>");

            if (presenza.getArrivo() != null && presenza.getPartenza() != null) {
                htmlContent.append("<li><strong>Data Arrivo:</strong> ").append(presenza.getArrivo().format(formatter)).append("</li>")
                        .append("<li><strong>Data Partenza:</strong> ").append(presenza.getPartenza().format(formatter)).append("</li>");
            }
        } else {
            htmlContent.append("<li><strong>Richiesta Alloggio:</strong> No (Autonomo)</li>");
        }

        if (presenza.getNote() != null && !presenza.getNote().trim().isEmpty()) {
            htmlContent.append("<li><strong>Note speciali / Allergie:</strong> \"").append(presenza.getNote()).append("\"</li>");
        }

        htmlContent.append("</ul>")
                .append("<br/>")
                .append("<div style='background-color: #f9f9f9; padding: 15px; border-radius: 8px; border-left: 4px solid #ff4d4d;'>")
                .append("<h4 style='margin-top: 0;'>📍 Ti ricordiamo l'appuntamento:</h4>")
                .append("<p style='margin-bottom: 5px;'><strong>Domenica 25 Ottobre 2026 alle ore 11:00</strong></p>")
                .append("<p style='margin-top: 0;'>Presso: <em>A.Roma Lifestyle Hotel (Via Giorgio Zoega, 59, Roma)</em></p>")
                .append("</div>")
                .append("<p>Non vediamo l'ora di festeggiare insieme a voi! 🎉</p>")
                .append("<p>Un grande abbraccio,<br/><strong>Ludovica & Hongyu ✨</strong></p>")
                .append("</div>");

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Matrimonio <onboarding@resend.dev>")
                .to(presenza.getEmail()) // Invia all'invitato
                .subject("Conferma presenza matrimonio: Ludovica & Hongyu! ❤️")
                .html(htmlContent.toString())
                .build();

        try {
            // Spedisce la richiesta web a Resend (Non bloccata da Render!)
            CreateEmailResponse data = resend.emails().send(params);
            System.out.println("🚀 Email di conferma inviata con successo via Resend! ID: " + data.getId());
        } catch (ResendException e) {
            System.err.println("🚨 Errore durante l'invio della mail con Resend: " + e.getMessage());
        }
    }

    @Deprecated
    /**
     * deprecato perche render blocca utilizzo di questo formato per evitare spam
     */
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
        String apiKey = System.getenv("RESEND_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) return;

        Resend resend = new Resend(apiKey);

        String htmlContenuto = "<div style='font-family: Arial, sans-serif; color: #333;'>" +
                "<h2>⚠️ ATTENZIONE: Invio Email Fallito</h2>" +
                "<p>Ciao Hongyu & Ludovica,</p>" +
                "<p>La presenza di <strong>" + presenza.getNomeCognome() + "</strong> (" + presenza.getEmail() + ") " +
                "è stata <strong>SALVATA CORRETTAMENTE</strong> nel database, ma il server non è riuscito a inviargli l'email di conferma automatica.</p>" +
                "<p>Dovrete mandargli una conferma manuale o verificare i contatti.</p>" +
                "<br/>" +
                "<div style='background-color: #f5f5f5; padding: 10px; border: 1px solid #ddd; font-family: monospace;'>" +
                "<strong>Dettaglio errore tecnico:</strong><br/>" + erroreDettaglio +
                "</div>" +
                "</div>";

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Sistema Matrimonio <onboarding@resend.dev>")
                .to("xu.hongyu1999@gmail.com")
                .subject("⚠️ ATTENZIONE: Invio Email Fallito per " + presenza.getNomeCognome())
                .html(htmlContenuto)
                .build();

        try {
            resend.emails().send(params);
            System.out.println("Email di allerta fallimento recapitata agli sposi via Resend.");
        } catch (ResendException e) {
            System.err.println("❌ Impossibile inviare l'email di allerta agli sposi via Resend: " + e.getMessage());
        }
    }

    public void salvaEInviaNotifiche(Presenza presenza) {
        Presenza presenzaSalvata = repository.save(presenza);
        System.out.println("Presenza salvata con successo sul DB per: " + presenzaSalvata.getNomeCognome());

        try {
            inviaEmailConResend(presenzaSalvata);
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