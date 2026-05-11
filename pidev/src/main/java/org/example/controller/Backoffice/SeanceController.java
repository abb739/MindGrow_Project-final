package org.example.controller.Backoffice;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.entities.Seance;
import org.example.services.SeanceService;
import org.example.utils.UploadPathResolver;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SeanceController {

    @FXML
    private TextField titreField;
    @FXML
    private TextField lieuField;
    @FXML
    private TextField capaciteField;
    @FXML
    private DatePicker dateDebutPicker;
    @FXML
    private Label heureDebutLabel;
    @FXML
    private DatePicker dateFinPicker;
    @FXML
    private Label heureFinLabel;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private ImageView imagePreview;
    @FXML
    private FlowPane cardsContainer;
    @FXML
    private Button saveBtn;

    private final SeanceService seanceService = new SeanceService();
    private Seance selectedSeance = null;
    private String selectedImagePath = null;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        chargerDonnees();
    }

    private void chargerDonnees() {
        refreshCards(seanceService.afficherSeances());
    }

    private void refreshCards(List<Seance> seances) {
        cardsContainer.getChildren().clear();
        for (Seance s : seances) {
            cardsContainer.getChildren().add(createSeanceCard(s));
        }
    }

    private javafx.scene.Node createSeanceCard(Seance s) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setPrefWidth(240);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 15, 0, 0, 5); -fx-border-color: #f1f5f9; -fx-border-radius: 20; -fx-border-width: 1; -fx-cursor: hand;");

        ImageView iv = new ImageView();
        if (s.getImage() != null && !s.getImage().isEmpty()) {
            try {
                String resolved = UploadPathResolver.resolve(s.getImage());
                java.io.File imageFile = resolved != null ? new java.io.File(resolved) : null;
                if (imageFile != null && imageFile.exists()) {
                    iv.setImage(new javafx.scene.image.Image(imageFile.toURI().toString()));
                }
            } catch (Exception e) {
                // Image loading failed
            }
        }
        iv.setFitWidth(200);
        iv.setFitHeight(130);
        iv.setPreserveRatio(false);
        iv.setStyle("-fx-background-radius: 15;");

        Label title = new Label(s.getTitre());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #4a4a4a;");

        Label date = new Label("📅 " + s.getDateDebut().format(formatter));
        date.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Label lieu = new Label("📍 " + s.getLieu());
        lieu.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
        lieu.setWrapText(true);
        lieu.setMaxWidth(200);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button btnEdit = new Button("✎ Modifier");
        btnEdit.setStyle(
                "-fx-background-color: white; -fx-text-fill: #4a4a4a; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-cursor: hand; -fx-padding: 8 12;");
        btnEdit.setOnAction(e -> {
            selectedSeance = s;
            remplirFormulaire(s);
        });

        Button btnDelete = new Button("🗑 Supprimer");
        btnDelete.setStyle(
                "-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-background-radius: 10; -fx-border-color: #fee2e2; -fx-border-radius: 10; -fx-cursor: hand; -fx-padding: 8 12;");
        btnDelete.setOnAction(e -> {
            confirmAndAction(() -> {
                seanceService.supprimerSeance(s.getIdSeance());
                chargerDonnees();
                handleClearForm(null);
            });
        });

        actions.getChildren().addAll(btnEdit, btnDelete);
        card.getChildren().addAll(iv, title, date, lieu, actions);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(162, 213, 171, 0.2), 20, 0, 0, 8); -fx-border-color: #a2d5ab; -fx-border-radius: 20; -fx-border-width: 1; -fx-translate-y: -5;"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 15, 0, 0, 5); -fx-border-color: #f1f5f9; -fx-border-radius: 20; -fx-border-width: 1; -fx-translate-y: 0;"));
        return card;
    }

    @FXML
    void handleChooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(cardsContainer.getScene().getWindow());
        if (file != null) {
            selectedImagePath = file.getAbsolutePath();
            try {
                imagePreview.setImage(new javafx.scene.image.Image(file.toURI().toString()));
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de charger l'aperçu de l'image.");
            }
        }
    }

    @FXML
    void handleSortDateASC(ActionEvent event) {
        List<Seance> sortedList = seanceService.afficherSeances().stream()
                .sorted(Comparator.comparing(Seance::getDateDebut))
                .collect(Collectors.toList());
        refreshCards(sortedList);
    }

    @FXML
    void handleSortDateDESC(ActionEvent event) {
        List<Seance> sortedList = seanceService.afficherSeances().stream()
                .sorted(Comparator.comparing(Seance::getDateDebut).reversed())
                .collect(Collectors.toList());
        refreshCards(sortedList);
    }

    @FXML
    void handleShowStatistics(ActionEvent event) {
        Stage statsStage = new Stage();
        statsStage.setTitle("Statistiques de Capacité par Séance");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Séance");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Capacité");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Capacité des Séances");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Participants maximum");

        for (Seance s : seanceService.afficherSeances()) {
            series.getData().add(new XYChart.Data<>(s.getTitre(), s.getCapacite()));
        }

        barChart.getData().add(series);

        Scene scene = new Scene(barChart, 800, 600);
        statsStage.setScene(scene);
        statsStage.show();
    }

    @FXML
    void handleManageReservations(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/Backoffice/ReservationSeanceManage.fxml"));
            VBox root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestion des Réservations");
            stage.setScene(new Scene(root, 1000, 700));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la gestion des réservations.");
        }
    }

    @FXML
    void handleExportCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter la liste des séances");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        File file = fileChooser.showSaveDialog(cardsContainer.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("ID;Titre;Lieu;Capacite;Debut;Fin;Description");
                for (Seance s : seanceService.afficherSeances()) {
                    writer.printf("%d;%s;%s;%d;%s;%s;%s%n",
                            s.getIdSeance(),
                            s.getTitre(),
                            s.getLieu(),
                            s.getCapacite(),
                            s.getDateDebut().format(formatter),
                            s.getDateFin().format(formatter),
                            s.getDescription().replace("\n", " "));
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Exportation");
                alert.setHeaderText(null);
                alert.setContentText("La liste a été exportée avec succès !");
                alert.showAndWait();
            } catch (Exception e) {
                showAlert("Erreur", "Une erreur est survenue lors de l'exportation.");
            }
        }
    }

    @FXML
    void handlePickTimeDebut(ActionEvent event) {
        showClockPicker(heureDebutLabel);
    }

    @FXML
    void handlePickTimeFin(ActionEvent event) {
        showClockPicker(heureFinLabel);
    }

    @FXML
    void handleOpenMap(ActionEvent event) {
        Stage mapStage = new Stage();
        mapStage.setTitle("Sélectionner un lieu sur la carte");

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        String url = getClass().getResource("/map_picker.html").toExternalForm();
        webEngine.load(url);

        // Debug JS errors
        webEngine.setOnAlert(alertEvent -> System.out.println("JS Alert: " + alertEvent.getData()));
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.FAILED) {
                System.err.println("Map loading failed!");
            }
        });

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        Button confirmBtn = new Button("Confirmer la sélection");
        confirmBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        confirmBtn.setOnAction(e -> {
            String location = (String) webEngine.executeScript("getSelectedLocation()");
            if (location != null && !location.isEmpty()) {
                lieuField.setText(location);
                mapStage.close();
            } else {
                showAlert("Avertissement", "Veuillez cliquer sur la carte pour choisir un lieu.");
            }
        });

        layout.getChildren().addAll(webView, confirmBtn);
        VBox.setVgrow(webView, Priority.ALWAYS);

        Scene scene = new Scene(layout, 800, 600);
        mapStage.setScene(scene);
        mapStage.show();
    }

    private void showClockPicker(Label targetLabel) {
        Stage pickerStage = new Stage();
        pickerStage.initStyle(StageStyle.UTILITY);
        pickerStage.setTitle("Sélectionner l'heure");

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffffff;");

        Label selectedTimeLabel = new Label(targetLabel.getText());
        selectedTimeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        StackPane clockFace = new StackPane();
        Circle background = new Circle(120, Color.web("#f1f1f1"));
        clockFace.getChildren().add(background);

        // Ajout des heures
        for (int i = 0; i < 24; i++) {
            final int hour = i;
            Button hourBtn = new Button(String.format("%02d", i));
            double angle = Math.toRadians((i * 15) - 90);
            double radius = (i < 12) ? 90 : 60; // Heures AM à l'extérieur, PM à l'intérieur
            hourBtn.setTranslateX(radius * Math.cos(angle));
            hourBtn.setTranslateY(radius * Math.sin(angle));
            hourBtn.setStyle(
                    "-fx-background-radius: 20; -fx-min-width: 30; -fx-min-height: 30; -fx-font-size: 10px; -fx-background-color: transparent; -fx-cursor: hand;");

            hourBtn.setOnAction(e -> {
                String currentMin = selectedTimeLabel.getText().split(":")[1];
                selectedTimeLabel.setText(String.format("%02d:%s", hour, currentMin));
            });
            clockFace.getChildren().add(hourBtn);
        }

        HBox minuteBox = new HBox(10);
        minuteBox.setAlignment(Pos.CENTER);
        for (int i = 0; i < 60; i += 5) {
            final int min = i;
            Button minBtn = new Button(String.format("%02d", i));
            minBtn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-cursor: hand;");
            minBtn.setOnAction(e -> {
                String currentHour = selectedTimeLabel.getText().split(":")[0];
                selectedTimeLabel.setText(String.format("%s:%02d", currentHour, min));
            });
            minuteBox.getChildren().add(minBtn);
        }

        Button confirmBtn = new Button("Confirmer");
        confirmBtn.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        confirmBtn.setOnAction(e -> {
            targetLabel.setText(selectedTimeLabel.getText());
            pickerStage.close();
        });

        root.getChildren().addAll(selectedTimeLabel, clockFace, new Label("Minutes :"), minuteBox, confirmBtn);

        Scene scene = new Scene(root);
        pickerStage.setScene(scene);
        pickerStage.show();
    }

    @FXML
    void handleSaveSeance(ActionEvent event) {
        try {
            String titre = titreField.getText().trim();
            String lieu = lieuField.getText().trim();
            String desc = descriptionArea.getText().trim();
            String capStr = capaciteField.getText().trim();

            LocalDate dDebut = dateDebutPicker.getValue();
            String hDebut = heureDebutLabel.getText();
            LocalDate dFin = dateFinPicker.getValue();
            String hFin = heureFinLabel.getText();

            if (titre.isEmpty() || lieu.isEmpty() || capStr.isEmpty() || dDebut == null || dFin == null) {
                showAlert("Erreur", "Veuillez remplir tous les champs obligatoires.");
                return;
            }

            int capacite = Integer.parseInt(capStr);
            LocalDateTime start = LocalDateTime.of(dDebut, LocalTime.parse(hDebut));
            LocalDateTime end = LocalDateTime.of(dFin, LocalTime.parse(hFin));

            if (end.isBefore(start)) {
                showAlert("Erreur", "La date de fin doit être après la date de début.");
                return;
            }

            if (selectedSeance == null) {
                seanceService.ajouterSeance(new Seance(titre, desc, lieu, start, end, capacite, selectedImagePath));
            } else {
                selectedSeance.setTitre(titre);
                selectedSeance.setLieu(lieu);
                selectedSeance.setDescription(desc);
                selectedSeance.setCapacite(capacite);
                selectedSeance.setDateDebut(start);
                selectedSeance.setDateFin(end);
                selectedSeance.setImage(selectedImagePath);
                seanceService.modifierSeance(selectedSeance);
            }

            handleClearForm(null);
            chargerDonnees();

        } catch (Exception e) {
            showAlert("Erreur", "Vérifiez le format des données (Heure HH:mm, Capacité Entier).");
            e.printStackTrace();
        }
    }

    @FXML
    void handleClearForm(ActionEvent event) {
        selectedSeance = null;
        selectedImagePath = null;
        titreField.clear();
        lieuField.clear();
        capaciteField.clear();
        dateDebutPicker.setValue(null);
        heureDebutLabel.setText("00:00");
        dateFinPicker.setValue(null);
        heureFinLabel.setText("00:00");
        descriptionArea.clear();
        imagePreview.setImage(null);
        if (saveBtn != null) {
            saveBtn.setText("Ajouter Séance");
        }
    }

    private void remplirFormulaire(Seance s) {
        titreField.setText(s.getTitre());
        lieuField.setText(s.getLieu());
        capaciteField.setText(String.valueOf(s.getCapacite()));
        dateDebutPicker.setValue(s.getDateDebut().toLocalDate());
        heureDebutLabel.setText(s.getDateDebut().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        dateFinPicker.setValue(s.getDateFin().toLocalDate());
        heureFinLabel.setText(s.getDateFin().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        descriptionArea.setText(s.getDescription());
        selectedImagePath = s.getImage();
        if (s.getImage() != null && !s.getImage().isEmpty()) {
            try {
                String resolved = UploadPathResolver.resolve(s.getImage());
                java.io.File imageFile = resolved != null ? new java.io.File(resolved) : null;
                if (imageFile != null && imageFile.exists()) {
                    imagePreview.setImage(new javafx.scene.image.Image(imageFile.toURI().toString()));
                } else {
                    imagePreview.setImage(null);
                }
            } catch (Exception e) {
                imagePreview.setImage(null);
            }
        } else {
            imagePreview.setImage(null);
        }
        if (saveBtn != null) {
            saveBtn.setText("Mettre à jour");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void confirmAndAction(Runnable action) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Suppression de la séance");
        alert.setContentText("Êtes-vous sûr ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            action.run();
        }
    }
}
