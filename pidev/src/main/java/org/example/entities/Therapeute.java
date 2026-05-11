package org.example.entities;

import java.time.LocalDateTime;

public class Therapeute {
    private int idTherapeute;
    private String nom;
    private String prenom;
    private String image;
    private String certificat;
    private String specialite;
    private String email;
    private String telephone;
    private LocalDateTime dateInscription;

    public Therapeute() {}

    public Therapeute(String nom, String prenom, String image, String certificat,
                      String specialite, String email, String telephone) {
        this.nom = nom;
        this.prenom = prenom;
        this.image = image;
        this.certificat = certificat;
        this.specialite = specialite;
        this.email = email;
        this.telephone = telephone;
        this.dateInscription = LocalDateTime.now();
    }

    public Therapeute(int idTherapeute, String nom, String prenom, String image, String certificat,
                      String specialite, String email, String telephone, LocalDateTime dateInscription) {
        this.idTherapeute = idTherapeute;
        this.nom = nom;
        this.prenom = prenom;
        this.image = image;
        this.certificat = certificat;
        this.specialite = specialite;
        this.email = email;
        this.telephone = telephone;
        this.dateInscription = dateInscription;
    }

    public int getIdTherapeute() { return idTherapeute; }
    public void setIdTherapeute(int idTherapeute) { this.idTherapeute = idTherapeute; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getCertificat() { return certificat; }
    public void setCertificat(String certificat) { this.certificat = certificat; }
    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    @Override
    public String toString() {
        return nom + " " + prenom;
    }
}
