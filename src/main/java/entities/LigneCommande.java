package entities;

public class LigneCommande {

    private int id;
    private int quantite;
    private double prix;
    private String etat;
    private utilisateur utilisateur;
    private Article article;

    public LigneCommande() {}

    public LigneCommande(int id, int quantite, double prix, String etat, utilisateur utilisateur, Article article) {
        this.id = id;
        this.quantite = quantite;
        this.prix = prix;
        this.etat = etat;
        this.utilisateur = utilisateur;
        this.article = article;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }
}
