package org.example.services;

import org.example.entities.Categorie;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieService {
    private Connection connection;

    public CategorieService() {
        this.connection = MyDataBase.getConnection();
    }

    public void ajouterCategorie(Categorie c) {
        String query = "INSERT INTO categorie (nom, description) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getDescription());
            ps.executeUpdate();
            System.out.println("Catégorie ajoutée !");
        } catch (SQLException e) {
            System.err.println("Erreur d'ajout catégorie : " + e.getMessage());
        }
    }

    public List<Categorie> afficherCategories() {
        List<Categorie> categories = new ArrayList<>();
        String query = "SELECT * FROM categorie";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                categories.add(new Categorie(
                        rs.getInt("id_categorie"),
                        rs.getString("nom"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur affichage catégories : " + e.getMessage());
        }
        return categories;
    }

    public void modifierCategorie(Categorie c) {
        String query = "UPDATE categorie SET nom = ?, description = ? WHERE id_categorie = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getDescription());
            ps.setInt(3, c.getIdCategorie());
            ps.executeUpdate();
            System.out.println("Catégorie modifiée !");
        } catch (SQLException e) {
            System.err.println("Erreur modification catégorie : " + e.getMessage());
        }
    }

    public void supprimerCategorie(int id) {
        String query = "DELETE FROM categorie WHERE id_categorie = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Catégorie supprimée !");
        } catch (SQLException e) {
            System.err.println("Erreur suppression catégorie : " + e.getMessage());
        }
    }

    public Categorie getCategorieById(int id) {
        String query = "SELECT * FROM categorie WHERE id_categorie = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Categorie(
                        rs.getInt("id_categorie"),
                        rs.getString("nom"),
                        rs.getString("description")
                );
            }
        } catch (SQLException e) {
            System.err.println("Erreur récup catégorie : " + e.getMessage());
        }
        return null;
    }
}
