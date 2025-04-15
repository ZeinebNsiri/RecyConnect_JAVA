package utils;

import entities.LigneCommande;
import java.util.ArrayList;
import java.util.List;

public class SessionPanier {
    private static final List<LigneCommande> panier = new ArrayList<>();

    public static void ajouterArticle(LigneCommande article) {
        panier.add(article);
    }

    public static List<LigneCommande> getPanier() {
        return panier;
    }

    public static void viderPanier() {
        panier.clear();
    }
    public static void supprimerArticle(LigneCommande ligne) {
        panier.removeIf(l -> l.getArticle().getId() == ligne.getArticle().getId());
    }

    public static double getTotalPanier() {
        double total = 0;
        for (LigneCommande ligne : panier) {
            total += ligne.getPrix() * ligne.getQuantite();
        }
        return total;
    }

}
