package org.example.entities;

public class Programme {
    private int idProgramme;
    private Categorie categorie; // Relation Many-to-One
    private String titre;
    private String description;
    private String image;
    private String video;

    public Programme() {
    }

    public Programme(Categorie categorie, String titre, String description, String image, String video) {
        this.categorie = categorie;
        this.titre = titre;
        this.description = description;
        this.image = image;
        this.video = video;
    }

    public Programme(int idProgramme, Categorie categorie, String titre, String description, String image, String video) {
        this.idProgramme = idProgramme;
        this.categorie = categorie;
        this.titre = titre;
        this.description = description;
        this.image = image;
        this.video = video;
    }

    public int getIdProgramme() {
        return idProgramme;
    }

    public void setIdProgramme(int idProgramme) {
        this.idProgramme = idProgramme;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    @Override
    public String toString() {
        return "Programme{" +
                "idProgramme=" + idProgramme +
                ", categorie=" + (categorie != null ? categorie.getNom() : "N/A") +
                ", titre='" + titre + '\'' +
                '}';
    }
}
