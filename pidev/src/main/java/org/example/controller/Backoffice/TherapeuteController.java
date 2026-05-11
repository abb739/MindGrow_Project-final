package org.example.controller.Backoffice;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.entities.Therapeute;
import org.example.services.TherapeuteService;
import org.example.utils.UploadPathResolver;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class TherapeuteController {

    @FXML
    private FlowPane cardsContainer;
    @FXML
    private TextField searchField;
    @FXML
    private VBox formPane;
    @FXML
    private Label formTitle;
    @FXML
    private Label errorLabel;
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField specialiteField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField telephoneField;
    @FXML
    private TextField imageField;
    @FXML
    private TextField certificatField;

    private final TherapeuteService therapeuteService = new TherapeuteService();
    private final ObservableList<Therapeute> therapeutesList = FXCollections.observableArrayList();
    private Therapeute therapeuteSelectionne = null;
    private boolean sortAsc = true;

    @FXML
    public void initialize() {
        loadDonnees();
        searchField.textProperty().addListener((obs, o, n) -> updateCards());
    }

    private void loadDonnees() {
        therapeutesList.setAll(therapeuteService.afficherTherapeutes());
        updateCards();
    }

    private void updateCards() {
        String filter = searchField.getText().toLowerCase().trim();

        List<Therapeute> filtered = therapeutesList.stream()
                .filter(t -> t.getNom().toLowerCase().contains(filter)
                        || t.getPrenom().toLowerCase().contains(filter)
                        || (t.getSpecialite() != null && t.getSpecialite().toLowerCase().contains(filter))
                        || (t.getEmail() != null && t.getEmail().toLowerCase().contains(filter)))
                .collect(Collectors.toList());

        // Apply current sort
        filtered.sort((a, b) -> {
            if (a.getDateInscription() == null)
                return 1;
            if (b.getDateInscription() == null)
                return -1;
            int cmp = a.getDateInscription().compareTo(b.getDateInscription());
            return sortAsc ? cmp : -cmp;
        });

        cardsContainer.getChildren().clear();
        for (Therapeute t : filtered) {
            cardsContainer.getChildren().add(buildCard(t));
        }
    }

    private VBox buildCard(Therapeute t) {
        VBox card = new VBox(10);
        card.setPrefSize(220, 320);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 15, 0, 0, 5); -fx-padding: 20; -fx-border-color: #f1f5f9; -fx-border-radius: 20; -fx-border-width: 1;");

        // Image du thérapeute
        ImageView imgView = new ImageView();
        imgView.setFitWidth(80);
        imgView.setFitHeight(80);
        imgView.setPreserveRatio(true);
        if (t.getImage() != null && !t.getImage().isEmpty()) {
            try {
                String resolved = UploadPathResolver.resolve(t.getImage());
                java.io.File imageFile = resolved != null ? new java.io.File(resolved) : null;
                if (imageFile != null && imageFile.exists()) {
                    imgView.setImage(new Image(imageFile.toURI().toString()));
                }
            } catch (Exception e) {
                imgView.setStyle("-fx-background-color: #bdc3c7;");
            }
        }
        StackPane imgContainer = new StackPane(imgView);
        imgContainer.setStyle(
                "-fx-background-color: #f8fafc; -fx-background-radius: 50; -fx-border-color: #e2e8f0; -fx-border-radius: 50; -fx-border-width: 1;");
        imgContainer.setPrefSize(100, 100);

        // Nom + Prénom
        Label nameLabel = new Label(t.getNom() + " " + t.getPrenom());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.valueOf("#4a4a4a"));
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);

        // Spécialité
        Label specLabel = new Label(t.getSpecialite() != null ? t.getSpecialite() : "-");
        specLabel.setStyle(
                "-fx-background-color: #f0fdf4; -fx-text-fill: #166534; -fx-padding: 5 12; -fx-background-radius: 10; -fx-font-size: 12; -fx-font-weight: bold; -fx-border-color: #dcfce7; -fx-border-radius: 10;");

        // Date
        Label dateLabel = new Label();
        if (t.getDateInscription() != null)
            dateLabel.setText("📅 " + t.getDateInscription().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11;");

        // Boutons action
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);

        Button editBtn = new Button("✏️");
        editBtn.setStyle(
                "-fx-background-color: #ffffff; -fx-text-fill: #4a4a4a; -fx-cursor: hand; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-padding: 8 12;");
        editBtn.setOnAction(e -> openFormForEdit(t));

        Button delBtn = new Button("🗑️");
        delBtn.setStyle(
                "-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-cursor: hand; -fx-background-radius: 10; -fx-border-color: #fee2e2; -fx-border-radius: 10; -fx-padding: 8 12;");
        delBtn.setOnAction(e -> confirmerSuppression(t));

        actions.getChildren().addAll(editBtn, delBtn);

        // Bouton certificat PDF
        if (t.getCertificat() != null && !t.getCertificat().isEmpty()) {
            Button certBtn = new Button("📄 Certificat");
            certBtn.setStyle(
                    "-fx-background-color: #f8fafc; -fx-text-fill: #4a4a4a; -fx-cursor: hand; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-padding: 5 10; -fx-font-size: 11;");
            certBtn.setOnAction(e -> ouvrirCertificat(t.getCertificat()));
            card.getChildren().add(certBtn);
        }

        card.getChildren().addAll(imgContainer, nameLabel, specLabel, dateLabel, actions);

        // Hover
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(162, 213, 171, 0.2), 20, 0, 0, 8); -fx-padding: 20; -fx-border-color: #a2d5ab; -fx-border-radius: 20; -fx-border-width: 1; -fx-translate-y: -5;"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 15, 0, 0, 5); -fx-padding: 20; -fx-border-color: #f1f5f9; -fx-border-radius: 20; -fx-border-width: 1; -fx-translate-y: 0;"));

        return card;
    }

    private void ouvrirCertificat(String path) {
        try {
            File file = new File(path);
            if (file.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                new Alert(Alert.AlertType.WARNING, "Fichier introuvable : " + path).show();
            }
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir le fichier.").show();
        }
    }

    @FXML
    void handleSortAsc(ActionEvent event) {
        sortAsc = true;
        updateCards();
    }

    @FXML
    void handleSortDesc(ActionEvent event) {
        sortAsc = false;
        updateCards();
    }

    @FXML
    void handleExportCsv(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        chooser.setInitialFileName("therapeutes.csv");
        File file = chooser.showSaveDialog(null);
        if (file == null)
            return;

        try (FileWriter fw = new FileWriter(file)) {
            fw.write("ID,Nom,Prénom,Spécialité,Email,Téléphone,Date Inscription\n");
            for (Therapeute t : therapeutesList) {
                fw.write(String.format("%d,%s,%s,%s,%s,%s,%s\n",
                        t.getIdTherapeute(),
                        esc(t.getNom()), esc(t.getPrenom()), esc(t.getSpecialite()),
                        esc(t.getEmail()), esc(t.getTelephone()),
                        t.getDateInscription() != null
                                ? t.getDateInscription().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                                : ""));
            }
            new Alert(Alert.AlertType.INFORMATION, "Export CSV réussi : " + file.getName()).show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'export : " + e.getMessage()).show();
        }
    }

    private String esc(String s) {
        if (s == null)
            return "";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    @FXML
    void handleShowStats(ActionEvent event) {
        Map<String, Long> specialiteCount = therapeutesList.stream()
                .collect(Collectors.groupingBy(
                        t -> (t.getSpecialite() != null && !t.getSpecialite().isEmpty()) ? t.getSpecialite()
                                : "Non définie",
                        Collectors.counting()));

        PieChart chart = new PieChart();
        chart.setTitle("Répartition par Spécialité");
        specialiteCount
                .forEach((spec, count) -> chart.getData().add(new PieChart.Data(spec + " (" + count + ")", count)));

        Stage stage = new Stage();
        stage.setTitle("Statistiques Thérapeutes");
        stage.setScene(new Scene(chart, 550, 450));
        stage.show();
    }

    @FXML
    void handleAdd(ActionEvent event) {
        therapeuteSelectionne = null;
        clearForm();
        formTitle.setText("Nouveau Thérapeute");
        showForm(true);
    }

    @FXML
    void handleSave(ActionEvent event) {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        if (nom.isEmpty() || prenom.isEmpty()) {
            errorLabel.setText("Nom et Prénom sont obligatoires.");
            return;
        }
        errorLabel.setText("");

        String specialite = specialiteField.getText().trim();
        String email = emailField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String image = imageField.getText().trim();
        String certificat = certificatField.getText().trim();

        if (therapeuteSelectionne == null) {
            Therapeute t = new Therapeute(nom, prenom,
                    image.isEmpty() ? null : image, certificat.isEmpty() ? null : certificat,
                    specialite.isEmpty() ? null : specialite, email.isEmpty() ? null : email,
                    telephone.isEmpty() ? null : telephone);
            therapeuteService.ajouterTherapeute(t);
        } else {
            therapeuteSelectionne.setNom(nom);
            therapeuteSelectionne.setPrenom(prenom);
            therapeuteSelectionne.setSpecialite(specialite.isEmpty() ? null : specialite);
            therapeuteSelectionne.setEmail(email.isEmpty() ? null : email);
            therapeuteSelectionne.setTelephone(telephone.isEmpty() ? null : telephone);
            therapeuteSelectionne.setImage(image.isEmpty() ? null : image);
            therapeuteSelectionne.setCertificat(certificat.isEmpty() ? null : certificat);
            therapeuteService.modifierTherapeute(therapeuteSelectionne);
        }
        loadDonnees();
        showForm(false);
        clearForm();
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        loadDonnees();
    }

    @FXML
    void handleCancelForm(ActionEvent event) {
        showForm(false);
        clearForm();
    }

    @FXML
    void handleChooseImage(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(null);
        if (file != null)
            imageField.setText(file.getAbsolutePath());
    }

    @FXML
    void handleChooseCertificat(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.png", "*.jpg"));
        File file = chooser.showOpenDialog(null);
        if (file != null)
            certificatField.setText(file.getAbsolutePath());
    }

    private void openFormForEdit(Therapeute t) {
        therapeuteSelectionne = t;
        formTitle.setText("Modifier le Thérapeute");
        nomField.setText(t.getNom());
        prenomField.setText(t.getPrenom());
        specialiteField.setText(t.getSpecialite() != null ? t.getSpecialite() : "");
        emailField.setText(t.getEmail() != null ? t.getEmail() : "");
        telephoneField.setText(t.getTelephone() != null ? t.getTelephone() : "");
        imageField.setText(t.getImage() != null ? t.getImage() : "");
        certificatField.setText(t.getCertificat() != null ? t.getCertificat() : "");
        showForm(true);
    }

    private void confirmerSuppression(Therapeute t) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + t.getNom() + " " + t.getPrenom() + " ?",
                ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                therapeuteService.supprimerTherapeute(t.getIdTherapeute());
                loadDonnees();
            }
        });
    }

    private void showForm(boolean visible) {
        formPane.setVisible(visible);
        formPane.setManaged(visible);
    }

    private void clearForm() {
        nomField.clear();
        prenomField.clear();
        specialiteField.clear();
        emailField.clear();
        telephoneField.clear();
        imageField.clear();
        certificatField.clear();
        errorLabel.setText("");
        therapeuteSelectionne = null;
    }
}
