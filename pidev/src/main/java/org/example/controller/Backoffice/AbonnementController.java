package org.example.controller.Backoffice;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.example.entities.Abonnement;
import org.example.services.AbonnementService;

import java.util.Optional;

public class AbonnementController {

    @FXML
    private TextField nomField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField prixField;
    @FXML
    private ComboBox<Integer> dureeComboBox;
    @FXML
    private TextField searchField;
    @FXML
    private TableView<Abonnement> abonnementTable;
    @FXML
    private TableColumn<Abonnement, Integer> colId;
    @FXML
    private TableColumn<Abonnement, String> colNom;
    @FXML
    private TableColumn<Abonnement, String> colDescription;
    @FXML
    private TableColumn<Abonnement, Double> colPrix;
    @FXML
    private TableColumn<Abonnement, Integer> colDuree;
    @FXML
    private TableColumn<Abonnement, Void> colActions;

    private final AbonnementService abonnementService = new AbonnementService();
    private ObservableList<Abonnement> abonnementList = FXCollections.observableArrayList();
    private Abonnement selectedAbonnement = null;

    @FXML
    public void initialize() {
        setupCombo();
        setupColumns();
        loadDonnees();
        setupSearch();
    }

    private void setupCombo() {
        dureeComboBox.setItems(FXCollections.observableArrayList(1, 3, 6, 12, 24));
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idAbonnement"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("dureeMois"));

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        Callback<TableColumn<Abonnement, Void>, TableCell<Abonnement, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Abonnement, Void> call(final TableColumn<Abonnement, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("✎");
                    private final Button btnDelete = new Button("🗑");
                    private final HBox pane = new HBox(5, btnEdit, btnDelete);

                    {
                        btnEdit.setStyle(
                                "-fx-background-color: white; -fx-text-fill: #4a4a4a; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-cursor: hand; -fx-padding: 5 10;");
                        btnDelete.setStyle(
                                "-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-background-radius: 10; -fx-border-color: #fee2e2; -fx-border-radius: 10; -fx-cursor: hand; -fx-padding: 5 10;");

                        btnEdit.setOnMouseEntered(e -> btnEdit.setStyle(
                                "-fx-background-color: #f8fafc; -fx-text-fill: #4a4a4a; -fx-background-radius: 10; -fx-border-color: #a2d5ab; -fx-border-radius: 10; -fx-cursor: hand; -fx-padding: 5 10;"));
                        btnEdit.setOnMouseExited(e -> btnEdit.setStyle(
                                "-fx-background-color: white; -fx-text-fill: #4a4a4a; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-cursor: hand; -fx-padding: 5 10;"));

                        btnEdit.setOnAction(event -> {
                            selectedAbonnement = getTableView().getItems().get(getIndex());
                            fillForm(selectedAbonnement);
                        });

                        btnDelete.setOnAction(event -> {
                            Abonnement a = getTableView().getItems().get(getIndex());
                            handleDelete(a);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty)
                            setGraphic(null);
                        else
                            setGraphic(pane);
                    }
                };
            }
        };
        colActions.setCellFactory(cellFactory);
    }

    private void loadDonnees() {
        abonnementList.setAll(abonnementService.afficherAbonnements());
    }

    private void setupSearch() {
        FilteredList<Abonnement> filteredData = new FilteredList<>(abonnementList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(abonnement -> {
                if (newValue == null || newValue.isEmpty())
                    return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return abonnement.getNom().toLowerCase().contains(lowerCaseFilter);
            });
        });

        javafx.collections.transformation.SortedList<Abonnement> sortedData = new javafx.collections.transformation.SortedList<>(
                filteredData);
        sortedData.comparatorProperty().bind(abonnementTable.comparatorProperty());
        abonnementTable.setItems(sortedData);
    }

    @FXML
    void handleSave(ActionEvent event) {
        try {
            String nom = nomField.getText().trim();
            String desc = descriptionArea.getText().trim();
            String prixStr = prixField.getText().trim();
            Integer duree = dureeComboBox.getValue();

            if (nom.isEmpty() || prixStr.isEmpty() || duree == null) {
                showAlert("Erreur", "Veuillez remplir les champs obligatoires (Nom, Prix, Durée).");
                return;
            }

            double prix = Double.parseDouble(prixStr);

            if (selectedAbonnement == null) {
                abonnementService.ajouterAbonnement(new Abonnement(nom, desc, prix, duree));
            } else {
                selectedAbonnement.setNom(nom);
                selectedAbonnement.setDescription(desc);
                selectedAbonnement.setPrix(prix);
                selectedAbonnement.setDureeMois(duree);
                abonnementService.modifierAbonnement(selectedAbonnement);
            }

            handleClear(null);
            loadDonnees();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le prix doit être un nombre valide.");
        }
    }

    @FXML
    void handleClear(ActionEvent event) {
        selectedAbonnement = null;
        nomField.clear();
        descriptionArea.clear();
        prixField.clear();
        dureeComboBox.getSelectionModel().clearSelection();
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        loadDonnees();
    }

    @FXML
    void handleShowStatistics(ActionEvent event) {
        java.util.Map<Integer, Long> stats = abonnementList.stream()
                .collect(java.util.stream.Collectors.groupingBy(Abonnement::getDureeMois,
                        java.util.stream.Collectors.counting()));

        ObservableList<javafx.scene.chart.PieChart.Data> pieData = FXCollections.observableArrayList();
        stats.forEach((duree, count) -> pieData
                .add(new javafx.scene.chart.PieChart.Data(duree + " mois (" + count + ")", count)));

        javafx.scene.chart.PieChart chart = new javafx.scene.chart.PieChart(pieData);
        chart.setTitle("Répartition des offres par durée");

        javafx.scene.Scene scene = new javafx.scene.Scene(chart, 500, 500);
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Statistiques Abonnements");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void handleExportCSV(ActionEvent event) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Enregistrer l'export CSV");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        fileChooser.setInitialFileName("abonnements_export.csv");

        java.io.File file = fileChooser.showSaveDialog(abonnementTable.getScene().getWindow());
        if (file != null) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                writer.println("ID,Nom,Description,Prix,Duree_Mois");
                for (Abonnement a : abonnementList) {
                    writer.printf("%d,\"%s\",\"%s\",%.2f,%d%n",
                            a.getIdAbonnement(),
                            a.getNom().replace("\"", "\"\""),
                            a.getDescription().replace("\"", "\"\""),
                            a.getPrix(),
                            a.getDureeMois());
                }
                showAlertInfo("Succès", "Données exportées avec succès !");
            } catch (java.io.IOException e) {
                showAlert("Erreur", "Échec de l'exportation : " + e.getMessage());
            }
        }
    }

    private void fillForm(Abonnement a) {
        nomField.setText(a.getNom());
        descriptionArea.setText(a.getDescription());
        prixField.setText(String.valueOf(a.getPrix()));
        dureeComboBox.setValue(a.getDureeMois());
    }

    private void handleDelete(Abonnement a) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Suppression de l'abonnement : " + a.getNom());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette offre ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            abonnementService.supprimerAbonnement(a.getIdAbonnement());
            loadDonnees();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAlertInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
