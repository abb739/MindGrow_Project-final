package org.example.controller.Backoffice;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.example.entities.ReservationSeance;
import org.example.services.ReservationSeanceService;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ReservationSeanceManageController {

    @FXML private TableView<ReservationSeance> reservationTable;
    @FXML private TableColumn<ReservationSeance, Integer> colId;
    @FXML private TableColumn<ReservationSeance, String> colSeance;
    @FXML private TableColumn<ReservationSeance, String> colUtilisateur;
    @FXML private TableColumn<ReservationSeance, String> colDate;
    @FXML private TableColumn<ReservationSeance, String> colStatut;
    @FXML private TableColumn<ReservationSeance, Void> colActions;

    private final ReservationSeanceService reservationService = new ReservationSeanceService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupColumns();
        loadReservations();
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idReservation"));
        colSeance.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSeance().getTitre()));
        colUtilisateur.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUtilisateur().getNom() + " " + cellData.getValue().getUtilisateur().getPrenom()));
        colDate.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDateReservation().format(formatter)));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        Callback<TableColumn<ReservationSeance, Void>, TableCell<ReservationSeance, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<ReservationSeance, Void> call(final TableColumn<ReservationSeance, Void> param) {
                return new TableCell<>() {
                    private final Button btnStatus = new Button("Statut");
                    private final Button btnDelete = new Button("Supprimer");
                    private final HBox pane = new HBox(10, btnStatus, btnDelete);

                    {
                        btnStatus.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
                        btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                        
                        btnStatus.setOnAction(event -> {
                            ReservationSeance r = getTableView().getItems().get(getIndex());
                            handleEditStatus(r);
                        });

                        btnDelete.setOnAction(event -> {
                            ReservationSeance r = getTableView().getItems().get(getIndex());
                            handleDelete(r);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        };
        colActions.setCellFactory(cellFactory);
    }

    @FXML
    public void loadReservations() {
        ObservableList<ReservationSeance> list = FXCollections.observableArrayList(reservationService.getAllReservations());
        reservationTable.setItems(list);
    }

    private void handleEditStatus(ReservationSeance r) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(r.getStatut(), "confirmée", "annulée", "en attente");
        dialog.setTitle("Modifier le statut");
        dialog.setHeaderText("Sélectionnez le nouveau statut pour la réservation #" + r.getIdReservation());
        dialog.setContentText("Statut :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newStatus -> {
            reservationService.modifierStatut(r.getIdReservation(), newStatus);
            loadReservations();
        });
    }

    private void handleDelete(ReservationSeance r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Suppression de la réservation #" + r.getIdReservation());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réservation ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            reservationService.supprimerReservation(r.getIdReservation());
            loadReservations();
        }
    }
}
