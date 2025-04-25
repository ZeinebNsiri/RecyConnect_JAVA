package controllers;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONObject;
import services.ArticleService;
import entities.Article;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ArticleDetailController {

    @FXML private ImageView articleImage;
    @FXML private Label articleName;
    @FXML private Label articleCategory;
    @FXML private Label articleLocation;
    @FXML private Label priceLabel;
    @FXML private Label quantityLabel;
    @FXML private Label articleDescription;
    @FXML private Button orderButton;
    @FXML private WebView mapView;

    private final ArticleService articleService = new ArticleService();

    public void loadArticleData(int articleId) {
        try {
            Article article = articleService.getArticleById(articleId);
            if (article == null) return;

            // Données article
            articleName.setText(article.getNom_article());
            articleCategory.setText(articleService.getCategorieById(article.getCategorie_id()).getNom_categorie());
            articleLocation.setText(article.getLocalisation_article());
            articleDescription.setText(article.getDescription_article());
            priceLabel.setText(article.getPrix() + " TN/Kg");
            quantityLabel.setText(article.getQuantite_article() + " Kg");

            // ✅ Affichage image
            String imagePath = "C:/Users/Admin/Desktop/PI_RecyConnect_TechSquad/public/uploads/photo_dir/" + article.getImage_article();
            articleImage.setImage(new Image(imagePath));

            // ✅ Chargement de la carte dans le WebView
            String htmlPath = getClass().getResource("/map.html").toExternalForm(); // 🔥 assure-toi que `map.html` est bien dans `resources/`
            WebEngine webEngine = mapView.getEngine();
            // ✅ Fixer la taille de la carte (agrandir pour éviter le bug de découpage)
            mapView.setPrefSize(1000, 500); // Largeur x Hauteur
            mapView.setMinSize(1000, 500);
            mapView.setMaxSize(1000, 500);
            webEngine.load(htmlPath);

            // Geocodage Nominatim
            String localisation = article.getLocalisation_article();
            String apiUrl = "https://nominatim.openstreetmap.org/search?format=json&countrycodes=TN&q=" +
                    URLEncoder.encode(localisation, "UTF-8");

            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestProperty("User-Agent", "JavaFX-App");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            JSONArray results = new JSONArray(response.toString());
            if (!results.isEmpty()) {
                JSONObject result = results.getJSONObject(0);
                double lat = result.getDouble("lat");
                double lon = result.getDouble("lon");

                webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        JSObject window = (JSObject) webEngine.executeScript("window");
                        window.call("initMap", lat, lon, localisation);
                    }
                });
            } else {
                System.out.println("❌ Localisation introuvable : " + localisation);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addToCart() {
        System.out.println("Article ajouté au panier.");
    }
}
