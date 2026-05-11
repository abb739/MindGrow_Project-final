package org.example.services;

import org.example.entities.Seance;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SeanceService {
    private Connection connection;

    public SeanceService() {
        this.connection = MyDataBase.getConnection();
    }

    public void ajouterSeance(Seance seance) {
        String query = "INSERT INTO seance (titre, description, lieu, date_debut, date_fin, capacite, image) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, seance.getTitre());
            ps.setString(2, seance.getDescription());
            ps.setString(3, seance.getLieu());
            ps.setTimestamp(4, Timestamp.valueOf(seance.getDateDebut()));
            ps.setTimestamp(5, Timestamp.valueOf(seance.getDateFin()));
            ps.setInt(6, seance.getCapacite());
            ps.setString(7, seance.getImage());
            ps.executeUpdate();
            System.out.println("Séance ajoutée avec succès!");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la séance: " + e.getMessage());
        }
    }

    public List<Seance> afficherSeances() {
        List<Seance> seances = new ArrayList<>();
        String query = "SELECT * FROM seance";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                Seance s = new Seance(
                        rs.getInt("id_seance"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("lieu"),
                        rs.getTimestamp("date_debut").toLocalDateTime(),
                        rs.getTimestamp("date_fin").toLocalDateTime(),
                        rs.getInt("capacite"),
                        rs.getString("image")
                );
                seances.add(s);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des séances: " + e.getMessage());
        }
        return seances;
    }

    public void modifierSeance(Seance seance) {
        String query = "UPDATE seance SET titre=?, description=?, lieu=?, date_debut=?, date_fin=?, capacite=?, image=? WHERE id_seance=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, seance.getTitre());
            ps.setString(2, seance.getDescription());
            ps.setString(3, seance.getLieu());
            ps.setTimestamp(4, Timestamp.valueOf(seance.getDateDebut()));
            ps.setTimestamp(5, Timestamp.valueOf(seance.getDateFin()));
            ps.setInt(6, seance.getCapacite());
            ps.setString(7, seance.getImage());
            ps.setInt(8, seance.getIdSeance());
            ps.executeUpdate();
            System.out.println("Séance modifiée avec succès!");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification de la séance: " + e.getMessage());
        }
    }

    public void supprimerSeance(int id) {
        String query = "DELETE FROM seance WHERE id_seance=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Séance supprimée avec succès!");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la séance: " + e.getMessage());
        }
    }

    public Seance getSeanceById(int id) {
        String query = "SELECT * FROM seance WHERE id_seance=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Seance(
                        rs.getInt("id_seance"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("lieu"),
                        rs.getTimestamp("date_debut").toLocalDateTime(),
                        rs.getTimestamp("date_fin").toLocalDateTime(),
                        rs.getInt("capacite"),
                        rs.getString("image")
                );
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la séance: " + e.getMessage());
        }
        return null;
    }

    public void regulerCapacite(int idSeance, int delta) {
        String query = "UPDATE seance SET capacite = capacite + ? WHERE id_seance = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, delta);
            ps.setInt(2, idSeance);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la capacité: " + e.getMessage());
        }
    }
}
