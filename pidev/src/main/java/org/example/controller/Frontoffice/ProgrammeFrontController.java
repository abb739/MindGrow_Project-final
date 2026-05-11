package org.example.controller.Frontoffice;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.example.entities.Categorie;
import org.example.entities.Programme;
import org.example.services.CategorieService;
import org.example.services.FavoriService;
import org.example.services.ProgrammeService;
import org.example.utils.UploadPathResolver;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProgrammeFrontController {

    @FXML
    private FlowPane progCardsContainer;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> catFilterCombo;
    @FXML
    private Button prevBtn, nextBtn;
    @FXML
    private Button youtubeSearchBtn;
    @FXML
    private Label pageLabel;

    private Stage chatbotStage; // Pour éviter d'ouvrir plusieurs fenêtres

    private final ProgrammeService programmeService = new ProgrammeService();
    private final CategorieService categorieService = new CategorieService();
    private final FavoriService favoriService = new FavoriService();

    private List<Programme> allProgrammes = new ArrayList<>();
    private List<Programme> filteredProgrammes = new ArrayList<>();

    // Pour la démonstration, on utilise l'ID utilisateur 1 (meddeb@gmail.com)
    private final int currentUserId = 1;

    private int currentPage = 0;
    private final int itemsPerPage = 2;

    @FXML
    public void initialize() {
        loadData();
        setupFilters();
        updateDisplay();
    }

    private void loadData() {
        allProgrammes = programmeService.afficherProgrammes();
        filteredProgrammes = new ArrayList<>(allProgrammes);

        // Charger les catégories dans le combo
        List<String> categories = categorieService.afficherCategories().stream()
                .map(org.example.entities.Categorie::getNom)
                .collect(Collectors.toList());
        catFilterCombo.setItems(FXCollections.observableArrayList(categories));
        catFilterCombo.getItems().add(0, "Toutes les catégories");
        catFilterCombo.getItems().add(1, "⭐ Mes Favoris");
    }

    private void setupFilters() {
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            applyFilters();
        });

        catFilterCombo.valueProperty().addListener((obs, oldV, newV) -> {
            applyFilters();
        });
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String selectedCat = catFilterCombo.getValue();

        filteredProgrammes = allProgrammes.stream()
                .filter(p -> p.getTitre().toLowerCase().contains(searchText))
                .filter(p -> {
                    if (selectedCat == null || selectedCat.equals("Toutes les catégories"))
                        return true;
                    if (selectedCat.equals("⭐ Mes Favoris"))
                        return favoriService.estFavori(currentUserId, p.getIdProgramme());
                    return p.getCategorie().getNom().equals(selectedCat);
                })
                .collect(Collectors.toList());

        currentPage = 0;
        updateDisplay();
    }

    private void updateDisplay() {
        progCardsContainer.getChildren().clear();

        int totalItems = filteredProgrammes.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        if (totalPages == 0)
            totalPages = 1;

        if (currentPage >= totalPages)
            currentPage = totalPages - 1;
        if (currentPage < 0)
            currentPage = 0;

        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, totalItems);

        for (int i = start; i < end; i++) {
            progCardsContainer.getChildren().add(createProgrammeCard(filteredProgrammes.get(i)));
        }

        pageLabel.setText("Page " + (currentPage + 1) + " / " + totalPages);
        prevBtn.setDisable(currentPage == 0);
        nextBtn.setDisable(currentPage >= totalPages - 1);
    }

    @FXML
    void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            updateDisplay();
        }
    }

    @FXML
    void handleNextPage() {
        int totalPages = (int) Math.ceil((double) filteredProgrammes.size() / itemsPerPage);
        if (currentPage < totalPages - 1) {
            currentPage++;
            updateDisplay();
        }
    }

    @FXML
    void handleOpenChatbot() {
        if (chatbotStage != null && chatbotStage.isShowing()) {
            chatbotStage.toFront();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Frontoffice/Chatbot.fxml"));
            Parent root = loader.load();

            ChatbotController controller = loader.getController();
            controller.setProgrammes(allProgrammes);

            chatbotStage = new Stage();
            chatbotStage.setTitle("Assistant IA MindGrow");
            chatbotStage.setScene(new Scene(root));
            chatbotStage.setResizable(false);
            chatbotStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleOpenTimer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Frontoffice/TimerDialog.fxml"));
            Parent root = loader.load();

            Stage timerStage = new Stage();
            timerStage.setTitle("Minuteur MindGrow");
            timerStage.setScene(new Scene(root));
            timerStage.setResizable(false);
            timerStage.setAlwaysOnTop(true);
            timerStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleYoutubeSearch() {
        try {
            String searchText = searchField.getText().trim();
            String query = "MindGrow " + (searchText.isEmpty() ? "bien-être développement personnel" : searchText);
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://www.youtube.com/results?search_query=" + encodedQuery;
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshProgrammesCards() {
        // Redondant avec updateDisplay désormais
    }

    private void updateHeartIcon(Button btn, boolean isFav) {
        if (isFav) {
            btn.setText("❤️");
            btn.setTooltip(new Tooltip("Retirer des favoris"));
        } else {
            btn.setText("🤍");
            btn.setTooltip(new Tooltip("Ajouter aux favoris"));
        }
    }

    private VBox createProgrammeCard(Programme p) {
        VBox card = new VBox(10);
        card.setPrefWidth(220);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 0; -fx-effect: dropshadow(gaussian, rgba(11,122,143,0.12), 15, 0, 0, 6); -fx-border-color: #f1f5f9; -fx-border-radius: 16; -fx-border-width: 1; -fx-cursor: hand;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(190);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        try {
            String resolved = UploadPathResolver.resolve(p.getImage());
            if (resolved != null && !resolved.isBlank() && new java.io.File(resolved).exists()) {
                imageView.setImage(new Image(new java.io.File(resolved).toURI().toString()));
            } else {
                imageView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
            }
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
        }

        Label titleLabel = new Label(p.getTitre());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2c3e50;");
        titleLabel.setWrapText(true);

        Label catLabel = new Label(p.getCategorie().getNom());
        catLabel.setStyle("-fx-text-fill: #0b7a8f; -fx-font-size: 13px; -fx-font-weight: bold;");

        // --- Bouton Favori (Cœur) ---
        Button favBtn = new Button();
        boolean isFav = favoriService.estFavori(currentUserId, p.getIdProgramme());
        updateHeartIcon(favBtn, isFav);
        favBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 18px; -fx-text-fill: #0b7a8f;");

        favBtn.setOnAction(e -> {
            boolean currentFav = favoriService.estFavori(currentUserId, p.getIdProgramme());
            if (currentFav) {
                favoriService.supprimerFavori(currentUserId, p.getIdProgramme());
                updateHeartIcon(favBtn, false);
            } else {
                favoriService.ajouterFavori(currentUserId, p.getIdProgramme());
                updateHeartIcon(favBtn, true);
            }
            e.consume(); // Empêcher l'ouverture des détails
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topRow = new HBox(catLabel, spacer, favBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(imageView, topRow, titleLabel);

        // Clic sur la carte pour voir les détails
        card.setOnMouseClicked(event -> {
            showProgrammeDetails(p);
        });

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle()
                + "-fx-scale-x: 1.03; -fx-scale-y: 1.03; -fx-border-color: rgba(11,122,143,0.25); -fx-border-radius: 16;"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace(
                "-fx-scale-x: 1.03; -fx-scale-y: 1.03; -fx-border-color: rgba(11,122,143,0.25); -fx-border-radius: 16;", "")));


        return card;
    }

    private void showProgrammeDetails(Programme p) {
        Stage detailStage = new Stage();
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 30; -fx-background-color: #ffffff;");
        root.setPrefWidth(700);
        root.setMinWidth(700);

        Label title = new Label(p.getTitre());
        title.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #4a4a4a;");

        Label cat = new Label("Catégorie : " + p.getCategorie().getNom());
        cat.setStyle("-fx-font-size: 16px; -fx-text-fill: #0b7a8f; -fx-font-weight: bold;");

        Separator separator = new Separator();

        Label descTitle = new Label("Description du programme :");
        descTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label desc = new Label(p.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");

        root.getChildren().addAll(title, cat, separator, descTitle, desc);

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
                    mediaView.setFitWidth(640);
                    mediaView.setPreserveRatio(true);

                    // Conteneur pour la vidéo avec fond noir
                    StackPane videoContainer = new StackPane(mediaView);
                    videoContainer.setStyle("-fx-background-color: black; -fx-background-radius: 5;");
                    videoContainer.setPrefHeight(360);

                    Button playBtn = new Button("▶ Lecture");
                    Button pauseBtn = new Button("⏸ Pause");
                    Button stopBtn = new Button("⏹ Stop");

                    playBtn.setStyle(
                            "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #0b7a8f 0%, #065f6e 100%); -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand; -fx-font-weight: bold;");
                    pauseBtn.setStyle(
                            "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #0b7a8f 0%, #065f6e 100%); -fx-text-fill: white; -fx-background-radius: 15; -fx-font-weight: bold; -fx-cursor: hand;");
                    stopBtn.setStyle(
                            "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

                    HBox videoControls = new HBox(15, playBtn, pauseBtn, stopBtn);
                    videoControls.setStyle("-fx-alignment: center; -fx-padding: 10;");

                    playBtn.setOnAction(e -> mediaPlayer.play());
                    pauseBtn.setOnAction(e -> mediaPlayer.pause());
                    stopBtn.setOnAction(e -> mediaPlayer.stop());

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

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600);

        Scene scene = new Scene(scrollPane);
        detailStage.setTitle("MindGrow - Détails du Programme : " + p.getTitre());
        detailStage.setScene(scene);
        detailStage.show();
    }
}
