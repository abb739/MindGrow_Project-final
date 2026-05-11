package org.example.services;

import org.example.entities.Avis;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AvisService {

    private final Connection connection;

    public AvisService() {
        this.connection = MyDataBase.getConnection();
    }

    public void ajouterAvis(Avis avis) {
        String query = "INSERT INTO avis (id_therapeute, id_utilisateur, note, commentaire) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, avis.getIdTherapeute());
            ps.setInt(2, avis.getIdUtilisateur());
            ps.setInt(3, avis.getNote());
            ps.setString(4, avis.getCommentaire());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur ajout avis : " + e.getMessage());
        }
    }

    public List<Avis> getAvisParTherapeute(int idTherapeute) {
        List<Avis> list = new ArrayList<>();
        String query = "SELECT * FROM avis WHERE id_therapeute = ? ORDER BY date_avis DESC";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idTherapeute);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Avis a = new Avis();
                a.setIdAvis(rs.getInt("id_avis"));
                a.setIdTherapeute(rs.getInt("id_therapeute"));
                a.setIdUtilisateur(rs.getInt("id_utilisateur"));
                a.setNote(rs.getInt("note"));
                a.setCommentaire(rs.getString("commentaire"));
                Timestamp ts = rs.getTimestamp("date_avis");
                if (ts != null)
                    a.setDateAvis(ts.toLocalDateTime());
                list.add(a);
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération avis : " + e.getMessage());
        }
        return list;
    }

    public List<AvisDetail> getAvisDetailsParTherapeute(int idTherapeute) {
        List<AvisDetail> list = new ArrayList<>();
        String query = "SELECT a.*, u.nom, u.prenom FROM avis a " +
                "JOIN utilisateur u ON a.id_utilisateur = u.id_utilisateur " +
                "WHERE a.id_therapeute = ? ORDER BY a.date_avis DESC";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idTherapeute);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                AvisDetail ad = new AvisDetail();
                ad.setIdAvis(rs.getInt("id_avis"));
                ad.setIdTherapeute(rs.getInt("id_therapeute"));
                ad.setIdUtilisateur(rs.getInt("id_utilisateur"));
                ad.setNote(rs.getInt("note"));
                ad.setCommentaire(rs.getString("commentaire"));
                Timestamp ts = rs.getTimestamp("date_avis");
                if (ts != null)
                    ad.setDateAvis(ts.toLocalDateTime());
                ad.setNomUtilisateur(rs.getString("nom"));
                ad.setPrenomUtilisateur(rs.getString("prenom"));
                list.add(ad);
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération détails avis : " + e.getMessage());
        }
        return list;
    }

    public static class AvisDetail extends Avis {
        private String nomUtilisateur;
        private String prenomUtilisateur;

        public String getNomUtilisateur() {
            return nomUtilisateur;
        }

        public void setNomUtilisateur(String nomUtilisateur) {
            this.nomUtilisateur = nomUtilisateur;
        }

        public String getPrenomUtilisateur() {
            return prenomUtilisateur;
        }

        public void setPrenomUtilisateur(String prenomUtilisateur) {
            this.prenomUtilisateur = prenomUtilisateur;
        }
    }

    public double getMoyenneNote(int idTherapeute) {
        String query = "SELECT AVG(note) as moyenne FROM avis WHERE id_therapeute = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idTherapeute);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("moyenne");
            }
        } catch (SQLException e) {
            System.err.println("Erreur calcul moyenne : " + e.getMessage());
        }
        return 0.0;
    }

    public int getNombreAvis(int idTherapeute) {
        String query = "SELECT COUNT(*) as total FROM avis WHERE id_therapeute = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idTherapeute);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt("total");
        } catch (SQLException e) {
            System.err.println("Erreur count avis : " + e.getMessage());
        }
        return 0;
    }

    public void modifierAvis(Avis avis) {
        String query = "UPDATE avis SET note = ?, commentaire = ?, date_avis = CURRENT_TIMESTAMP WHERE id_avis = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, avis.getNote());
            ps.setString(2, avis.getCommentaire());
            ps.setInt(3, avis.getIdAvis());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur modification avis : " + e.getMessage());
        }
    }

    public void supprimerAvis(int idAvis) {
        String query = "DELETE FROM avis WHERE id_avis = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idAvis);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur suppression avis : " + e.getMessage());
        }
    }
}
