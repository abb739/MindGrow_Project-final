package org.example.services;

import org.example.entities.Programme;
import org.example.entities.Categorie;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriService {
    private Connection connection;

    public FavoriService() {
        this.connection = MyDataBase.getConnection();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String query = "CREATE TABLE IF NOT EXISTS favoris (" +
                "id_favori INT AUTO_INCREMENT PRIMARY KEY, " +
                "id_utilisateur INT NOT NULL, " +
                "id_programme INT NOT NULL, " +
                "FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur) ON DELETE CASCADE, " +
                "FOREIGN KEY (id_programme) REFERENCES programme(id_programme) ON DELETE CASCADE, " +
                "UNIQUE(id_utilisateur, id_programme)" +
                ")";
        try (Statement st = connection.createStatement()) {
            st.execute(query);
        } catch (SQLException e) {
            System.err.println("Erreur creation table favoris: " + e.getMessage());
        }
    }

    public void ajouterFavori(int idUser, int idProg) {
        String query = "INSERT IGNORE INTO favoris (id_utilisateur, id_programme) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idUser);
            ps.setInt(2, idProg);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimerFavori(int idUser, int idProg) {
        String query = "DELETE FROM favoris WHERE id_utilisateur = ? AND id_programme = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idUser);
            ps.setInt(2, idProg);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean estFavori(int idUser, int idProg) {
        String query = "SELECT COUNT(*) FROM favoris WHERE id_utilisateur = ? AND id_programme = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idUser);
            ps.setInt(2, idProg);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Programme> getProgrammesFavoris(int idUser) {
        List<Programme> favoris = new ArrayList<>();
        String query = "SELECT p.*, c.nom as cat_nom, c.description as cat_desc FROM programme p " +
                "JOIN favoris f ON p.id_programme = f.id_programme " +
                "JOIN categorie c ON p.id_categorie = c.id_categorie " +
                "WHERE f.id_utilisateur = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idUser);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Categorie c = new Categorie(rs.getInt("id_categorie"), rs.getString("cat_nom"), rs.getString("cat_desc"));
                Programme p = new Programme(
                        rs.getInt("id_programme"),
                        c,
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("image"),
                        rs.getString("video")
                );
                favoris.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return favoris;
    }
}
