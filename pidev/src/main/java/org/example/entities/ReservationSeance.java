package org.example.entities;

import java.time.LocalDateTime;

public class ReservationSeance {
    private int idReservation;
    private Seance seance;
    private Utilisateur utilisateur;
    private LocalDateTime dateReservation;
    private String statut;

    public ReservationSeance() {
    }

    public ReservationSeance(int idReservation, Seance seance, Utilisateur utilisateur, LocalDateTime dateReservation, String statut) {
        this.idReservation = idReservation;
        this.seance = seance;
        this.utilisateur = utilisateur;
        this.dateReservation = dateReservation;
        this.statut = statut;
    }

    public ReservationSeance(Seance seance, Utilisateur utilisateur, String statut) {
        this.seance = seance;
        this.utilisateur = utilisateur;
        this.dateReservation = LocalDateTime.now();
        this.statut = statut;
    }

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public Seance getSeance() {
        return seance;
    }

    public void setSeance(Seance seance) {
        this.seance = seance;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public LocalDateTime getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(LocalDateTime dateReservation) {
        this.dateReservation = dateReservation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "ReservationSeance{" +
                "idReservation=" + idReservation +
                ", seance=" + seance.getTitre() +
                ", utilisateur=" + utilisateur.getEmail() +
                ", dateReservation=" + dateReservation +
                ", statut='" + statut + '\'' +
                '}';
    }
}