package org.example.entities;

import java.time.LocalDateTime;

public class Avis {
    private int idAvis;
    private int idTherapeute;
    private int idUtilisateur;
    private int note; // 1 à 5
    private String commentaire;
    private LocalDateTime dateAvis;

    public Avis() {}

    public Avis(int idTherapeute, int idUtilisateur, int note, String commentaire) {
        this.idTherapeute = idTherapeute;
        this.idUtilisateur = idUtilisateur;
        this.note = note;
        this.commentaire = commentaire;
        this.dateAvis = LocalDateTime.now();
    }

    public int getIdAvis() { return idAvis; }
    public void setIdAvis(int idAvis) { this.idAvis = idAvis; }
    public int getIdTherapeute() { return idTherapeute; }
    public void setIdTherapeute(int idTherapeute) { this.idTherapeute = idTherapeute; }
    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }
    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public LocalDateTime getDateAvis() { return dateAvis; }
    public void setDateAvis(LocalDateTime dateAvis) { this.dateAvis = dateAvis; }
}
