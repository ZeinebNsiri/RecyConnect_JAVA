package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import services.ArticleService;
import entities.Article;

public class ArticleDetailController {

    @FXML
    private ImageView articleImage;
    @FXML
    private Label articleName;
    @FXML
    private Label articleCategory;
    @FXML
    private Label articleLocation;
    @FXML
    private Label priceLabel;
    @FXML
    private Label quantityLabel;
    @FXML
    private Button orderButton;
    @FXML
    private Label articleDescription;

    private final ArticleService articleService = new ArticleService();

    // Method to load article data dynamically using the article ID
    public void loadArticleData(int articleId) {
        try {
            // Fetch the article by ID using the service method
            Article article = articleService.getArticleById(articleId);

            if (article != null) {
                articleName.setText(article.getNom_article());
                articleCategory.setText( articleService.getCategorieById(article.getCategorie_id()).getNom_categorie());
                articleLocation.setText( article.getLocalisation_article());
                articleDescription.setText(article.getDescription_article());
                priceLabel.setText(article.getPrix() + " TN/Kg");
                quantityLabel.setText(article.getQuantite_article() + " Kg");

                // Load the image
                String imagePath = "C:/Users/Admin/Desktop/PI_RecyConnect_TechSquad/public/uploads/photo_dir/" + article.getImage_article(); // Replace with actual path
                articleImage.setImage(new Image(imagePath));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Action for the "Ajouter au panier" button
    @FXML
    private void addToCart() {
        // Implement cart functionality (e.g., add article to user's cart)
        System.out.println("Article added to cart.");
    }
}