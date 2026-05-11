package org.example.services;

import org.example.entities.ReservationSeance;
import org.example.entities.Seance;
import org.example.entities.Utilisateur;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationSeanceService {
    private Connection connection;
    private SeanceService seanceService = new SeanceService();
    private UtilisateurService utilisateurService = new UtilisateurService();

    public ReservationSeanceService() {
        this.connection = MyDataBase.getConnection();
    }

    public int ajouterReservation(ReservationSeance r) {
        String query = "INSERT INTO reservation (id_seance, id_utilisateur, date_reservation, statut) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getSeance().getIdSeance());
            ps.setInt(2, r.getUtilisateur().getIdUtilisateur());
            ps.setTimestamp(3, Timestamp.valueOf(r.getDateReservation()));
            ps.setString(4, r.getStatut());
            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                r.setIdReservation(id);
                
                // Décrémenter la capacité
                seanceService.regulerCapacite(r.getSeance().getIdSeance(), -1);
                
                System.out.println("Réservation de séance ajoutée avec succès!");
                return id;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la réservation de séance: " + e.getMessage());
        }
        return -1;
    }

    public List<ReservationSeance> getReservationsByUtilisateur(int idUtilisateur) {
        List<ReservationSeance> list = new ArrayList<>();
        String query = "SELECT * FROM reservation WHERE id_utilisateur = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idUtilisateur);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Seance seance = seanceService.getSeanceById(rs.getInt("id_seance"));
                Utilisateur user = utilisateurService.getUtilisateurById(rs.getInt("id_utilisateur"));
                list.add(new ReservationSeance(
                        rs.getInt("id_reservation"),
                        seance,
                        user,
                        rs.getTimestamp("date_reservation").toLocalDateTime(),
                        rs.getString("statut")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des réservations de séance: " + e.getMessage());
        }
        return list;
    }

    public void annulerReservation(int idReservation) {
        // Récupérer l'ID de la séance avant d'annuler pour restaurer la capacité
        String selectQuery = "SELECT id_seance, statut FROM reservation WHERE id_reservation = ?";
        int idSeance = -1;
        String oldStatut = "";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, idReservation);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                idSeance = rs.getInt("id_seance");
                oldStatut = rs.getString("statut");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (idSeance != -1 && !"annulée".equals(oldStatut)) {
            String query = "UPDATE reservation SET statut = 'annulée' WHERE id_reservation = ?";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, idReservation);
                ps.executeUpdate();
                
                // Incrémenter la capacité
                seanceService.regulerCapacite(idSeance, 1);
                
                System.out.println("Réservation de séance annulée.");
            } catch (SQLException e) {
                System.err.println("Erreur lors de l'annulation de la séance: " + e.getMessage());
            }
        }
    }

    public boolean aDejaReserve(int idSeance, int idUtilisateur) {
        String query = "SELECT COUNT(*) FROM reservation WHERE id_seance = ? AND id_utilisateur = ? AND statut != 'annulée'";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idSeance);
            ps.setInt(2, idUtilisateur);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<ReservationSeance> getAllReservations() {
        List<ReservationSeance> list = new ArrayList<>();
        String query = "SELECT * FROM reservation";
        try (Statement st = connection.createStatement()) {
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                Seance seance = seanceService.getSeanceById(rs.getInt("id_seance"));
                Utilisateur user = utilisateurService.getUtilisateurById(rs.getInt("id_utilisateur"));
                list.add(new ReservationSeance(
                        rs.getInt("id_reservation"),
                        seance,
                        user,
                        rs.getTimestamp("date_reservation").toLocalDateTime(),
                        rs.getString("statut")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void modifierStatut(int idReservation, String nouveauStatut) {
        String selectQuery = "SELECT id_seance, statut FROM reservation WHERE id_reservation = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, idReservation);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int idSeance = rs.getInt("id_seance");
                String ancienStatut = rs.getString("statut");

                if (!ancienStatut.equals(nouveauStatut)) {
                    if (!"annulée".equals(ancienStatut) && "annulée".equals(nouveauStatut)) {
                        seanceService.regulerCapacite(idSeance, 1);
                    }
                    else if ("annulée".equals(ancienStatut) && !"annulée".equals(nouveauStatut)) {
                        seanceService.regulerCapacite(idSeance, -1);
                    }

                    String updateQuery = "UPDATE reservation SET statut = ? WHERE id_reservation = ?";
                    try (PreparedStatement psUpdate = connection.prepareStatement(updateQuery)) {
                        psUpdate.setString(1, nouveauStatut);
                        psUpdate.setInt(2, idReservation);
                        psUpdate.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimerReservation(int idReservation) {
        String selectQuery = "SELECT id_seance, statut FROM reservation WHERE id_reservation = ?";
        try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
            ps.setInt(1, idReservation);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int idSeance = rs.getInt("id_seance");
                String statut = rs.getString("statut");

                if (!"annulée".equals(statut)) {
                    seanceService.regulerCapacite(idSeance, 1);
                }

                String deleteQuery = "DELETE FROM reservation WHERE id_reservation = ?";
                try (PreparedStatement psDelete = connection.prepareStatement(deleteQuery)) {
                    psDelete.setInt(1, idReservation);
                    psDelete.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}