package entities;

import java.util.List;

public class CategorieArticle {

    private int id;
    private String nom_categorie;
    private String image_categorie;
    private String description_categorie;

    public CategorieArticle(int id, String nom_categorie, String description_categorie, String image_categorie) {
        this.id = id;
        this.nom_categorie = nom_categorie;
        this.description_categorie = description_categorie;
        this.image_categorie = image_categorie;

    }

    public CategorieArticle(String nom_categorie, String description_categorie, String image_categorie) {
        this.nom_categorie = nom_categorie;
        this.description_categorie = description_categorie;
        this.image_categorie = image_categorie;

    }

    public CategorieArticle() {};


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom_categorie() {
        return nom_categorie;
    }

    public void setNom_categorie(String nom_categorie) {
        this.nom_categorie = nom_categorie;
    }

    public String getImage_categorie() {
        return image_categorie;
    }

    public void setImage_categorie(String image_categorie) {
        this.image_categorie = image_categorie;
    }

    public String getDescription_categorie() {
        return description_categorie;
    }

    public void setDescription_categorie(String description_categorie) {
        this.description_categorie = description_categorie;
    }
}