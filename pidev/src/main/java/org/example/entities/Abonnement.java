package org.example.entities;

public class Abonnement {
    private int idAbonnement;
    private String nom;
    private String description;
    private double prix;
    private int dureeMois;

    public Abonnement() {}

    public Abonnement(int idAbonnement, String nom, String description, double prix, int dureeMois) {
        this.idAbonnement = idAbonnement;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.dureeMois = dureeMois;
    }

    public Abonnement(String nom, String description, double prix, int dureeMois) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.dureeMois = dureeMois;
    }

    public int getIdAbonnement() {
        return idAbonnement;
    }

    public void setIdAbonnement(int idAbonnement) {
        this.idAbonnement = idAbonnement;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getDureeMois() {
        return dureeMois;
    }

    public void setDureeMois(int dureeMois) {
        this.dureeMois = dureeMois;
    }

    @Override
    public String toString() {
        return "Abonnement{" +
                "idAbonnement=" + idAbonnement +
                ", nom='" + nom + '\'' +
                ", prix=" + prix +
                ", dureeMois=" + dureeMois +
                '}';
    }
}
