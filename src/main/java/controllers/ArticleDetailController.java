package controllers;

import entities.Article;
import entities.LigneCommande;
import entities.utilisateur;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import services.ArticleService;
import org.json.JSONArray;
import org.json.JSONObject;
import services.LigneCommandeService;
import utils.SessionPanier;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;

public class ArticleDetailController {

    @FXML private ImageView articleImage;
    @FXML private Label articleName;
    @FXML private Label articleCategory;
    @FXML private Label articleLocation;
    @FXML private Label priceLabel;
    @FXML private Label quantityLabel;
    @FXML private Text articleDescription;
    @FXML private Button commanderButton;
    @FXML private AnchorPane mapContainer;

    private final ArticleService articleService = new ArticleService();
    private LigneCommandeService ligneCommandeService = new LigneCommandeService();
    public void loadArticleData(int articleId) {
        try {
            Article article = articleService.getArticleById(articleId);
            if (article == null) return;

            articleName.setText(article.getNom_article());
            articleCategory.setText(articleService.getCategorieById(article.getCategorie_id()).getNom_categorie());
            articleLocation.setText(article.getLocalisation_article());
            articleDescription.setText(article.getDescription_article());
            priceLabel.setText(article.getPrix() + " TN/Kg");
            quantityLabel.setText(article.getQuantite_article() + " Kg");

            int ownerId = article.getUtilisateur_id();
            utilisateur user = utils.Session.getInstance().getCurrentUser();
            int currentUserId = utils.Session.getInstance().getCurrentUser().getId(); // à adapter si nom de méthode

            if (ownerId == currentUserId) {
                commanderButton.setText("Liste des commandes");
                commanderButton.setOnAction(e -> showOrders());
            } else {
                commanderButton.setText("Commander");
                commanderButton.setOnAction(event -> {
                    // Création de la ligne de commande avec l'article sélectionné
                    LigneCommande ligne = new LigneCommande();
                    ligne.setArticle(article);
                    ligne.setQuantite(1);  // Quantité par défaut
                    ligne.setPrix(ligne.getArticle().getPrix());
                    ligne.setEtat("En attente"); // Par exemple, "En attente" ou un autre état selon ta logique
                    ligne.setUtilisateur(user); // L'utilisateur actuel

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
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseUser.fxml"));
                        Parent root = loader.load();
                        BaseUserController baseUserController = loader.getController();
                        baseUserController.panier();
                        priceLabel.getScene().setRoot(root);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });
            }


            String imagePath = "file:/C:/Users/azizz/OneDrive/Bureau/Recyconnect/public/uploads/photo_dir/" + article.getImage_article();
            articleImage.setImage(new Image(imagePath));

            String localisation = article.getLocalisation_article();
            String apiUrl = "https://nominatim.openstreetmap.org/search?format=json&countrycodes=TN&q=" + URLEncoder.encode(localisation, "UTF-8");
            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestProperty("User-Agent", "JavaFX-App");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            JSONArray results = new JSONArray(response.toString());
            if (!results.isEmpty()) {
                JSONObject result = results.getJSONObject(0);
                double lat = result.getDouble("lat");
                double lon = result.getDouble("lon");
                showMap(lat, lon, localisation);
                System.out.println(">> Carte insérée avec succès.");
            } else {
                System.out.println("Localisation introuvable : " + localisation);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMap(double lat, double lon, String name) {
        javafx.application.Platform.runLater(() -> {
            SwingNode swingNode = new SwingNode();
            SwingUtilities.invokeLater(() -> {
                MapViewerController mapViewer = new MapViewerController();
                JPanel mapPanel = mapViewer.createMapPanel(lat, lon, name);

                javafx.application.Platform.runLater(() -> {
                    swingNode.setContent(mapPanel);

                    // Nettoyer le conteneur et insérer le SwingNode
                    mapContainer.getChildren().clear();
                    mapContainer.getChildren().add(swingNode);

                    // Lier le SwingNode aux bords pour qu’il prenne toute la place
                    AnchorPane.setTopAnchor(swingNode, 0.0);
                    AnchorPane.setBottomAnchor(swingNode, 0.0);
                    AnchorPane.setLeftAnchor(swingNode, 0.0);
                    AnchorPane.setRightAnchor(swingNode, 0.0);
                });
            });
        });
    }


    @FXML
    private void addToCart() {
        System.out.println("Article ajouté au panier.");
    }
    private void showOrders() {
        System.out.println("Redirection vers la liste des commandes...");
        // Tu peux ici charger une nouvelle vue FXML ou afficher une liste contextuelle
    }

}
