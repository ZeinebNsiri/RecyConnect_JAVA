package entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Commande {

    private int id;

    public Commande(double prixTotal, String statut, LocalDateTime dateCommande) {
        this.prixTotal = prixTotal;
        this.statut = statut;
        this.dateCommande = dateCommande;
    }

    private double prixTotal;
    private String statut;
    private LocalDateTime dateCommande;
    private List<LigneCommande> ligneCommandes;

    public Commande() {
        this.ligneCommandes = new ArrayList<>();
        this.dateCommande = LocalDateTime.now();
        this.statut = "Non payé";
    }

    public Commande(int id) {
        this.id = id;
        this.ligneCommandes = new ArrayList<>();
        this.dateCommande = LocalDateTime.now();
        this.statut = "Non payé";
    }

    public void addLigneCommande(LigneCommande ligne) {
        this.ligneCommandes.add(ligne);
    }

    public void calculerPrixTotal() {
        this.prixTotal = ligneCommandes.stream()
                .mapToDouble(l -> l.getPrix() * l.getQuantite())
                .sum();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(double prixTotal) {
        this.prixTotal = prixTotal;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDateTime dateCommande) {
        this.dateCommande = dateCommande;
    }

    public List<LigneCommande> getLigneCommandes() {
        return ligneCommandes;
    }

    public void setLigneCommandes(List<LigneCommande> ligneCommandes) {
        this.ligneCommandes = ligneCommandes;
    }
}