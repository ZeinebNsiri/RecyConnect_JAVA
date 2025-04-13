package entities;

public class Article {
    private int id;
    private CategorieArticle categorie;
    private utilisateur utilisateur_id;
    private String nom_article;
    private String description_article;
    private int quantite_article;
    private double prix;
    private String image_article;
    private String localisation_article;

    public Article(int id, CategorieArticle categorie, utilisateur utilisateur_id, String nom_article, String description_article, int quantite_article, double prix, String image_article, String localisation_article) {
        this.id = id;
        this.categorie = categorie;
        this.utilisateur_id = utilisateur_id;
        this.nom_article = nom_article;
        this.description_article = description_article;
        this.quantite_article = quantite_article;
        this.prix = prix;
        this.image_article = image_article;
        this.localisation_article = localisation_article;
    }

    //const sans id


    public Article(CategorieArticle categorie, utilisateur utilisateur_id, String nom_article, String description_article, int quantite_article, double prix, String image_article, String localisation_article) {
        this.categorie = categorie;
        this.utilisateur_id = utilisateur_id;
        this.nom_article = nom_article;
        this.description_article = description_article;
        this.quantite_article = quantite_article;
        this.prix = prix;
        this.image_article = image_article;
        this.localisation_article = localisation_article;
    }

    public Article(){};

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom_article() {
        return nom_article;
    }

    public void setNom_article(String nom_article) {
        this.nom_article = nom_article;
    }

    public String getDescription_article() {
        return description_article;
    }

    public void setDescription_article(String description_article) {
        this.description_article = description_article;
    }

    public int getQuantite_article() {
        return quantite_article;
    }

    public void setQuantite_article(int quantite_article) {
        this.quantite_article = quantite_article;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getImage_article() {
        return image_article;
    }

    public void setImage_article(String image_article) {
        this.image_article = image_article;
    }

    public String getLocalisation_article() {
        return localisation_article;
    }

    public void setLocalisation_article(String localisation_article) {
        this.localisation_article = localisation_article;
    }

    public utilisateur getUtilisateur_id() {
        return utilisateur_id;
    }

    public void setUtilisateur_id(utilisateur utilisateur_id) {
        this.utilisateur_id = utilisateur_id;
    }

    public CategorieArticle getCategorie() {
        return categorie;
    }

    public void setCategorie(CategorieArticle categorie) {
        this.categorie = categorie;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", nom_article='" + nom_article + '\'' +
                ", description_article='" + description_article + '\'' +
                ", quantite_article=" + quantite_article +
                ", prix=" + prix +
                ", image_article='" + image_article + '\'' +
                ", localisation_article='" + localisation_article + '\'' +
                ", utilisateur_id=" + utilisateur_id +
                ", categorie=" + categorie +
                '}';
    }


}