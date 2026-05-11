package org.example.entities;

import java.time.LocalDateTime;

public class Achat {
    private int idAchat;
    private int idAbonnement;
    private int idUtilisateur;
    private LocalDateTime dateAchat;
    private String statut;

    // Constructeurs
    public Achat() {}

    public Achat(int idAbonnement, int idUtilisateur) {
        this.idAbonnement = idAbonnement;
        this.idUtilisateur = idUtilisateur;
        this.statut = "actif";
        this.dateAchat = LocalDateTime.now();
    }

    public Achat(int idAchat, int idAbonnement, int idUtilisateur, LocalDateTime dateAchat, String statut) {
        this.idAchat = idAchat;
        this.idAbonnement = idAbonnement;
        this.idUtilisateur = idUtilisateur;
        this.dateAchat = dateAchat;
        this.statut = statut;
    }

    // Getters et Setters
    public int getIdAchat() { return idAchat; }
    public void setIdAchat(int idAchat) { this.idAchat = idAchat; }

    public int getIdAbonnement() { return idAbonnement; }
    public void setIdAbonnement(int idAbonnement) { this.idAbonnement = idAbonnement; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public LocalDateTime getDateAchat() { return dateAchat; }
    public void setDateAchat(LocalDateTime dateAchat) { this.dateAchat = dateAchat; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    @Override
    public String toString() {
        return "Achat{" +
                "idAchat=" + idAchat +
                ", idAbonnement=" + idAbonnement +
                ", idUtilisateur=" + idUtilisateur +
                ", dateAchat=" + dateAchat +
                ", statut='" + statut + '\'' +
                '}';
    }
}
