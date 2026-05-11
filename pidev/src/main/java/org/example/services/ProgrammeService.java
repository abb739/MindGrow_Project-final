package org.example.services;

import org.example.entities.Categorie;
import org.example.entities.Programme;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProgrammeService {
    private Connection connection;

    public ProgrammeService() {
        this.connection = MyDataBase.getConnection();
    }

    public void ajouterProgramme(Programme p) {
        String query = "INSERT INTO programme (id_categorie, titre, description, image, video) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, p.getCategorie().getIdCategorie());
            ps.setString(2, p.getTitre());
            ps.setString(3, p.getDescription());
            ps.setString(4, p.getImage());
            ps.setString(5, p.getVideo());
            ps.executeUpdate();
            System.out.println("Programme ajouté !");
        } catch (SQLException e) {
            System.err.println("Erreur d'ajout programme : " + e.getMessage());
        }
    }

    public List<Programme> afficherProgrammes() {
        List<Programme> programmes = new ArrayList<>();
        // Jointure pour récupérer le nom de la catégorie en une seule fois
        String query = "SELECT p.*, c.nom as nom_categorie, c.description as desc_categorie " +
                       "FROM programme p " +
                       "JOIN categorie c ON p.id_categorie = c.id_categorie";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Categorie cat = new Categorie(
                        rs.getInt("id_categorie"),
                        rs.getString("nom_categorie"),
                        rs.getString("desc_categorie")
                );
                programmes.add(new Programme(
                        rs.getInt("id_programme"),
                        cat,
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("image"),
                        rs.getString("video")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur affichage programmes : " + e.getMessage());
        }
        return programmes;
    }

    public void modifierProgramme(Programme p) {
        String query = "UPDATE programme SET id_categorie = ?, titre = ?, description = ?, image = ?, video = ? WHERE id_programme = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, p.getCategorie().getIdCategorie());
            ps.setString(2, p.getTitre());
            ps.setString(3, p.getDescription());
            ps.setString(4, p.getImage());
            ps.setString(5, p.getVideo());
            ps.setInt(6, p.getIdProgramme());
            ps.executeUpdate();
            System.out.println("Programme modifié !");
        } catch (SQLException e) {
            System.err.println("Erreur modification programme : " + e.getMessage());
        }
    }

    public void supprimerProgramme(int id) {
        String query = "DELETE FROM programme WHERE id_programme = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Programme supprimé !");
        } catch (SQLException e) {
            System.err.println("Erreur suppression programme : " + e.getMessage());
        }
    }
}
