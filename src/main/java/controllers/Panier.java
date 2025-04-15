package controllers;

import entities.Article;
import entities.LigneCommande;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.Button;
import utils.SessionPanier;
import services.LigneCommandeService;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class Panier {

    @FXML
    private VBox panierContainer;
    @FXML
    private Label totalLabel;

    private LigneCommandeService ligneCommandeService = new LigneCommandeService();

    @FXML
    public void initialize() {
        afficherPanier();
    }

    @FXML
    private void viderPanier(ActionEvent event) {
        SessionPanier.viderPanier();
        afficherPanier();
    }

    @FXML
    private void commander(ActionEvent event) {
        // Récupération des articles dans le panier
        for (LigneCommande ligne : SessionPanier.getPanier()) {
            // Créer la ligne de commande pour chaque article
            LigneCommande ligneCommande = new LigneCommande();
            ligneCommande.setArticle(ligne.getArticle());
            ligneCommande.setQuantite(ligne.getQuantite());
            ligneCommande.setPrix(ligne.getArticle().getPrix());
            ligneCommande.setEtat("En attente"); // Par exemple, "En attente" ou un autre état selon ta logique
            ligneCommande.setUtilisateur(ligne.getUtilisateur()); // L'utilisateur actuel

            // Ajouter la ligne de commande dans la base de données
            try {
                ligneCommandeService.addLigneCommande(ligneCommande);
                System.out.println("Ligne de commande ajoutée pour l'article : " + ligne.getArticle().getNom_article());
            } catch (SQLException e) {
                e.printStackTrace();
                // Gérer l'exception (par exemple afficher un message d'erreur)
            }
        }

        // Confirmation ou redirection après la commande
        System.out.println("Commande validée !");
        // Optionnellement, tu peux rediriger l'utilisateur ou afficher un message de succès
    }


    public void afficherPanier() {
        panierContainer.getChildren().clear();

        double total = 0;

        for (LigneCommande ligne : SessionPanier.getPanier()) {
            HBox ligneHBox = new HBox(30);
            ligneHBox.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 10;");

            Label produitLabel = new Label(ligne.getArticle().getNom_article());
            produitLabel.setPrefWidth(250);

            Label prixLabel = new Label(String.format("%.2f DT", ligne.getArticle().getPrix()));
            prixLabel.setPrefWidth(100);

            Label quantiteLabel = new Label(String.valueOf(ligne.getQuantite()));
            quantiteLabel.setPrefWidth(50);

            Button moinsBtn = new Button("-");
            Button plusBtn = new Button("+");

            moinsBtn.setOnAction(e -> {
                if (ligne.getQuantite() > 1) {
                    ligne.setQuantite(ligne.getQuantite() - 1);
                    try {
                        ligneCommandeService.updateQuantite(ligne); // ⬅️ fonction à créer
                        afficherPanier();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            plusBtn.setOnAction(e -> {
                ligne.setQuantite(ligne.getQuantite() + 1);
                try {
                    ligneCommandeService.updateQuantite(ligne); // ⬅️ fonction à créer
                    afficherPanier();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

            HBox quantiteBox = new HBox(5, moinsBtn, quantiteLabel, plusBtn);

            double totalLigne = ligne.getQuantite() * ligne.getArticle().getPrix();
            Label totalLigneLabel = new Label(String.format("%.2f DT", totalLigne));
            totalLigneLabel.setPrefWidth(100);

            Button supprimerBtn = new Button("🗑");
            supprimerBtn.setOnAction(e -> {
                SessionPanier.supprimerArticle(ligne);
                afficherPanier();
            });

            ligneHBox.getChildren().addAll(produitLabel, prixLabel, quantiteBox, totalLigneLabel, supprimerBtn);
            panierContainer.getChildren().add(ligneHBox);

            total += totalLigne;
        }

        totalLabel.setText(String.format("%.2f DT", total));
    }

}
