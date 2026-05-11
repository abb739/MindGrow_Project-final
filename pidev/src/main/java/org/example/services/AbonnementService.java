package org.example.services;

import org.example.entities.Abonnement;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AbonnementService {
    private Connection connection;

    public AbonnementService() {
        this.connection = MyDataBase.getConnection();
    }

    public void ajouterAbonnement(Abonnement a) {
        String query = "INSERT INTO abonnement (nom, description, prix, duree_mois) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, a.getNom());
            ps.setString(2, a.getDescription());
            ps.setDouble(3, a.getPrix());
            ps.setInt(4, a.getDureeMois());
            ps.executeUpdate();
            System.out.println("Abonnement ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'abonnement : " + e.getMessage());
        }
    }

    public List<Abonnement> afficherAbonnements() {
        List<Abonnement> list = new ArrayList<>();
        String query = "SELECT * FROM abonnement";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Abonnement(
                        rs.getInt("id_abonnement"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getDouble("prix"),
                        rs.getInt("duree_mois")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des abonnements : " + e.getMessage());
        }
        return list;
    }

    public void modifierAbonnement(Abonnement a) {
        String query = "UPDATE abonnement SET nom = ?, description = ?, prix = ?, duree_mois = ? WHERE id_abonnement = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, a.getNom());
            ps.setString(2, a.getDescription());
            ps.setDouble(3, a.getPrix());
            ps.setInt(4, a.getDureeMois());
            ps.setInt(5, a.getIdAbonnement());
            ps.executeUpdate();
            System.out.println("Abonnement modifié avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification de l'abonnement : " + e.getMessage());
        }
    }

    public void supprimerAbonnement(int id) {
        String query = "DELETE FROM abonnement WHERE id_abonnement = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Abonnement supprimé avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'abonnement : " + e.getMessage());
        }
    }

    public Abonnement getAbonnementById(int id) {
        String query = "SELECT * FROM abonnement WHERE id_abonnement = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Abonnement(
                        rs.getInt("id_abonnement"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getDouble("prix"),
                        rs.getInt("duree_mois")
                );
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'abonnement : " + e.getMessage());
        }
        return null;
    }
}
