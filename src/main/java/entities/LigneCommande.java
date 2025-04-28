package entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LigneCommande {

    private int id;
    private int quantite;
    private double prix;
    private String etat;
    private utilisateur utilisateur;
    private Article article;
    private LocalDateTime dateCommande;

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
    public String getNomClient() {
        return utilisateur != null ? utilisateur.getNom_user() : "";
    }

    public String getArticles() {
        return article != null ? article.getNom_article() : "";
    }

    public String getStatutCommande() {
        return etat; // ou autre champ si tu veux afficher le statut de la commande
    }

    public String getDateCommande() {
        return dateCommande != null ? dateCommande.toString() : "";
    }

    public void setDateCommande(LocalDateTime dateCommande) {
        this.dateCommande = dateCommande;
    }
    // Pour affichage formaté de la date
    public String getDateCommandeFormatted() {
        if (dateCommande == null) return "";
        return dateCommande.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private int commandeId;

    public int getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(int commandeId) {
        this.commandeId = commandeId;
    }



}
