package org.example.services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    // Configurez ici une adresse e-mail d'envoi (Gmail ou Outlook fonctionne très
    // bien).
    // EXEMPLE TRES IMPORTANT POUR GMAIL : Il faut utiliser un "Mot de passe
    // d'application" et non votre vrai mot de passe.
    // (Google Account -> Security -> 2-Step Verification -> App Passwords).
    private static final String SENDER_EMAIL = "votre.email@gmail.com";
    private static final String SENDER_PASSWORD = "votre_mot_de_passe_application";

    public boolean envoyerEmailReinitialisation(String destinataire, String nouveauMdp) {
        // Configuration SMTP (Ici pour Gmail, à adapter si Outlook: smtp.office365.com,
        // port 587)
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Création de la session
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject("MindGrow - Réinitialisation de votre mot de passe");

            // Corps de l'e-mail avec un peu de HTML
            String corpsHtml = "<h3>Bonjour,</h3>"
                    + "<p>Vous avez demandé la réinitialisation de votre mot de passe sur la plateforme <b>MindGrow</b>.</p>"
                    + "<p>Voici votre mot de passe temporaire : <b style='color:blue;font-size:18px;'>" + nouveauMdp
                    + "</b></p>"
                    + "<p>Nous vous conseillons de vous connecter avec celui-ci puis de le modifier depuis votre espace profil.</p>"
                    + "<br><p>L'équipe MindGrow.</p>";

            message.setContent(corpsHtml, "text/html; charset=utf-8");

            // Envoi du message
            Transport.send(message);
            System.out.println("E-mail envoyé avec succès à " + destinataire);
            return true;

        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'e-mail : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
