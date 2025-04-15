package controllers;

import entities.Article;
import entities.LigneCommande;
import entities.utilisateur;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.LigneCommandeService;
import utils.MyDataBase;
import utils.SessionPanier;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class ArticlePanier implements Initializable {
    @FXML
    private VBox articleContainer;
    private LigneCommandeService ligneCommandeService = new LigneCommandeService();
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadArticlesFromDB();
    }

    private void loadArticlesFromDB() {
        Connection conx = MyDataBase.getInstance().getConx();
        String req = "SELECT * FROM article";

        try {
            Statement stmt = conx.createStatement();
            ResultSet rs = stmt.executeQuery(req);

            while (rs.next()) {
                Article article = new Article();
                article.setId(rs.getInt("id"));
                article.setNom_article(rs.getString("nom_article"));
                article.setDescription_article(rs.getString("description_article"));
                article.setPrix(rs.getDouble("prix"));
                article.setQuantite_article(rs.getInt("quantite_article"));
                article.setImage_article(rs.getString("image_article"));
                article.setLocalisation_article(rs.getString("localisation_article"));

                HBox articleCard = createStyledArticleBox(article);
                articleContainer.getChildren().add(articleCard);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createStyledArticleBox(Article article) {
        HBox box = new HBox(20);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.3, 0, 2);");

        Label nameLabel = new Label("🛒 Nom: " + article.getNom_article());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label descLabel = new Label("📋 " + article.getDescription_article());
        Label priceLabel = new Label("💵 Prix: " + article.getPrix() + " TND");
        Label quantityLabel = new Label("📦 Quantité: " + article.getQuantite_article());

        VBox detailsBox = new VBox(5, nameLabel, descLabel, priceLabel, quantityLabel);
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

        Button commanderButton = new Button("Commander");
        commanderButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");

        // Ajout de l'article au panier
        commanderButton.setOnAction(event -> {
            // Création de la ligne de commande avec l'article sélectionné
            LigneCommande ligne = new LigneCommande();
            ligne.setArticle(article);
            ligne.setQuantite(1);  // Quantité par défaut
            ligne.setPrix(ligne.getArticle().getPrix());
            ligne.setEtat("En attente"); // Par exemple, "En attente" ou un autre état selon ta logique
            ligne.setUtilisateur(u); // L'utilisateur actuel

            // Ajouter la ligne de commande dans la base de données
            try {
                ligneCommandeService.addLigneCommande(ligne);
                System.out.println("Ligne de commande ajoutée pour l'article : " + ligne.getArticle().getNom_article());
            } catch (SQLException e) {
                e.printStackTrace();
                // Gérer l'exception (par exemple afficher un message d'erreur)
            }
            // Ajouter l'article au panier (SessionPanier)
            SessionPanier.ajouterArticle(ligne);
            System.out.println("Article ajouté au panier : " + article.getNom_article());

            // Redirection vers la page Panier
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Panier.fxml"));
                Parent page = loader.load();

                Scene newScene = new Scene(page);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(newScene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }

    });


        // Ajouter un espace flexible pour espacer le bouton
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Ajouter les éléments dans la boîte (details + bouton commander)
        box.getChildren().addAll(detailsBox, spacer, commanderButton);
        return box;
    }}


