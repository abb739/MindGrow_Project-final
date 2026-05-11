package org.example.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.example.entities.Abonnement;
import org.example.entities.ReservationSeance;
import org.example.entities.Utilisateur;
import org.example.entities.Achat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public class PdfGenerator {

    public static void generateSubscriptionPdf(Achat achat, Abonnement abonnement, Utilisateur utilisateur) {
        String dest = "Recu_Abonnement_" + achat.getIdAchat() + ".pdf";
        try {
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Titre
            Paragraph title = new Paragraph("REÇU D'ABONNEMENT")
                    .setFontSize(26)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.CYAN);
            document.add(title);
            document.add(new Paragraph("MindGrow - Votre plateforme préférée").setTextAlignment(TextAlignment.CENTER)
                    .setItalic());

            document.add(new Paragraph("\n"));

            // Détails
            Table table = new Table(UnitValue.createPercentArray(new float[] { 40, 60 }));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addCell(new Paragraph("Numéro d'achat:").setBold());
            table.addCell(new Paragraph(String.valueOf(achat.getIdAchat())));

            table.addCell(new Paragraph("Client:").setBold());
            table.addCell(new Paragraph(utilisateur.getNom() + " " + utilisateur.getPrenom()));

            table.addCell(new Paragraph("Offre:").setBold());
            table.addCell(new Paragraph(abonnement.getNom()));

            table.addCell(new Paragraph("Prix payé:").setBold());
            table.addCell(new Paragraph(abonnement.getPrix() + " TND (via Stripe)"));

            table.addCell(new Paragraph("Durée:").setBold());
            table.addCell(new Paragraph(abonnement.getDureeMois() + " Mois"));

            table.addCell(new Paragraph("Date d'activation:").setBold());
            table.addCell(new Paragraph(achat.getDateAchat().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

            document.add(table);

            document.add(new Paragraph("\n"));

            // QR Code
            String qrContent = "Achat ID: " + achat.getIdAchat() + "\n" +
                    "Client: " + utilisateur.getEmail() + "\n" +
                    "Abonnement: " + abonnement.getNom() + "\n" +
                    "Prix: " + abonnement.getPrix() + " TND";

            byte[] qrCodeImage = generateQrCodeImage(qrContent);
            if (qrCodeImage != null) {
                Image img = new Image(ImageDataFactory.create(qrCodeImage));
                img.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
                document.add(new Paragraph("Vérification numérique").setTextAlignment(TextAlignment.CENTER));
                document.add(img);
            }

            document.close();

            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(new File(dest));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generateReservationPdf(ReservationSeance reservation) {
        String dest = "reservation_ticket_" + reservation.getIdReservation() + ".pdf";
        try {
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Titre
            Paragraph title = new Paragraph("TICKET DE RÉSERVATION")
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.DARK_GRAY);
            document.add(title);

            document.add(new Paragraph("\n"));

            // Détails Séance
            Table table = new Table(UnitValue.createPercentArray(new float[] { 30, 70 }));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addCell(new Paragraph("Séance:").setBold());
            table.addCell(new Paragraph(reservation.getSeance().getTitre()));

            table.addCell(new Paragraph("Lieu:").setBold());
            table.addCell(new Paragraph(reservation.getSeance().getLieu()));

            table.addCell(new Paragraph("Date:").setBold());
            table.addCell(new Paragraph(
                    reservation.getSeance().getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

            table.addCell(new Paragraph("Utilisateur:").setBold());
            table.addCell(new Paragraph(
                    reservation.getUtilisateur().getNom() + " " + reservation.getUtilisateur().getPrenom()));

            table.addCell(new Paragraph("Statut:").setBold());
            table.addCell(new Paragraph(reservation.getStatut()));

            document.add(table);

            document.add(new Paragraph("\n"));

            // QR Code
            String qrContent = "Réservation ID: " + reservation.getIdReservation() + "\n" +
                    "Séance: " + reservation.getSeance().getTitre() + "\n" +
                    "Client: " + reservation.getUtilisateur().getNom() + " " + reservation.getUtilisateur().getPrenom()
                    + "\n" +
                    "Date: " + reservation.getSeance().getDateDebut();

            byte[] qrCodeImage = generateQrCodeImage(qrContent);
            if (qrCodeImage != null) {
                Image img = new Image(ImageDataFactory.create(qrCodeImage));
                img.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
                document.add(new Paragraph("Scannez pour vérifier").setTextAlignment(TextAlignment.CENTER));
                document.add(img);
            }

            document.close();
            System.out.println("PDF créé avec succès : " + dest);

            // Optionnel: Ouvrir le PDF automatiquement
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(new File(dest));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] generateQrCodeImage(String text) {
        try {
            int width = 200;
            int height = 200;
            BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
