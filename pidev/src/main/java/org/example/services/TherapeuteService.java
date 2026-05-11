package org.example.services;

import org.example.entities.Therapeute;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TherapeuteService {

    private Connection connection;

    public TherapeuteService() {
        this.connection = MyDataBase.getConnection();
    }

    public void ajouterTherapeute(Therapeute t) {
        String query = "INSERT INTO therapeute (nom, prenom, image, certificat, specialite, email, telephone) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, t.getNom());
            ps.setString(2, t.getPrenom());
            ps.setString(3, t.getImage());
            ps.setString(4, t.getCertificat());
            ps.setString(5, t.getSpecialite());
            ps.setString(6, t.getEmail());
            ps.setString(7, t.getTelephone());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur ajout thérapeute : " + e.getMessage());
        }
    }

    public List<Therapeute> afficherTherapeutes() {
        List<Therapeute> list = new ArrayList<>();
        String query = "SELECT * FROM therapeute ORDER BY date_inscription DESC";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("date_inscription");
                list.add(new Therapeute(
                        rs.getInt("id_therapeute"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("image"),
                        rs.getString("certificat"),
                        rs.getString("specialite"),
                        rs.getString("email"),
                        rs.getString("telephone"),
                        ts != null ? ts.toLocalDateTime() : null
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération thérapeutes : " + e.getMessage());
        }
        return list;
    }

    public void modifierTherapeute(Therapeute t) {
        String query = "UPDATE therapeute SET nom=?, prenom=?, image=?, certificat=?, specialite=?, email=?, telephone=? WHERE id_therapeute=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, t.getNom());
            ps.setString(2, t.getPrenom());
            ps.setString(3, t.getImage());
            ps.setString(4, t.getCertificat());
            ps.setString(5, t.getSpecialite());
            ps.setString(6, t.getEmail());
            ps.setString(7, t.getTelephone());
            ps.setInt(8, t.getIdTherapeute());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur modification thérapeute : " + e.getMessage());
        }
    }

    public void supprimerTherapeute(int id) {
        String query = "DELETE FROM therapeute WHERE id_therapeute=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur suppression thérapeute : " + e.getMessage());
        }
    }
}
