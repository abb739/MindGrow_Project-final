package org.example.controller.Backoffice;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.entities.Utilisateur;
import org.example.services.UtilisateurService;
import org.example.utils.AuthThemeManager;

// Imports iText pour l'export PDF
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class AdminDashboardController {

    @FXML
    private AnchorPane rootPane;

    // Vues principales pour basculer
    @FXML
    private VBox usersView;
    @FXML
    private VBox profilView;
    @FXML
    private VBox programmesView;
    @FXML
    private VBox seancesView;
    @FXML
    private VBox abonnementsView;
    @FXML
    private VBox therapeutesView;

    // Éléments du Profil
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;

    // Éléments TableView et Filtres
    @FXML
    private TableView<Utilisateur> usersTable;
    @FXML
    private TableColumn<Utilisateur, Integer> colId;
    @FXML
    private TableColumn<Utilisateur, String> colNom;
    @FXML
    private TableColumn<Utilisateur, String> colPrenom;
    @FXML
    private TableColumn<Utilisateur, String> colEmail;
    @FXML
    private TableColumn<Utilisateur, String> colRole;
    @FXML
    private TableColumn<Utilisateur, Void> colAction;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> roleFilter;
    @FXML
    private ToggleButton darkModeToggle;

    private Utilisateur utilisateurConnecte;
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private ObservableList<Utilisateur> utilisateursList = FXCollections.observableArrayList();
    private FilteredList<Utilisateur> filteredData;

    public void initialize() {
        if (rootPane != null) {
            AuthThemeManager.applyDefaultTheme(rootPane);
            playEntranceAnimation(rootPane);
        }

        if (darkModeToggle != null) {
            darkModeToggle.setSelected(AuthThemeManager.isDarkMode());
            darkModeToggle.setText(AuthThemeManager.isDarkMode() ? "☀️ Mode Clair" : "🌙 Mode Sombre");
        }
    }

    public void initData(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
        System.out.println("Espace Admin chargé pour : " + utilisateur.getEmail());

        // Pré-remplir profil
        nomField.setText(utilisateur.getNom());
        prenomField.setText(utilisateur.getPrenom());
        emailField.setText(utilisateur.getEmail());

        // Configurer TableView et Filtres
        configurerTableView();
        configurerFiltres();
        chargerUtilisateurs();

        // Afficher la liste des utilisateurs par défaut
        showUsersView(null);
    }

    // ==========================================
    // GESTION DU PROFIL
    // ==========================================

    @FXML
    void handleUpdateProfil(ActionEvent event) {
        playButtonClickAnimation(event);
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Nom, Prénom et Email obligatoires.");
            return;
        }

        utilisateurConnecte.setNom(nom);
        utilisateurConnecte.setPrenom(prenom);
        utilisateurConnecte.setEmail(email);

        if (!password.isEmpty()) {
            if (password.length() < 8) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("8 caractères minimum pour le MDP.");
                return;
            }
            utilisateurConnecte.setMotDePasse(password);
        }

        utilisateurService.modifierUtilisateur(utilisateurConnecte);
        messageLabel.setStyle("-fx-text-fill: green;");
        messageLabel.setText("Profil mis à jour !");
        passwordField.clear();

        // Rafraichir le tableau si l'admin modifie ses propres infos
        chargerUtilisateurs();
    }

    // ==========================================
    // GESTION LISTE UTILISATEURS
    // ==========================================

    private void configurerTableView() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUtilisateur"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Ajouter un bouton de suppression dans la colonne action
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Supprimer");

            {
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                deleteButton.setOnAction(event -> {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    supprimerUtilisateur(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(deleteButton);
                    box.setStyle("-fx-alignment: center;");
                    setGraphic(box);
                }
            }
        });
    }

    private void chargerUtilisateurs() {
        List<Utilisateur> listeBDD = utilisateurService.afficherUtilisateurs();
        utilisateursList.setAll(listeBDD);
    }

    private void configurerFiltres() {
        // Initialiser la ComboBox
        roleFilter.setItems(FXCollections.observableArrayList("Tous", "admin", "client"));
        roleFilter.getSelectionModel().selectFirst();

        // Envelopper l'ObservableList dans une FilteredList
        filteredData = new FilteredList<>(utilisateursList, b -> true);

        // Listener pour la barre de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterData());

        // Listener pour la ComboBox de rôle
        roleFilter.valueProperty().addListener((observable, oldValue, newValue) -> filterData());

        // Envelopper la FilteredList dans une SortedList pour que le tri du tableau
        // fonctionne toujours
        SortedList<Utilisateur> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(usersTable.comparatorProperty());

        // Ajouter les données triées (et filtrées) au tableau
        usersTable.setItems(sortedData);
    }

    private void filterData() {
        String searchKeyword = searchField.getText().toLowerCase();
        String roleKeyword = roleFilter.getValue();

        filteredData.setPredicate(utilisateur -> {
            boolean matchesSearch = true;
            boolean matchesRole = true;

            // Filtre par nom ou prénom
            if (searchKeyword != null && !searchKeyword.isEmpty()) {
                if (utilisateur.getNom().toLowerCase().contains(searchKeyword) ||
                        utilisateur.getPrenom().toLowerCase().contains(searchKeyword)) {
                    matchesSearch = true;
                } else {
                    matchesSearch = false;
                }
            }

            // Filtre par rôle
            if (roleKeyword != null && !roleKeyword.equals("Tous")) {
                if (!utilisateur.getRole().equalsIgnoreCase(roleKeyword)) {
                    matchesRole = false;
                }
            }

            return matchesSearch && matchesRole;
        });
    }

    // ==========================================
    // STATISTIQUES ET EXPORT
    // ==========================================

    @FXML
    void showStatistics(ActionEvent event) {
        playButtonClickAnimation(event);
        int nbAdmins = 0;
        int nbClients = 0;

        // Calcul à partir de la liste complète chargée
        for (Utilisateur u : utilisateursList) {
            if ("admin".equalsIgnoreCase(u.getRole())) {
                nbAdmins++;
            } else if ("client".equalsIgnoreCase(u.getRole())) {
                nbClients++;
            }
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Admins (" + nbAdmins + ")", nbAdmins),
                new PieChart.Data("Clients (" + nbClients + ")", nbClients));

        PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Répartition des Rôles Utilisateurs");

        Scene scene = new Scene(new VBox(chart), 400, 400);
        Stage stage = new Stage();
        stage.setTitle("Statistiques");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void exportToPDF(ActionEvent event) {
        playButtonClickAnimation(event);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("Liste_Utilisateurs.pdf");

        // Ouvrir la boîte de dialogue de sauvegarde
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                // Initialiser l'écriture PDF
                PdfWriter writer = new PdfWriter(file.getAbsolutePath());
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc);

                document.add(new Paragraph("Rapport: Liste des Utilisateurs MindGrow")
                        .setBold().setFontSize(18).setMarginBottom(20));

                // Créer un tableau (4 colonnes) pour le PDF
                Table table = new Table(new float[] { 1, 3, 3, 3 });
                table.setWidth(100);

                // En-têtes
                table.addHeaderCell("ID");
                table.addHeaderCell("Nom complet");
                table.addHeaderCell("Email");
                table.addHeaderCell("Rôle");

                // Remplir le tableau avec les données *actuellement filtrées* du TableView
                for (Utilisateur u : usersTable.getItems()) {
                    table.addCell(String.valueOf(u.getIdUtilisateur()));
                    table.addCell(u.getNom() + " " + u.getPrenom());
                    table.addCell(u.getEmail());
                    table.addCell(u.getRole());
                }

                document.add(table);
                document.close();

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Succès");
                info.setHeaderText(null);
                info.setContentText("PDF généré avec succès à l'emplacement :\n" + file.getAbsolutePath());
                info.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Erreur");
                error.setHeaderText(null);
                error.setContentText("Erreur lors de la génération du PDF : " + e.getMessage());
                error.showAndWait();
            }
        }
    }

    private void supprimerUtilisateur(Utilisateur user) {
        if (user.getIdUtilisateur() == utilisateurConnecte.getIdUtilisateur()) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Erreur");
            error.setHeaderText(null);
            error.setContentText("Vous ne pouvez pas supprimer votre propre compte admin depuis cette liste.");
            error.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Suppression de l'utilisateur : " + user.getEmail());
        alert.setContentText("Êtes-vous sûr ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            utilisateurService.supprimerUtilisateur(user.getIdUtilisateur());
            chargerUtilisateurs(); // Rafraichir le tableau
        }
    }

    // ==========================================
    // NAVIGATION
    // ==========================================

    @FXML
    void showUsersView(ActionEvent event) {
        playButtonClickAnimation(event);
        profilView.setVisible(false);
        programmesView.setVisible(false);
        seancesView.setVisible(false);
        usersView.setVisible(true);
        chargerUtilisateurs(); // Toujours recharger avant d'afficher
        playViewTransition(usersView);
    }

    @FXML
    void showProfilView(ActionEvent event) {
        playButtonClickAnimation(event);
        usersView.setVisible(false);
        programmesView.setVisible(false);
        seancesView.setVisible(false);
        profilView.setVisible(true);
        playViewTransition(profilView);
    }

    @FXML
    void showProgrammesView(ActionEvent event) {
        playButtonClickAnimation(event);
        usersView.setVisible(false);
        profilView.setVisible(false);
        seancesView.setVisible(false);
        programmesView.setVisible(true);

        if (programmesView.getChildren().isEmpty()) {
            try {
                Node node = FXMLLoader.load(getClass().getResource("/Backoffice/ProgrammeCRUD.fxml"));
                programmesView.getChildren().add(node);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        playViewTransition(programmesView);
    }

    @FXML
    void showSeancesView(ActionEvent event) {
        playButtonClickAnimation(event);
        usersView.setVisible(false);
        profilView.setVisible(false);
        programmesView.setVisible(false);
        abonnementsView.setVisible(false);
        seancesView.setVisible(true);

        if (seancesView.getChildren().isEmpty()) {
            try {
                Node node = FXMLLoader.load(getClass().getResource("/Backoffice/SeanceCRUD.fxml"));
                seancesView.getChildren().add(node);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        playViewTransition(seancesView);
    }

    @FXML
    void showAbonnementsView(ActionEvent event) {
        playButtonClickAnimation(event);
        usersView.setVisible(false);
        profilView.setVisible(false);
        programmesView.setVisible(false);
        seancesView.setVisible(false);
        therapeutesView.setVisible(false);
        abonnementsView.setVisible(true);

        if (abonnementsView.getChildren().isEmpty()) {
            try {
                Node node = FXMLLoader.load(getClass().getResource("/Backoffice/AbonnementCRUD.fxml"));
                abonnementsView.getChildren().add(node);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        playViewTransition(abonnementsView);
    }

    @FXML
    void showTherapeutesView(ActionEvent event) {
        playButtonClickAnimation(event);
        usersView.setVisible(false);
        profilView.setVisible(false);
        programmesView.setVisible(false);
        seancesView.setVisible(false);
        abonnementsView.setVisible(false);
        therapeutesView.setVisible(true);

        if (therapeutesView.getChildren().isEmpty()) {
            try {
                Node node = FXMLLoader.load(getClass().getResource("/Backoffice/TherapeuteCRUD.fxml"));
                therapeutesView.getChildren().add(node);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        playViewTransition(therapeutesView);
    }

    @FXML
    void handleLogout(ActionEvent event) {
        playButtonClickAnimation(event);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Frontoffice/SignIn.fxml"));
            AuthThemeManager.applyDefaultTheme(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("Connexion");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleDarkModeToggle(ActionEvent event) {
        boolean darkMode = darkModeToggle.isSelected();
        AuthThemeManager.setDarkMode(darkMode, rootPane);
        darkModeToggle.setText(AuthThemeManager.isDarkMode() ? "☀️ Mode Clair" : "🌙 Mode Sombre");
    }

    private void playEntranceAnimation(Node node) {
        if (node == null) return;
        FadeTransition fade = new FadeTransition(Duration.millis(450), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        ScaleTransition scale = new ScaleTransition(Duration.millis(450), node);
        scale.setFromX(0.98);
        scale.setFromY(0.98);
        scale.setToX(1);
        scale.setToY(1);
        scale.play();
    }

    private void playViewTransition(Node node) {
        if (node == null) return;
        FadeTransition fade = new FadeTransition(Duration.millis(275), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void playButtonClickAnimation(ActionEvent event) {
        if (event == null) return;
        Object source = event.getSource();
        if (source instanceof Button button) {
            ScaleTransition scale = new ScaleTransition(Duration.millis(120), button);
            scale.setFromX(1.0);
            scale.setFromY(1.0);
            scale.setToX(0.96);
            scale.setToY(0.96);
            scale.setAutoReverse(true);
            scale.setCycleCount(2);
            scale.play();
        }
    }
}
