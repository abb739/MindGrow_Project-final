package org.example.services;

import org.example.entities.Achat;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AchatService {

    private Connection connection;

    public AchatService() {
        connection = MyDataBase.getConnection();
    }

    public void ajouterAchat(Achat a) {
        String query = "INSERT INTO achat (id_abonnement, id_utilisateur, statut) VALUES (?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, a.getIdAbonnement());
            pst.setInt(2, a.getIdUtilisateur());
            pst.setString(3, a.getStatut());
            pst.executeUpdate();
            System.out.println("Achat ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'achat : " + e.getMessage());
        }
    }

    public List<Achat> afficherAchats() {
        List<Achat> list = new ArrayList<>();
        String query = "SELECT * FROM achat";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Achat(
                        rs.getInt("id_achat"),
                        rs.getInt("id_abonnement"),
                        rs.getInt("id_utilisateur"),
                        rs.getTimestamp("date_achat").toLocalDateTime(),
                        rs.getString("statut")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la lecture des achats : " + e.getMessage());
        }
        return list;
    }

    public Achat getAchatActifParUtilisateur(int idUtilisateur) {
        String query = "SELECT * FROM achat WHERE id_utilisateur = ? AND statut = 'actif' ORDER BY date_achat DESC LIMIT 1";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idUtilisateur);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Achat(
                        rs.getInt("id_achat"),
                        rs.getInt("id_abonnement"),
                        rs.getInt("id_utilisateur"),
                        rs.getTimestamp("date_achat").toLocalDateTime(),
                        rs.getString("statut")
                );
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de l'achat actif : " + e.getMessage());
        }
        return null;
    }

    public void modifierStatut(int idAchat, String nouveauStatut) {
        String query = "UPDATE achat SET statut = ? WHERE id_achat = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, nouveauStatut);
            pst.setInt(2, idAchat);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur modification statut achat : " + e.getMessage());
        }
    }

    public void supprimerAchatsParUtilisateur(int idUtilisateur) {
        String query = "DELETE FROM achat WHERE id_utilisateur = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idUtilisateur);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur suppression achats utilisateur : " + e.getMessage());
        }
    }
}
