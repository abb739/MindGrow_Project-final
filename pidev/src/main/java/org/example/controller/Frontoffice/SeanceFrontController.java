package org.example.controller.Frontoffice;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.example.entities.ReservationSeance;
import org.example.entities.Seance;
import org.example.entities.Utilisateur;
import org.example.services.ReservationSeanceService;
import org.example.services.SeanceService;
import org.example.utils.PdfGenerator;
import org.example.utils.UploadPathResolver;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class SeanceFrontController {

    @FXML
    private FlowPane cardsContainer;

    private final SeanceService seanceService = new SeanceService();
    private final ReservationSeanceService reservationService = new ReservationSeanceService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'à' HH:mm");

    private Utilisateur currentUser;
    private Stage chatbotStage;

    @FXML
    public void initialize() {
        loadSeances();
    }

    @FXML
    void handleOpenChatbot() {
        if (chatbotStage != null && chatbotStage.isShowing()) {
            chatbotStage.toFront();
            return;
        }

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/Frontoffice/Chatbot.fxml"));
            javafx.scene.Parent root = loader.load();

            ChatbotController controller = loader.getController();
            controller.setSeances(seanceService.afficherSeances());

            chatbotStage = new Stage();
            chatbotStage.setTitle("Assistant IA MindGrow - Séances");
            chatbotStage.setScene(new Scene(root));
            chatbotStage.setResizable(false);
            chatbotStage.show();

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void setUtilisateur(Utilisateur user) {
        this.currentUser = user;
        if (user != null) {
            System.out.println("Chargement de l'utilisateur : " + user.getEmail());
            List<ReservationSeance> myRes = reservationService.getReservationsByUtilisateur(user.getIdUtilisateur());
            int activeCount = (int) myRes.stream().filter(r -> !"annulée".equals(r.getStatut())).count();
            System.out.println("Nombre de réservations actives : " + activeCount);
            if (activeCount > 0) {
                org.example.utils.NotificationUtils.showNotification(
                        "Vos Réservations MindGrow",
                        "Vous avez " + activeCount + " réservation(s) active(s) pour les séances. Bon retour !");
            }
        }
    }

    private void loadSeances() {
        cardsContainer.getChildren().clear();
        List<Seance> seances = seanceService.afficherSeances();
        for (Seance s : seances) {
            cardsContainer.getChildren().add(createFlashcard(s));
        }
    }

    private Node createFlashcard(Seance s) {
        VBox card = new VBox(15);
        card.setPrefWidth(280);
        card.setPadding(new Insets(0));
        card.setStyle(
                "-fx-background-color: -fx-card; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(11,122,143,0.12), 15, 0, 0, 5); -fx-border-color: #f1f5f9; -fx-border-radius: 16; -fx-border-width: 1; -fx-cursor: hand;");

        // Image Section
        ImageView iv = new ImageView();
        if (s.getImage() != null && !s.getImage().isEmpty()) {
            try {
                String resolved = UploadPathResolver.resolve(s.getImage());
                java.io.File imageFile = resolved != null ? new java.io.File(resolved) : null;
                if (imageFile != null && imageFile.exists()) {
                    iv.setImage(new Image(imageFile.toURI().toString()));
                }
            } catch (Exception e) {
            }
        }
        iv.setFitWidth(280);
        iv.setFitHeight(180);
        iv.setPreserveRatio(false);
        iv.setStyle("-fx-background-radius: 16 16 0 0;");

        // Content Section
        VBox content = new VBox(8);
        content.setPadding(new Insets(15));

        Label title = new Label(s.getTitre());
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: -fx-text-primary;");
        title.setWrapText(true);

        Label date = new Label("📅 " + s.getDateDebut().format(formatter));
        date.setStyle("-fx-text-fill: -fx-primary; -fx-font-weight: bold;");

        Label lieu = new Label("📍 " + s.getLieu());
        lieu.setStyle("-fx-text-fill: derive(-fx-text-primary, -40%);");
        lieu.setWrapText(true);

        Label cap = new Label("👥 Capacité: " + s.getCapacite() + " personnes");
        cap.setStyle("-fx-text-fill: #27ae60;");

        Label desc = new Label(s.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: -fx-text-primary;");
        desc.setMaxHeight(60);

        Button btnReserve = new Button("🎟 Réserver");
        btnReserve.setMaxWidth(Double.MAX_VALUE);
        btnReserve.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #0b7a8f 0%, #065f6e 100%); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand; -fx-padding: 10 20;");
        btnReserve.setOnAction(e -> handleReserve(s));

        content.getChildren().addAll(title, date, lieu, cap, desc, btnReserve);
        card.getChildren().addAll(iv, content);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: -fx-card; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(11,122,143,0.2), 20, 0, 0, 8); -fx-border-color: rgba(11,122,143,0.2); -fx-border-radius: 16; -fx-border-width: 1; -fx-translate-y: -5;"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: -fx-card; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(11,122,143,0.12), 15, 0, 0, 5); -fx-border-color: #f1f5f9; -fx-border-radius: 16; -fx-border-width: 1; -fx-translate-y: 0;"));

        return card;
    }

    private void handleReserve(Seance s) {
        if (currentUser == null) {
            showAlert("Erreur", "Utilisateur non connecté.");
            return;
        }

        // Vérification de la capacité
        if (s.getCapacite() <= 0) {
            showAlert("Séance Complète", "Désolé, il n'y a plus de places disponibles pour cette séance.");
            return;
        }

        if (reservationService.aDejaReserve(s.getIdSeance(), currentUser.getIdUtilisateur())) {
            showAlert("Info", "Vous avez déjà une réservation pour cette séance.");
            return;
        }
        ReservationSeance res = new ReservationSeance(s, currentUser, "en attente");
        int id = reservationService.ajouterReservation(res);
        if (id != -1) {
            res.setIdReservation(id);
            showAlertSuccess("Succès", "Demande de réservation envoyée ! Un ticket PDF a été généré.");
            PdfGenerator.generateReservationPdf(res);
            loadSeances(); // Rafraîchir les cartes pour voir la nouvelle capacité
        } else {
            showAlert("Erreur", "Une erreur est survenue lors de la réservation.");
        }
    }

    @FXML
    void showCalendar() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/Frontoffice/SeanceCalendar.fxml"));
            VBox root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Calendrier des Séances");
            stage.setScene(new Scene(root, 800, 700));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le calendrier : " + e.getMessage());
        }
    }

    @FXML
    void showMyReservations() {
        if (currentUser == null)
            return;

        List<ReservationSeance> res = reservationService.getReservationsByUtilisateur(currentUser.getIdUtilisateur());

        Stage stage = new Stage();
        stage.setTitle("Mes Réservations");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f4f6f9;");

        Label title = new Label("Mes Réservations");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));

        ScrollPane scroll = new ScrollPane();
        VBox list = new VBox(10);
        list.setPadding(new Insets(10));
        list.setStyle("-fx-background-color: transparent;");

        for (ReservationSeance r : res) {
            HBox item = new HBox(15);
            item.setPadding(new Insets(10));
            item.setStyle(
                    "-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
            item.setAlignment(Pos.CENTER_LEFT);

            Label sName = new Label(r.getSeance().getTitre());
            sName.setStyle("-fx-font-weight: bold;");
            sName.setPrefWidth(150);

            Label sStatut = new Label(r.getStatut());
            sStatut.setTextFill(r.getStatut().equals("annulée") ? Color.RED : Color.GREEN);

            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button btnCancel = new Button("Annuler");
            btnCancel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            btnCancel.setDisable(r.getStatut().equals("annulée"));
            btnCancel.setOnAction(e -> {
                reservationService.annulerReservation(r.getIdReservation());
                stage.close();
                showMyReservations();
                loadSeances(); // Rafraîchir aussi la vue principale
            });

            item.getChildren().addAll(sName, sStatut, spacer, btnCancel);
            list.getChildren().add(item);
        }

        scroll.setContent(list);
        scroll.setFitToWidth(true);
        root.getChildren().addAll(title, scroll);

        Scene scene = new Scene(root, 550, 450);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAlertSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
