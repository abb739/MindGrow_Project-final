package org.example.controller.Frontoffice;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import org.example.entities.Abonnement;
import org.example.entities.Achat;
import org.example.entities.Utilisateur;
import org.example.services.AbonnementService;
import org.example.services.AchatService;
import org.example.utils.PdfGenerator;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AbonnementFrontController {

    @FXML private FlowPane cardsContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private ComboBox<Abonnement> offersComboBox;
    @FXML private ComboBox<String> currencyComboBox;

    private final AbonnementService abonnementService = new AbonnementService();
    private final AchatService achatService = new AchatService();
    private final ObservableList<Abonnement> allAbonnements = FXCollections.observableArrayList();
    private Utilisateur utilisateurConnecte;

    // Taux de conversion fixes (Exemple: 1 TND = X Devise)
    private final Map<String, Double> conversionRates = new HashMap<>();
    private String selectedCurrency = "TND";

    @FXML
    public void initialize() {
        setupSortCombo();
        setupOffersCombo();
        setupCurrencyCombo();
        loadDonnees();
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateDisplay());
        sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateDisplay());
        currencyComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            selectedCurrency = newVal;
            updateDisplay();
        });
    }

    public void setUtilisateur(Utilisateur u) {
        this.utilisateurConnecte = u;
    }

    private void setupSortCombo() {
        sortComboBox.setItems(FXCollections.observableArrayList(
            "Prix : Croissant",
            "Prix : Décroissant",
            "Nom : A-Z"
        ));
    }

    private void setupOffersCombo() {
        offersComboBox.setConverter(new StringConverter<Abonnement>() {
            @Override
            public String toString(Abonnement a) {
                return (a == null) ? "" : a.getNom() + " (" + a.getPrix() + " TND)";
            }
            @Override
            public Abonnement fromString(String string) {
                return null;
            }
        });
    }

    private void setupCurrencyCombo() {
        conversionRates.put("TND", 1.0);
        conversionRates.put("EUR", 0.30);
        conversionRates.put("USD", 0.32);
        conversionRates.put("GBP", 0.25);

        currencyComboBox.setItems(FXCollections.observableArrayList(conversionRates.keySet()));
        currencyComboBox.setValue("TND");
    }

    private void loadDonnees() {
        List<Abonnement> list = abonnementService.afficherAbonnements();
        allAbonnements.setAll(list);
        offersComboBox.setItems(allAbonnements);
        updateDisplay();
    }

    @FXML
    void handleRefreshPrices(ActionEvent event) {
        updateDisplay();
    }

    private void updateDisplay() {
        String searchText = searchField.getText().toLowerCase().trim();
        String sortOption = sortComboBox.getValue();

        List<Abonnement> filteredList = allAbonnements.stream()
            .filter(a -> a.getNom().toLowerCase().contains(searchText))
            .collect(Collectors.toList());

        if (sortOption != null) {
            if (sortOption.equals("Prix : Croissant")) {
                filteredList.sort(Comparator.comparingDouble(Abonnement::getPrix));
            } else if (sortOption.equals("Prix : Décroissant")) {
                filteredList.sort(Comparator.comparingDouble(Abonnement::getPrix).reversed());
            } else if (sortOption.equals("Nom : A-Z")) {
                filteredList.sort(Comparator.comparing(a -> a.getNom().toLowerCase()));
            }
        }

        cardsContainer.getChildren().clear();
        for (Abonnement a : filteredList) {
            cardsContainer.getChildren().add(createAbonnementCard(a));
        }
    }

    @FXML
    void handleSubscribe(ActionEvent event) {
        if (utilisateurConnecte == null) return;
        
        Abonnement selected = offersComboBox.getValue();
        if (selected == null) {
            showAlert("Erreur", "Veuillez choisir une offre dans la liste.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Frontoffice/Payment.fxml"));
            Parent root = loader.load();
            
            PaymentController paymentController = loader.getController();
            paymentController.setAmount(selected.getPrix());
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            if (paymentController.isPaymentSuccess()) {
                Achat current = achatService.getAchatActifParUtilisateur(utilisateurConnecte.getIdUtilisateur());
                if (current != null) {
                    achatService.modifierStatut(current.getIdAchat(), "annulé");
                }

                Achat newAchat = new Achat(selected.getIdAbonnement(), utilisateurConnecte.getIdUtilisateur());
                achatService.ajouterAchat(newAchat);

                Achat generatedAchat = achatService.getAchatActifParUtilisateur(utilisateurConnecte.getIdUtilisateur());
                if (generatedAchat != null) {
                    PdfGenerator.generateSubscriptionPdf(generatedAchat, selected, utilisateurConnecte);
                }

                showAlertInfo("Succès", "Félicitations ! Votre paiement Stripe a été validé. Votre reçu PDF a été généré.");
            } else {
                showAlert("Paiement Annulé", "L'abonnement n'a pas été validé car le paiement a été annulé.");
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger l'interface de paiement.");
        }
    }

    @FXML
    void handleShowMyOffer(ActionEvent event) {
        if (utilisateurConnecte == null) return;

        Achat active = achatService.getAchatActifParUtilisateur(utilisateurConnecte.getIdUtilisateur());
        if (active == null) {
            showAlertInfo("Mon Offre", "Vous n'avez aucun abonnement actif pour le moment.");
            return;
        }

        Abonnement details = null;
        for (Abonnement a : allAbonnements) {
            if (a.getIdAbonnement() == active.getIdAbonnement()) {
                details = a;
                break;
            }
        }

        if (details != null) {
            String msg = "Offre actuelle : " + details.getNom() + "\n" +
                         "Description : " + details.getDescription() + "\n" +
                         "Prix : " + details.getPrix() + " TND\n" +
                         "Durée : " + details.getDureeMois() + " mois\n" +
                         "Date d'achat : " + active.getDateAchat().toString();
            showAlertInfo("Mon Offre Active", msg);
        }
    }

    private VBox createAbonnementCard(Abonnement a) {
        VBox card = new VBox(15);
        card.setPrefSize(260, 320);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; " +
                     "-fx-background-radius: 16; " +
                     "-fx-effect: dropshadow(gaussian, rgba(11,122,143,0.15), 15, 0, 0, 5); " +
                     "-fx-border-color: rgba(0,0,0,0.06); " +
                     "-fx-border-width: 1; " +
                     "-fx-border-radius: 16; " +
                     "-fx-padding: 24;");

        Label badge = new Label(a.getDureeMois() + " MOIS");
        badge.setStyle("-fx-background-color: #e0f5f8; -fx-text-fill: #0b7a8f; -fx-padding: 5 12; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 11px;");

        Label title = new Label(a.getNom());
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.valueOf("#2c3e50"));
        title.setWrapText(true);
        title.setAlignment(Pos.CENTER);

        // Conversion du prix
        double rate = conversionRates.getOrDefault(selectedCurrency, 1.0);
        double convertedPrice = a.getPrix() * rate;

        HBox priceBox = new HBox(2);
        priceBox.setAlignment(Pos.BASELINE_CENTER);
        Label price = new Label(String.format("%.2f", convertedPrice));
        price.setFont(Font.font("System", FontWeight.BOLD, 30));
        price.setTextFill(Color.valueOf("#0b7a8f"));
        Label unit = new Label(" " + selectedCurrency);
        unit.setFont(Font.font("System", FontWeight.NORMAL, 16));
        unit.setTextFill(Color.valueOf("#7f8c8d"));
        priceBox.getChildren().addAll(price, unit);

        Label desc = new Label(a.getDescription());
        desc.setFont(Font.font("System", 13));
        desc.setTextFill(Color.valueOf("#7f8c8d"));
        desc.setWrapText(true);
        desc.setPrefHeight(80);
        desc.setAlignment(Pos.TOP_CENTER);
        desc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        card.getChildren().addAll(badge, title, priceBox, desc);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(11,122,143,0.25), 20, 0, 0, 8); -fx-border-color: rgba(11,122,143,0.18); -fx-border-width: 1; -fx-border-radius: 16; -fx-padding: 24; -fx-translate-y: -5;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(11,122,143,0.15), 15, 0, 0, 5); -fx-border-color: rgba(0,0,0,0.06); -fx-border-width: 1; -fx-border-radius: 16; -fx-padding: 24; -fx-translate-y: 0;"));

        return card;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }

    private void showAlertInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
