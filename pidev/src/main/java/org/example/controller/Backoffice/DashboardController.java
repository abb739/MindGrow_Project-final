package org.example.controller.Backoffice;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import org.example.entities.Utilisateur;
import org.example.services.UtilisateurService;

import java.time.LocalDate;
import java.util.*;

public class DashboardController {

    @FXML
    private Label lblTotalUsers;
    @FXML
    private Label lblTotalAdmins;
    @FXML
    private Label lblTotalClients;

    @FXML
    private PieChart chartUserRoles;

    private UtilisateurService utilisateurService;

    @FXML
    public void initialize() {
        utilisateurService = new UtilisateurService();
        actualiserDashboard();
    }

    @FXML
    private void actualiserDashboard() {
        chargerStatistiques();
        chargerGraphiques();
    }

    private void chargerStatistiques() {
        List<Utilisateur> users = utilisateurService.afficherUtilisateurs();
        long admins = users.stream().filter(u -> "admin".equalsIgnoreCase(u.getRole())).count();
        long clients = users.stream().filter(u -> "client".equalsIgnoreCase(u.getRole())).count();

        lblTotalUsers.setText(String.valueOf(users.size()));
        lblTotalAdmins.setText(String.valueOf(admins));
        lblTotalClients.setText(String.valueOf(clients));
    }

    private void chargerGraphiques() {
        List<Utilisateur> users = utilisateurService.afficherUtilisateurs();
        long admins = users.stream().filter(u -> "admin".equalsIgnoreCase(u.getRole())).count();
        long clients = users.stream().filter(u -> "client".equalsIgnoreCase(u.getRole())).count();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Admins (" + admins + ")", admins),
                new PieChart.Data("Clients (" + clients + ")", clients));
        chartUserRoles.setData(pieChartData);
    }
}