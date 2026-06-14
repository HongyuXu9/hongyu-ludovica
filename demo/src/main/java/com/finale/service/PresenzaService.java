package com.finale.service;

import com.finale.entity.Presenza;
import com.finale.repository.PresenzaRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    public void inviaEmailConBrevo(Presenza presenza) {
        String apiKey = System.getenv("BREVO_API_KEY");

        // Definiamo il mittente
        String mittenteEmail = "xupetrella@gmail.com";
        String mittenteNome = "Ludovica & Hongyu";

        //  Costruzione del corpo HTML
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<p>Ciao <b>").append(presenza.getNomeCognome()).append("</b>,</p>")
                .append("<p>Grazie mille! Abbiamo ricevuto la tua conferma per il nostro grande giorno.</p>")
                .append("<p>Ecco il riepilogo dei dettagli registrati:<br>")
                .append("• Adulti: ").append(presenza.getNadulti()).append("<br>")
                .append("• Bambini: ").append(presenza.getNbambini()).append("<br>");

        if (presenza.isAlloggio()) {
            htmlContent.append("• Richiesta Alloggio: Sì<br>")
                    .append("• Persone in alloggio (Adulti): ").append(presenza.getNalloggioadulti()).append("<br>")
                    .append("• Persone in alloggio (Bambini): ").append(presenza.getNalloggiobambini()).append("<br>");
            if (presenza.getArrivo() != null && presenza.getPartenza() != null) {
                htmlContent.append("• Data Arrivo: ").append(presenza.getArrivo().format(formatter)).append("<br>")
                        .append("• Data Partenza: ").append(presenza.getPartenza().format(formatter)).append("<br>");
            }
        } else {
            htmlContent.append("• Richiesta Alloggio: No (Autonomo)<br>");
        }

        if (presenza.getNote() != null && !presenza.getNote().trim().isEmpty()) {
            htmlContent.append("• Note speciali / Allergie: <i>\"").append(presenza.getNote()).append("\"</i><br>");
        }

        htmlContent.append("</p><p>📍 <b>Ti ricordiamo l'appuntamento:</b><br>")
                .append("Domenica 25 Ottobre 2026 alle ore 11:00<br>")
                .append("Presso: <b>A.Roma Lifestyle Hotel</b> (Via Giorgio Zoega, 59, Roma)</p>")
                .append("<p>Non vediamo l'ora di festeggiare insieme a voi! 🎉</p>")
                .append("<p>Un grande abbraccio,<br>")
                .append("<b>Ludovica & Hongyu</b> ✨</p>");

        // Preparazione Payload JSON
        String safeHtml = htmlContent.toString().replace("\"", "\\\"").replace("\n", " ");
        String jsonPayload = String.format(
                "{\"sender\":{\"name\":\"%s\",\"email\":\"%s\"}," +
                        "\"to\":[{\"email\":\"%s\",\"name\":\"%s\"}]," +
                        "\"subject\":\"Conferma presenza matrimonio: Ludovica & Hongyu! ❤️\"," +
                        "\"htmlContent\":\"%s\"}",
                mittenteNome, mittenteEmail, presenza.getEmail(), presenza.getNomeCognome(), safeHtml
        );

        // Invio tramite HttpClient
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("accept", "application/json")
                    .header("api-key", apiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("✅ Email di conferma inviata con successo da " + mittenteEmail +  " a: " + presenza.getEmail());
            } else {
                throw new RuntimeException("Errore API Brevo: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("❌ Errore durante l'invio dell'email: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void inviaEmailFallimentoNotifica(Presenza presenza, String erroreDettaglio) {
        String apiKey = System.getenv("BREVO_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) return;

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

        // Costruiamo il JSON manuale per Brevo
        String jsonPayload = "{"
                + "\"sender\":{\"name\":\"Sistema Matrimonio\",\"email\":\"xupetrella@gmail.com\"},"
                + "\"to\":[{\"email\":\"xupetrella@gmail.com\",\"name\":\"Hongyu & Ludovica\"}],"
                + "\"subject\":\"⚠️ ATTENZIONE: Invio Email Fallito per " + presenza.getNomeCognome() + "\","
                + "\"htmlContent\":\"" + htmlContenuto.replace("\"", "\\\"").replace("\n", " ") + "\""
                + "}";

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email")) // <--- URL CORRETTO QUI
                    .header("accept", "application/json")
                    .header("api-key", apiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("🚀 Email di allerta fallimento recapitata agli sposi via Brevo.");
            } else {
                System.err.println("❌ Errore Brevo durante l'allerta (Status " + response.statusCode() + "): " + response.body());
            }
        } catch (Exception e) {
            System.err.println("❌ Impossibile inviare l'email di allerta agli sposi: " + e.getMessage());
        }
    }

    public void salvaEInviaNotifiche(Presenza presenza) {
        Presenza presenzaSalvata = repository.save(presenza);

        try {
            inviaEmailConBrevo(presenzaSalvata);
            System.out.println("Email inviata correttamente all'invitato: " + presenzaSalvata.getEmail());
        }catch (Exception e) {
            System.err.println("🚨 ERRORE: Invio email fallito per " + presenzaSalvata.getNomeCognome() + ". Errore: " + e.getMessage());

            try {
                inviaEmailFallimentoNotifica(presenzaSalvata, e.toString());
                System.out.println("Email di allerta fallimento recapitata agli sposi.");
            } catch (Exception mailSposiException) {
                System.err.println("❌ Impossibile inviare anche l'email di allerta agli sposi: " + mailSposiException.getMessage());
            }
        }
    }

    public List<Presenza> tutte() {
        return repository.findAll();
    }

    public Presenza findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presenza non trovata con id: " + id));
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
}