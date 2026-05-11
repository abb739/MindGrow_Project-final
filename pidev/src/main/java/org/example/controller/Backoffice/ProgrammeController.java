package org.example.controller.Backoffice;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Scene;
import org.example.entities.Categorie;
import org.example.entities.Programme;
import org.example.services.CategorieService;
import org.example.services.ProgrammeService;
import org.example.utils.UploadPathResolver;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class ProgrammeController {

    // Éléments Catégories
    @FXML
    private TextField catNomField;
    @FXML
    private TextArea catDescArea;
    @FXML
    private TableView<Categorie> catTable;
    @FXML
    private TableColumn<Categorie, Integer> colCatId;
    @FXML
    private TableColumn<Categorie, String> colCatNom;
    @FXML
    private TableColumn<Categorie, String> colCatDesc;
    @FXML
    private TableColumn<Categorie, Void> colCatAction;

    // Éléments Programmes
    @FXML
    private ComboBox<Categorie> catCombo;
    @FXML
    private TextField progTitreField;
    @FXML
    private TextArea progDescArea;
    @FXML
    private TextField progImageField;
    @FXML
    private TextField progVideoField;
    @FXML
    private FlowPane progCardsContainer;
    @FXML
    private Button progSaveBtn;

    private final CategorieService categorieService = new CategorieService();
    private final ProgrammeService programmeService = new ProgrammeService();

    private Categorie selectedCategorie = null;
    private Programme selectedProgramme = null;

    @FXML
    public void initialize() {
        configurerTables();
        chargerDonnees();

        // Sélection dans les tables pour modification
        catTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                selectedCategorie = newV;
                catNomField.setText(newV.getNom());
                catDescArea.setText(newV.getDescription());
            }
        });

        setupDynamicValidation();
    }

    private void setupDynamicValidation() {
        // Validation Catégorie Nom : Lettres seulement
        catNomField.textProperty().addListener((obs, oldV, newV) -> {
            if (!newV.matches("[a-zA-Z\\s]*")) {
                catNomField.setText(oldV);
                catNomField.setStyle("-fx-border-color: red;");
            } else {
                catNomField.setStyle("");
            }
        });

        // Validation Catégorie Description : Max 20 caractères
        catDescArea.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.length() > 20) {
                catDescArea.setText(oldV);
                catDescArea.setStyle("-fx-border-color: red;");
            } else {
                catDescArea.setStyle("");
            }
        });

        // Validation Programme Titre : Lettres seulement
        progTitreField.textProperty().addListener((obs, oldV, newV) -> {
            if (!newV.matches("[a-zA-Z0-9\\s]*")) { // On autorise les chiffres pour les titres de programmes
                progTitreField.setText(oldV);
                progTitreField.setStyle("-fx-border-color: red;");
            } else {
                progTitreField.setStyle("");
            }
        });

        // Validation Programme Description : Max 20 caractères
        progDescArea.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.length() > 20) {
                progDescArea.setText(oldV);
                progDescArea.setStyle("-fx-border-color: red;");
            } else {
                progDescArea.setStyle("");
            }
        });
    }

    private void configurerTables() {
        // Catégories
        colCatId.setCellValueFactory(new PropertyValueFactory<>("idCategorie"));
        colCatNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCatDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        ajouterBoutonSuppressionCat();
    }

    private void chargerDonnees() {
        catTable.setItems(FXCollections.observableArrayList(categorieService.afficherCategories()));
        catCombo.setItems(FXCollections.observableArrayList(categorieService.afficherCategories()));
        refreshProgrammesCards();
    }

    private void refreshProgrammesCards() {
        progCardsContainer.getChildren().clear();
        List<Programme> programmes = programmeService.afficherProgrammes();
        for (Programme p : programmes) {
            progCardsContainer.getChildren().add(createProgrammeCard(p));
        }
    }

    private VBox createProgrammeCard(Programme p) {
        VBox card = new VBox(10);
        card.setPrefWidth(220);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 15, 0, 0, 5); -fx-border-color: #f1f5f9; -fx-border-radius: 20; -fx-border-width: 1; -fx-cursor: hand;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(120);
        imageView.setStyle("-fx-background-radius: 15;");
        try {
            if (p.getImage() != null && !p.getImage().isEmpty()) {
                String resolved = UploadPathResolver.resolve(p.getImage());
                java.io.File imageFile = resolved != null ? new java.io.File(resolved) : null;
                if (imageFile != null && imageFile.exists()) {
                    imageView.setImage(new Image(imageFile.toURI().toString()));
                } else {
                    imageView.setImage(new Image("/images/placeholder.png"));
                }
            } else {
                imageView.setImage(new Image("/images/placeholder.png")); // S'assurer qu'un placeholder existe ou
                                                                          // mettre à jour
            }
        } catch (Exception e) {
            // Placeholder fallback
        }

        Label titleLabel = new Label(p.getTitre());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #4a4a4a;");
        titleLabel.setWrapText(true);

        Label catLabel = new Label(p.getCategorie().getNom());
        catLabel.setStyle(
                "-fx-background-color: #f0fdf4; -fx-text-fill: #166534; -fx-padding: 4 10; -fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: bold;");

        HBox actions = new HBox(10);
        Button editBtn = new Button("✏️ Modifier");
        Button deleteBtn = new Button("🗑️ Supprimer");
        editBtn.setStyle(
                "-fx-background-color: #ffffff; -fx-text-fill: #4a4a4a; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-cursor: hand; -fx-padding: 8 15;");
        deleteBtn.setStyle(
                "-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-background-radius: 10; -fx-border-color: #fee2e2; -fx-border-radius: 10; -fx-cursor: hand; -fx-padding: 8 15;");

        editBtn.setOnAction(e -> {
            selectedProgramme = p;
            progTitreField.setText(p.getTitre());
            progDescArea.setText(p.getDescription());
            progImageField.setText(p.getImage());
            progVideoField.setText(p.getVideo());
            catCombo.setValue(p.getCategorie());
            progSaveBtn.setText("Mettre à jour");
        });

        deleteBtn.setOnAction(e -> confirmAndAction(() -> {
            programmeService.supprimerProgramme(p.getIdProgramme());
            chargerDonnees();
        }));

        actions.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().addAll(imageView, titleLabel, catLabel, actions);

        // Clic sur la carte pour voir les détails
        card.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 || event.getTarget() instanceof VBox || event.getTarget() instanceof Label
                    || event.getTarget() instanceof ImageView) {
                showProgrammeDetails(p);
            }
        });

        return card;
    }

    private void showProgrammeDetails(Programme p) {
        Stage detailStage = new Stage();
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 40; -fx-background-color: #ffffff;");
        root.setPrefWidth(650);

        Label title = new Label(p.getTitre());
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label cat = new Label("Catégorie : " + p.getCategorie().getNom());
        cat.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

        TextArea desc = new TextArea(p.getDescription());
        desc.setEditable(false);
        desc.setWrapText(true);
        desc.setPrefHeight(120);
        desc.setStyle(
                "-fx-background-radius: 15; -fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 15; -fx-padding: 10;");

        root.getChildren().addAll(title, cat, desc);

        if (p.getVideo() != null && !p.getVideo().isEmpty()) {
            try {
                File videoFile = new File(p.getVideo());
                if (videoFile.exists()) {
                    Media media = new Media(videoFile.toURI().toString());
                    MediaPlayer mediaPlayer = new MediaPlayer(media);

                    // Gestion des erreurs média
                    mediaPlayer.setOnError(() -> {
                        System.err.println("Erreur MediaPlayer : " + mediaPlayer.getError().getMessage());
                    });

                    MediaView mediaView = new MediaView(mediaPlayer);
                    mediaView.setFitWidth(540);
                    mediaView.setPreserveRatio(true);

                    // Conteneur pour la vidéo avec fond noir
                    StackPane videoContainer = new StackPane(mediaView);
                    videoContainer.setStyle("-fx-background-color: black; -fx-background-radius: 5;");
                    videoContainer.setPrefHeight(300);

                    Button playBtn = new Button("▶ Lecture");
                    Button pauseBtn = new Button("⏸ Pause");
                    HBox videoControls = new HBox(15, playBtn, pauseBtn);
                    videoControls.setStyle("-fx-alignment: center; -fx-padding: 15;");

                    playBtn.setStyle(
                            "-fx-background-color: #a2d5ab; -fx-text-fill: #4a4a4a; -fx-font-weight: bold; -fx-background-radius: 12; -fx-cursor: hand; -fx-padding: 8 20;");
                    pauseBtn.setStyle(
                            "-fx-background-color: #f8fafc; -fx-text-fill: #4a4a4a; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-cursor: hand; -fx-padding: 8 20;");

                    playBtn.setOnAction(e -> mediaPlayer.play());
                    pauseBtn.setOnAction(e -> mediaPlayer.pause());

                    root.getChildren().addAll(new Separator(), new Label("Vidéo de présentation :"), videoContainer,
                            videoControls);

                    detailStage.setOnCloseRequest(e -> mediaPlayer.stop());
                } else {
                    Label errorLabel = new Label("⚠️ Fichier vidéo introuvable à l'emplacement :\n" + p.getVideo());
                    errorLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-style: italic;");
                    root.getChildren().addAll(new Separator(), errorLabel);
                }
            } catch (Exception e) {
                root.getChildren().add(new Label("Erreur de lecture vidéo : " + e.getMessage()));
            }
        }

        Scene scene = new Scene(root);
        detailStage.setTitle("Détails du Programme - " + p.getTitre());
        detailStage.setScene(scene);
        detailStage.show();
    }

    // ==========================================
    // ACTIONS CATÉGORIES
    // ==========================================

    @FXML
    void handleSaveCategorie(ActionEvent event) {
        String nom = catNomField.getText().trim();
        String desc = catDescArea.getText().trim();

        if (nom.isEmpty()) {
            showAlert("Erreur", "Le nom de la catégorie est obligatoire.");
            return;
        }

        if (desc.length() > 20) {
            showAlert("Erreur", "La description ne doit pas dépasser 20 caractères.");
            return;
        }

        if (selectedCategorie == null) {
            categorieService.ajouterCategorie(new Categorie(nom, desc));
        } else {
            selectedCategorie.setNom(nom);
            selectedCategorie.setDescription(desc);
            categorieService.modifierCategorie(selectedCategorie);
        }

        handleClearCat(null);
        chargerDonnees();
    }

    @FXML
    void handleClearCat(ActionEvent event) {
        selectedCategorie = null;
        catNomField.clear();
        catDescArea.clear();
        catTable.getSelectionModel().clearSelection();
    }

    private void ajouterBoutonSuppressionCat() {
        colCatAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Suppr.");
            {
                btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                btn.setOnAction(event -> {
                    Categorie c = getTableView().getItems().get(getIndex());
                    confirmAndAction(() -> {
                        categorieService.supprimerCategorie(c.getIdCategorie());
                        chargerDonnees();
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    // ==========================================
    // ACTIONS PROGRAMMES
    // ==========================================

    @FXML
    void handleBrowseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir l'image du programme");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            progImageField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    void handleBrowseVideo(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir la vidéo du programme");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Vidéos", "*.mp4", "*.avi", "*.mkv"));
        File selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            progVideoField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    void handleSaveProgramme(ActionEvent event) {
        Categorie cat = catCombo.getValue();
        String titre = progTitreField.getText().trim();
        String desc = progDescArea.getText().trim();
        String img = progImageField.getText().trim();
        String vid = progVideoField.getText().trim();

        if (cat == null || titre.isEmpty()) {
            showAlert("Erreur", "La catégorie et le titre sont obligatoires.");
            return;
        }

        if (desc.length() > 20) {
            showAlert("Erreur", "La description du programme ne doit pas dépasser 20 caractères.");
            return;
        }

        // Image et Vidéo sont facultatifs (déjà géré par la logique car on prend le
        // texte brut)

        if (selectedProgramme == null) {
            programmeService.ajouterProgramme(new Programme(cat, titre, desc, img, vid));
        } else {
            selectedProgramme.setCategorie(cat);
            selectedProgramme.setTitre(titre);
            selectedProgramme.setDescription(desc);
            selectedProgramme.setImage(img);
            selectedProgramme.setVideo(vid);
            programmeService.modifierProgramme(selectedProgramme);
        }

        handleClearProg(null);
        chargerDonnees();
    }

    @FXML
    void handleShowStats(ActionEvent event) {
        List<Programme> programmes = programmeService.afficherProgrammes();
        if (programmes.isEmpty()) {
            showAlert("Information", "Aucun programme disponible pour les statistiques.");
            return;
        }

        // Grouper les programmes par catégorie
        java.util.Map<String, Long> stats = programmes.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        p -> p.getCategorie().getNom(),
                        java.util.stream.Collectors.counting()));

        PieChart pieChart = new PieChart();
        stats.forEach((catNom, count) -> {
            pieChart.getData().add(new PieChart.Data(catNom + " (" + count + ")", count));
        });

        pieChart.setTitle("Répartition des Programmes par Catégorie");
        pieChart.setLegendSide(javafx.geometry.Side.BOTTOM);

        Stage stage = new Stage();
        VBox layout = new VBox(20, pieChart);
        layout.setStyle("-fx-padding: 20; -fx-background-color: white;");
        layout.setAlignment(javafx.geometry.Pos.CENTER);

        Scene scene = new Scene(layout, 600, 500);
        stage.setTitle("Statistiques des Programmes");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void handleClearProg(ActionEvent event) {
        selectedProgramme = null;
        progTitreField.clear();
        progDescArea.clear();
        progImageField.clear();
        progVideoField.clear();
        catCombo.getSelectionModel().clearSelection();
        if (progSaveBtn != null) {
            progSaveBtn.setText("Ajouter Programme");
        }
    }

    // ==========================================
    // UTILS
    // ==========================================

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
        alert.setHeaderText("Action irréversible");
        alert.setContentText("Êtes-vous sûr ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            action.run();
        }
    }
}
