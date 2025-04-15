package services;

import entities.Article;
import entities.LigneCommande;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LigneCommandeService {

    Connection conx;

    public LigneCommandeService() {
        conx = MyDataBase.getInstance().getConx();
    }

    // Méthode pour ajouter une ligne dans la table ligne_commande
    public void addLigneCommande(LigneCommande ligneCommande) throws SQLException {
        String query = "INSERT INTO `ligne_commande`( `user_c_id`, `article_c_id`, `quantite_c`, `prix_c`, `etat_c`) VALUES ( ?, ?, ?, ?, ?)";
        PreparedStatement ps = conx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

        // Calcul du total (quantité * prix unitaire)
        double total = ligneCommande.getQuantite() * ligneCommande.getPrix();

        // Préparation de la requête d'insertion

        ps.setInt(1, ligneCommande.getUtilisateur().getId()); // user_c_id
        ps.setInt(2, ligneCommande.getArticle().getId()); // article_c_id
        ps.setInt(3, ligneCommande.getQuantite()); // quantite_c
        ps.setDouble(4, ligneCommande.getPrix()); // prix_c
        ps.setString(5, ligneCommande.getEtat()); // etat_c (tu devras peut-être avoir un état spécifique ou une valeur comme "en cours")

        // Débogage - afficher les valeurs avant l'exécution
        System.out.println("Ajout de ligne commande avec : ");
        System.out.println("Article ID: " + ligneCommande.getArticle().getId());
        System.out.println("Utilisateur ID: " + ligneCommande.getUtilisateur().getId());
        System.out.println("Quantité: " + ligneCommande.getQuantite());
        System.out.println("Prix: " + ligneCommande.getPrix());
        System.out.println("État: " + ligneCommande.getEtat());

        // Lancer l'insertion dans la base de données
        int rowsAffected = ps.executeUpdate();
        if (rowsAffected > 0) {
            // Si l'insertion est réussie, récupérer l'ID généré
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                ligneCommande.setId(generatedId); // Mise à jour de l'objet ligneCommande avec l'ID généré
            }
            System.out.println("Ligne de commande ajoutée avec succès!");
        } else {
            System.out.println("Aucune ligne de commande ajoutée.");
        }
    }

    public void updateQuantite(LigneCommande ligne) throws SQLException {
        String query = "UPDATE ligne_commande SET quantite_c = ? WHERE id = ?";
        try (PreparedStatement ps = conx.prepareStatement(query)) {
            ps.setInt(1, ligne.getQuantite());
            ps.setInt(2, ligne.getId());  // Assure-toi que ton objet LigneCommande a bien l'id assigné
            ps.executeUpdate();
        }
    }

}
