package controllers;

import entities.Article;
import entities.Commande;
import entities.LigneCommande;
import entities.utilisateur;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import services.CommandeService;
import utils.SessionPanier;
import services.LigneCommandeService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Panier {

    @FXML
    private VBox panierContainer;
    @FXML
    private Label totalLabel;
    @FXML
    private Button finaliserBtn;


    private LigneCommandeService ligneCommandeService = new LigneCommandeService();
    private CommandeService commandeService = new CommandeService();

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

        // Confirmation de la commande
        System.out.println("Commande validée !");

        // Rediriger l'utilisateur vers la page commande.fxml
        try {
            // Charger le fichier FXML de la page commande
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Commande.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène avec le fichier FXML
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));

            // Afficher la nouvelle scène
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Gérer l'exception (par exemple afficher un message d'erreur)
        }
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
                int stockDisponible = ligne.getArticle().getQuantite_article();
                int quantiteActuelle = ligne.getQuantite();

                if (quantiteActuelle < stockDisponible) {
                    ligne.setQuantite(quantiteActuelle + 1);
                    try {
                        ligneCommandeService.updateQuantite(ligne);
                        afficherPanier();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    // Affichage d'une alerte si la quantité max est atteinte
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                    alert.setTitle("Stock insuffisant");
                    alert.setHeaderText("Quantité maximale atteinte");
                    alert.setContentText("La quantité disponible en stock pour cet article est de " + stockDisponible + ".");
                    alert.showAndWait();
                }
            });


            HBox quantiteBox = new HBox(5, moinsBtn, quantiteLabel, plusBtn);

            double totalLigne = ligne.getQuantite() * ligne.getArticle().getPrix();
            Label totalLigneLabel = new Label(String.format("%.2f DT", totalLigne));
            totalLigneLabel.setPrefWidth(100);

            Button supprimerBtn = new Button("🗑");
            supprimerBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            supprimerBtn.setOnAction(e -> {
                // Supprimer de la base
                try {
                    ligneCommandeService.supprimerLigneCommande(ligne.getId());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    return;
                }

                // Supprimer du panier temporaire
                SessionPanier.supprimerArticle(ligne);

                // Rafraîchir l'affichage
                afficherPanier();
            });


            ligneHBox.getChildren().addAll(produitLabel, prixLabel, quantiteBox, totalLigneLabel, supprimerBtn);
            panierContainer.getChildren().add(ligneHBox);

            total += totalLigne;
        }

        totalLabel.setText(String.format("%.2f DT", total));
    }
    @FXML
    public void finaliserCommande(ActionEvent event) throws SQLException {
        utilisateur u = new utilisateur(
                1,                          // id
                "exemple@mail.com",         // email
                "Mnif",                     // nom_user
                "Sahar",                    // prenom
                "ROLE_CLIENT",              // roles
                "12345678",                 // num_tel
                "Tunis, Tunisie",           // adresse
                "motdepasse123",            // password
                true,                       // status
                "MF123456",                 // matricule_fiscale
                "photo.jpg"                 // photo_profil
        );


        // Créer une commande (il faut instancier la classe Commande, pas CommandeController)
        Commande commande = new Commande();
        commande.setDateCommande(LocalDateTime.now()); // Date actuelle
        commande.setStatut("En attente");  // Statut de la commande
        commande.setPrixTotal(SessionPanier.getTotalPanier()); // Prix total du panier

        // Ajouter la commande dans la base de données et récupérer l'ID
        int commandeId = commandeService.addCommande(commande); // Retourne l'ID de la commande insérée
        System.out.println("Commande ajoutée avec l'ID : " + commandeId);
        LigneCommandeService ligneCommandeService = new LigneCommandeService();
        List<LigneCommande> liste = ligneCommandeService.getLignesEnAttenteParUtilisateur(u.getId());
        // Associer chaque ligne de commande à cette commande
        for (LigneCommande ligne : liste) {
            System.out.println(ligne);
            ligne.setEtat("confirmée");
            ligneCommandeService.updateEtat(ligne); // Ajouter la ligne de commande à la base
        }

        // Redirection vers la page commande.fxml
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("commande.fxml"));
            Parent page = loader.load();
            Scene newScene = new Scene(page);
            Stage stage = (Stage) finaliserBtn.getScene().getWindow();
            stage.setScene(newScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Optionnellement, vider le panier après la commande
        SessionPanier.viderPanier();
    }
}

