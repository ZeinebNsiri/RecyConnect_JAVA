package entities;

public class CategorieCours {
    private int id;
    private String nomCategorie;
    private String descriptionCategorie;


    public CategorieCours() {
    }

    public CategorieCours(int id, String nomCategorie, String descriptionCategorie) {
        this.id = id;
        this.nomCategorie = nomCategorie;
        this.descriptionCategorie = descriptionCategorie;

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
