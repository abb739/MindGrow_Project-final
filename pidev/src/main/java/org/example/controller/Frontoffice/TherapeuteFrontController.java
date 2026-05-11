package org.example.controller.Frontoffice;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.example.entities.Avis;
import org.example.entities.Therapeute;
import org.example.entities.Utilisateur;
import org.example.services.AvisService;
import org.example.services.GeminiService;
import org.example.services.TherapeuteService;
import org.example.services.TranslationService;

import org.example.utils.WordFilter;
import org.example.utils.UploadPathResolver;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class TherapeuteFrontController {

    @FXML
    private FlowPane cardsContainer;
    @FXML
    private TextField searchField;
    @FXML
    private HBox specialityFilters;

    private final TherapeuteService therapeuteService = new TherapeuteService();
    private final AvisService avisService = new AvisService();
    private final TranslationService translationService = new TranslationService();
    private final GeminiService geminiService = new GeminiService();
    private final ObservableList<Therapeute> allTherapeutes = FXCollections.observableArrayList();
    private String selectedSpecialite = null;
    private Utilisateur utilisateurConnecte;

    public void setUtilisateur(Utilisateur u) {
        this.utilisateurConnecte = u;
    }

    @FXML
    public void initialize() {
        allTherapeutes.setAll(therapeuteService.afficherTherapeutes());
        buildSpecialityFilters();

        // Add AI Consultant Button
        Button aiBtn = new Button("Conseiller IA 🤖");
        aiBtn.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #0b7a8f 0%, #065f6e 100%); -fx-text-fill: white; -fx-background-radius: 24; -fx-cursor: hand; -fx-padding: 10 22; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(11,122,143,0.25), 10, 0, 0, 3);");
        aiBtn.setOnAction(e -> ouvrirDialogueAIConsultant());
        specialityFilters.getChildren().add(aiBtn);

        updateCards();
        searchField.textProperty().addListener((obs, o, n) -> updateCards());
    }

    private void ouvrirDialogueAIConsultant() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Assistant Orientation MindGrow 🤖");
        dialog.setHeaderText("Décrivez-moi vos besoins ou symptômes (ex: stress, anxiété...)");
        dialog.initOwner(cardsContainer.getScene().getWindow());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setMinWidth(500);

        TextArea inputArea = new TextArea();
        inputArea.setPromptText("Ex: Je me sens très stressé par mon travail en ce moment...");
        inputArea.setPrefRowCount(4);
        inputArea.setWrapText(true);

        Button suggestBtn = new Button("Obtenir une recommandation ✨");
        suggestBtn.setMaxWidth(Double.MAX_VALUE);
        suggestBtn.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #0b7a8f 0%, #065f6e 100%); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");

        TextArea responseArea = new TextArea();
        responseArea.setEditable(false);
        responseArea.setWrapText(true);
        responseArea.setPrefRowCount(8);
        responseArea.setPromptText("La recommandation de l'IA apparaîtra ici...");
        responseArea.setStyle("-fx-control-inner-background: #f8f9fa;");

        ProgressIndicator pi = new ProgressIndicator();
        pi.setVisible(false);
        pi.setMaxSize(30, 30);

        suggestBtn.setOnAction(e -> {
            String input = inputArea.getText().trim();
            if (input.isEmpty())
                return;

            suggestBtn.setDisable(true);
            pi.setVisible(true);
            responseArea.setText("Analyse en cours par l'IA...");

            new Thread(() -> {
                String recommendation = geminiService.generateTherapistRecommendation(input, allTherapeutes);
                javafx.application.Platform.runLater(() -> {
                    responseArea.setText(recommendation);
                    suggestBtn.setDisable(false);
                    pi.setVisible(false);
                });
            }).start();
        });

        HBox btnBox = new HBox(10, suggestBtn, pi);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(new Label("Votre message :"), inputArea, btnBox, new Label("Conseil de l'IA :"),
                responseArea);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void buildSpecialityFilters() {
        Button allBtn = makeChip("Tous", null);
        highlightChip(allBtn);
        specialityFilters.getChildren().add(allBtn);

        allTherapeutes.stream()
                .map(Therapeute::getSpecialite)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .forEach(spec -> specialityFilters.getChildren().add(makeChip(spec, spec)));
    }

    private Button makeChip(String label, String spec) {
        Button btn = new Button(label);
        resetChip(btn);
        btn.setOnAction(e -> {
            selectedSpecialite = spec;
            specialityFilters.getChildren().forEach(c -> {
                if (c instanceof Button b)
                    resetChip(b);
            });
            highlightChip(btn);
            updateCards();
        });
        return btn;
    }

    private void highlightChip(Button b) {
        b.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #0b7a8f 0%, #065f6e 100%); -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 6 18; -fx-font-weight: bold;");
    }

    private void resetChip(Button b) {
        b.setStyle(
                "-fx-background-color: white; -fx-text-fill: #4A4A4A; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 6 18; -fx-border-color: #e2e8f0; -fx-border-radius: 20;");
    }

    private void updateCards() {
        String filter = searchField.getText().toLowerCase().trim();
        List<Therapeute> filtered = allTherapeutes.stream()
                .filter(t -> {
                    boolean matchSearch = t.getNom().toLowerCase().contains(filter)
                            || t.getPrenom().toLowerCase().contains(filter)
                            || (t.getSpecialite() != null && t.getSpecialite().toLowerCase().contains(filter));
                    boolean matchSpec = selectedSpecialite == null || selectedSpecialite.equals(t.getSpecialite());
                    return matchSearch && matchSpec;
                })
                .collect(Collectors.toList());

        cardsContainer.getChildren().clear();
        for (Therapeute t : filtered) {
            cardsContainer.getChildren().add(buildCard(t));
        }
        if (filtered.isEmpty()) {
            Label empty = new Label("Aucun thérapeute trouvé.");
            empty.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 16;");
            cardsContainer.getChildren().add(empty);
        }
    }

    private VBox buildCard(Therapeute t) {
        double moyenne = avisService.getMoyenneNote(t.getIdTherapeute());
        int nbAvis = avisService.getNombreAvis(t.getIdTherapeute());

        VBox card = new VBox(10);
        card.setPrefSize(220, 340);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(11,122,143,0.12), 15, 0, 0, 5); -fx-border-color: #f1f5f9; -fx-border-radius: 16; -fx-border-width: 1;");

        // Avatar
        StackPane avatarPane = buildAvatar(t);

        // Nom
        Label nameLabel = new Label(t.getNom() + "\n" + t.getPrenom());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        nameLabel.setTextFill(Color.valueOf("#2c3e50"));
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setWrapText(true);

        // Spécialité badge
        Label specLabel = new Label(t.getSpecialite() != null ? "🎯 " + t.getSpecialite() : "🎯 -");
        specLabel.setStyle(
                "-fx-background-color: #e0f5f8; -fx-text-fill: #0b7a8f; -fx-padding: 4 12; -fx-background-radius: 16; -fx-font-size: 11; -fx-font-weight: bold;");

        // Contact
        Label emailLabel = new Label(t.getEmail() != null ? "✉ " + t.getEmail() : "");
        emailLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11;");
        emailLabel.setWrapText(true);
        emailLabel.setMaxWidth(200);
        Label telLabel = new Label(t.getTelephone() != null ? "📞 " + t.getTelephone() : "");
        telLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11;");

        // Étoiles + moyenne
        HBox starsBox = buildStarsDisplay(moyenne);
        Label moyenneLabel = new Label(String.format("%.1f/5 (%d avis)", moyenne, nbAvis));
        moyenneLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 10;");

        // Bouton avis
        Button avisBtn = new Button("\u2B50 Donner un avis");
        avisBtn.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #0b7a8f 0%, #065f6e 100%); -fx-text-fill: white; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 12; -fx-padding: 10 14;");
        avisBtn.setMaxWidth(Double.MAX_VALUE);
        avisBtn.setOnAction(e -> ouvrirDialogueAvis(t, card));

        card.getChildren().addAll(avatarPane, nameLabel, specLabel, emailLabel, telLabel, starsBox, moyenneLabel,
                avisBtn);

        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                afficherDetailsAvis(t, card);
            }
        });

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(11,122,143,0.2), 20, 0, 0, 8); -fx-padding: 20; -fx-translate-y: -5; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(11,122,143,0.12), 15, 0, 0, 5); -fx-padding: 20; -fx-translate-y: 0;"));

        return card;
    }

    private void afficherDetailsAvis(Therapeute t, VBox cardRef) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Avis pour " + t.getNom() + " " + t.getPrenom());
        dialog.setHeaderText("Liste des avis et commentaires");

        VBox container = new VBox(15);
        container.setPadding(new Insets(15));
        container.setMinWidth(450);

        List<AvisService.AvisDetail> details = avisService.getAvisDetailsParTherapeute(t.getIdTherapeute());

        if (details.isEmpty()) {
            container.getChildren().add(new Label("Aucun avis pour le moment."));
        } else {
            ScrollPane scrollPane = new ScrollPane();
            VBox reviewsList = new VBox(10);
            reviewsList.setPadding(new Insets(5));

            int currentUserId = utilisateurConnecte != null ? utilisateurConnecte.getIdUtilisateur() : -1;

            for (AvisService.AvisDetail ad : details) {
                VBox reviewItem = new VBox(5);
                reviewItem.setStyle(
                        "-fx-background-color: #f9f9f9; -fx-background-radius: 8; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 1);");

                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);
                Label name = new Label(ad.getNomUtilisateur() + " " + ad.getPrenomUtilisateur());
                name.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                HBox stars = buildStarsDisplay(ad.getNote());
                stars.setScaleX(0.8);
                stars.setScaleY(0.8);

                header.getChildren().addAll(name, stars);

                // Translation Button
                Button translateBtn = new Button("\uD83C\uDF10 Traduire");
                translateBtn.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #9b59b6; -fx-cursor: hand; -fx-font-size: 11; -fx-padding: 0 0 0 10;");
                Label translationLabel = new Label();
                translationLabel.setStyle("-fx-text-fill: #8e44ad; -fx-font-size: 11; -fx-font-style: italic;");
                translationLabel.setVisible(false);
                translationLabel.setManaged(false);
                translationLabel.setWrapText(true);

                translateBtn.setOnAction(ev -> {
                    if (translationLabel.isVisible()) {
                        translationLabel.setVisible(false);
                        translationLabel.setManaged(false);
                        translateBtn.setText("\uD83C\uDF10 Traduire");
                    } else {
                        if (ad.getCommentaire() != null && !ad.getCommentaire().isBlank()) {
                            translateBtn.setText("\u23F3...");
                            new Thread(() -> {
                                String translated = translationService.translate(ad.getCommentaire(), "fr", "en");
                                javafx.application.Platform.runLater(() -> {
                                    translationLabel.setText("🇬🇧 " + translated);
                                    translationLabel.setVisible(true);
                                    translationLabel.setManaged(true);
                                    translateBtn.setText("\uD83C\uDF10 Original");
                                });
                            }).start();
                        }
                    }
                });
                header.getChildren().add(translateBtn);

                // Add Edit/Delete buttons if it's the user's review
                if (ad.getIdUtilisateur() == currentUserId) {
                    Pane spacer = new Pane();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button editBtn = new Button("\u270F");
                    editBtn.setStyle(
                            "-fx-background-color: transparent; -fx-text-fill: #3498db; -fx-cursor: hand; -fx-font-size: 14;");
                    editBtn.setOnAction(ev -> {
                        dialog.close();
                        ouvrirDialogueEditAvis(ad, t, cardRef);
                    });

                    Button deleteBtn = new Button("🗑");
                    deleteBtn.setStyle(
                            "-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand; -fx-font-size: 14;");
                    deleteBtn.setOnAction(ev -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous supprimer votre avis ?",
                                ButtonType.YES, ButtonType.NO);
                        alert.showAndWait().ifPresent(bt -> {
                            if (bt == ButtonType.YES) {
                                avisService.supprimerAvis(ad.getIdAvis());
                                dialog.close();
                                // Refresh the card's stars
                                double newMoyenne = avisService.getMoyenneNote(t.getIdTherapeute());
                                int newNb = avisService.getNombreAvis(t.getIdTherapeute());

                                HBox newStars = buildStarsDisplay(newMoyenne);
                                cardRef.getChildren().set(5, newStars);
                                Label newMoyLbl = (Label) cardRef.getChildren().get(6);
                                newMoyLbl.setText(String.format("%.1f/5 (%d avis)", newMoyenne, newNb));
                            }
                        });
                    });

                    header.getChildren().addAll(spacer, editBtn, deleteBtn);
                }

                Label comment = new Label(
                        ad.getCommentaire() != null && !ad.getCommentaire().isEmpty() ? ad.getCommentaire()
                                : "Pas de commentaire.");
                comment.setWrapText(true);
                comment.setStyle("-fx-text-fill: #34495e; -fx-font-style: italic;");

                Label date = new Label(
                        ad.getDateAvis().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                date.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10;");

                reviewItem.getChildren().addAll(header, comment, translationLabel, date);
                reviewsList.getChildren().add(reviewItem);
            }

            scrollPane.setContent(reviewsList);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(400);
            container.getChildren().add(scrollPane);
        }

        dialog.getDialogPane().setContent(container);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void ouvrirDialogueEditAvis(AvisService.AvisDetail ad, Therapeute t, VBox cardRef) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier votre avis");
        dialog.setHeaderText("Modifiez votre note et commentaire");
        dialog.initOwner(cardsContainer.getScene().getWindow());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setMinWidth(400);
        content.setAlignment(Pos.CENTER_LEFT);

        // Stars
        HBox starsBox = new HBox(10);
        starsBox.setAlignment(Pos.CENTER);
        int[] selectedNote = { ad.getNote() };
        Label[] starsLabels = new Label[5];

        for (int i = 0; i < 5; i++) {
            final int val = i + 1;
            starsLabels[i] = new Label(i < ad.getNote() ? "★" : "☆");
            starsLabels[i].setStyle("-fx-font-size: 36; -fx-text-fill: " + (i < ad.getNote() ? "#f39c12" : "#bdc3c7")
                    + "; -fx-cursor: hand;");

            starsLabels[i].setOnMouseEntered(e -> {
                for (int j = 0; j < 5; j++)
                    starsLabels[j].setStyle("-fx-font-size: 36; -fx-text-fill: " + (j < val ? "#f39c12" : "#bdc3c7")
                            + "; -fx-cursor: hand;");
            });
            starsLabels[i].setOnMouseExited(e -> {
                for (int j = 0; j < 5; j++)
                    starsLabels[j].setStyle("-fx-font-size: 36; -fx-text-fill: "
                            + (j < selectedNote[0] ? "#f39c12" : "#bdc3c7") + "; -fx-cursor: hand;");
            });
            starsLabels[i].setOnMouseClicked(e -> {
                selectedNote[0] = val;
                for (int j = 0; j < 5; j++) {
                    starsLabels[j].setText(j < val ? "★" : "☆");
                    starsLabels[j].setStyle("-fx-font-size: 36; -fx-text-fill: " + (j < val ? "#f39c12" : "#bdc3c7")
                            + "; -fx-cursor: hand;");
                }
            });
            starsBox.getChildren().add(starsLabels[i]);
        }

        TextArea commentArea = new TextArea(ad.getCommentaire());
        commentArea.setPrefRowCount(4);
        commentArea.setWrapText(true);
        commentArea.setPromptText("Votre nouveau commentaire...");
        commentArea.setStyle("-fx-background-radius: 8; -fx-padding: 5;");

        content.getChildren().addAll(new Label("Votre note :"), starsBox, new Label("Votre commentaire :"),
                commentArea);
        dialog.getDialogPane().setContent(content);

        ButtonType modifType = new ButtonType("Enregistrer les modifications", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(modifType, ButtonType.CANCEL);

        // Styling the button
        Node modifBtn = dialog.getDialogPane().lookupButton(modifType);
        modifBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == modifType) {
                String comment = commentArea.getText().trim();
                if (WordFilter.containsBadWords(comment)) {
                    new Alert(Alert.AlertType.WARNING,
                            "Votre commentaire contient des mots inappropriés. Veuillez le modifier.").show();
                    return;
                }
                ad.setNote(selectedNote[0]);
                ad.setCommentaire(comment);
                avisService.modifierAvis(ad);

                // Update card UI
                double newMoyenne = avisService.getMoyenneNote(t.getIdTherapeute());
                int newNb = avisService.getNombreAvis(t.getIdTherapeute());

                HBox newStars = buildStarsDisplay(newMoyenne);
                cardRef.getChildren().set(5, newStars);
                Label newMoyLbl = (Label) cardRef.getChildren().get(6);
                newMoyLbl.setText(String.format("%.1f/5 (%d avis)", newMoyenne, newNb));

                new Alert(Alert.AlertType.INFORMATION, "Avis modifié avec succès !").show();
            }
        });
    }

    private HBox buildStarsDisplay(double moyenne) {
        HBox box = new HBox(2);
        box.setAlignment(Pos.CENTER);
        int full = (int) moyenne;
        for (int i = 1; i <= 5; i++) {
            Label star = new Label(i <= full ? "★" : "☆");
            star.setStyle("-fx-text-fill: " + (i <= full ? "#f39c12" : "#bdc3c7") + "; -fx-font-size: 18;");
            box.getChildren().add(star);
        }
        return box;
    }

    private void ouvrirDialogueAvis(Therapeute t, VBox cardRef) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Avis sur " + t.getNom() + " " + t.getPrenom());
        dialog.setHeaderText("Choisissez une note et laissez un commentaire");

        // Star rating interactive
        HBox starsBox = new HBox(8);
        starsBox.setAlignment(Pos.CENTER);
        int[] selectedNote = { 0 };
        Label[] stars = new Label[5];

        for (int i = 0; i < 5; i++) {
            final int val = i + 1;
            stars[i] = new Label("☆");
            stars[i].setStyle("-fx-font-size: 36; -fx-text-fill: #bdc3c7; -fx-cursor: hand;");

            stars[i].setOnMouseEntered(e -> {
                for (int j = 0; j < 5; j++)
                    stars[j].setStyle("-fx-font-size: 36; -fx-text-fill: " + (j < val ? "#f39c12" : "#bdc3c7")
                            + "; -fx-cursor: hand;");
            });
            stars[i].setOnMouseExited(e -> {
                for (int j = 0; j < 5; j++)
                    stars[j].setStyle("-fx-font-size: 36; -fx-text-fill: "
                            + (j < selectedNote[0] ? "#f39c12" : "#bdc3c7") + "; -fx-cursor: hand;");
            });
            stars[i].setOnMouseClicked(e -> {
                selectedNote[0] = val;
                for (int j = 0; j < 5; j++)
                    stars[j].setText(j < val ? "★" : "☆");
            });
            starsBox.getChildren().add(stars[i]);
        }

        // Note label
        Label noteHint = new Label("Cliquez sur une étoile pour choisir votre note");
        noteHint.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12;");
        noteHint.setAlignment(Pos.CENTER);

        // Commentaire
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Votre commentaire (optionnel)...");
        commentArea.setPrefRowCount(3);
        commentArea.setWrapText(true);
        commentArea.setStyle("-fx-background-radius: 8;");

        VBox content = new VBox(12, starsBox, noteHint, new Label("Commentaire :"), commentArea);
        content.setPadding(new Insets(10));
        content.setAlignment(Pos.CENTER_LEFT);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setMinWidth(380);

        ButtonType envoyerType = new ButtonType("Envoyer \u2B50", ButtonBar.ButtonData.OK_DONE);
        ButtonType annulerType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(envoyerType, annulerType);

        // Style the submit button
        Node envoyerBtn = dialog.getDialogPane().lookupButton(envoyerType);
        envoyerBtn.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #0b7a8f 0%, #065f6e 100%); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");

        dialog.setResultConverter(bt -> {
            if (bt == envoyerType) {
                if (selectedNote[0] == 0) {
                    new Alert(Alert.AlertType.WARNING, "Veuillez choisir une note (1 à 5 étoiles).").show();
                    return null;
                }
                if (utilisateurConnecte == null) {
                    new Alert(Alert.AlertType.WARNING, "Veuillez vous connecter pour laisser un avis.").show();
                    return null;
                }
                int userId = utilisateurConnecte.getIdUtilisateur();
                String comment = commentArea.getText().trim();

                if (WordFilter.containsBadWords(comment)) {
                    new Alert(Alert.AlertType.WARNING,
                            "Votre commentaire contient des mots inappropriés. Veuillez le modifier.").show();
                    return null;
                }

                Avis avis = new Avis(t.getIdTherapeute(), userId, selectedNote[0], comment);
                avisService.ajouterAvis(avis);

                // Refresh the card's stars
                double newMoyenne = avisService.getMoyenneNote(t.getIdTherapeute());
                int newNb = avisService.getNombreAvis(t.getIdTherapeute());

                // Update stars in card (index 5 = starsBox, index 6 = moyenneLabel)
                HBox newStars = buildStarsDisplay(newMoyenne);
                cardRef.getChildren().set(5, newStars);
                Label newMoyLbl = (Label) cardRef.getChildren().get(6);
                newMoyLbl.setText(String.format("%.1f/5 (%d avis)", newMoyenne, newNb));

                new Alert(Alert.AlertType.INFORMATION, "Merci pour votre avis ! \u2B50").show();
            }
            return null;
        });

        dialog.showAndWait();
    }

    private StackPane buildAvatar(Therapeute t) {
        StackPane pane = new StackPane();
        pane.setPrefSize(90, 90);
        if (t.getImage() != null && !t.getImage().isEmpty()) {
            try {
                String resolved = UploadPathResolver.resolve(t.getImage());
                java.io.File imageFile = resolved != null ? new java.io.File(resolved) : null;
                if (imageFile != null && imageFile.exists()) {
                    ImageView imgView = new ImageView(new Image(imageFile.toURI().toString()));
                    imgView.setFitWidth(90);
                    imgView.setFitHeight(90);
                    imgView.setPreserveRatio(true);
                    imgView.setClip(new Circle(45, 45, 45));
                    pane.getChildren().add(imgView);
                    return pane;
                }
            } catch (Exception ignored) {
            }
        }
        pane.setStyle("-fx-background-color: #0b7a8f; -fx-background-radius: 45;");
        String initials = (t.getNom().isEmpty() ? "?" : String.valueOf(t.getNom().charAt(0)).toUpperCase())
                + (t.getPrenom().isEmpty() ? "" : String.valueOf(t.getPrenom().charAt(0)).toUpperCase());
        Label init = new Label(initials);
        init.setFont(Font.font("System", FontWeight.BOLD, 28));
        init.setTextFill(Color.WHITE);
        pane.getChildren().add(init);
        return pane;
    }
}
