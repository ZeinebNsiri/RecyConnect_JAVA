package entities;

public class CategorieCours {
    private int id;
    private String nomCategorie;
    private String descriptionCategorie;


    public CategorieCours() {
    }

    public CategorieCours(int id, String descriptionCategorie, String nomCategorie) {
        this.id = id;
        this.descriptionCategorie = descriptionCategorie;
        this.nomCategorie = nomCategorie;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomCategorie() {
        return nomCategorie;
    }

    public void setNomCategorie(String nomCategorie) {
        this.nomCategorie = nomCategorie;
    }

    public String getDescriptionCategorie() {
        return descriptionCategorie;
    }

    public void setDescriptionCategorie(String descriptionCategorie) {
        this.descriptionCategorie = descriptionCategorie;
    }

    @Override
    public String toString() {
        return "Categorie_cours{" +
                "id=" + id +
                ", nomCategorie='" + nomCategorie + '\'' +
                ", descriptionCategorie='" + descriptionCategorie + '\'' +
                '}';
    }
}
