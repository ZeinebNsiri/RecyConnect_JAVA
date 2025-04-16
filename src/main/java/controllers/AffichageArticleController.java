package controllers;

import entities.Article;
import entities.CategorieArticle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import services.ArticleService;
import services.CateArtService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AffichageArticleController {

    @FXML
    private FlowPane categoryBar;

    @FXML
    private GridPane articleGrid;

    private final ArticleService articleService = new ArticleService();
    private final CateArtService cateArtService = new CateArtService();

    @FXML
    private Button proposerArticleBtn; // ajoute fx:id dans FXML

    @FXML
    public void initialize() {
        try {
            loadCategories();
            loadArticles();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //  Redirection vers le formulaire
        proposerArticleBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/formAjoutArticle.fxml"));
                Parent formView = loader.load();
                BorderPane root = (BorderPane) proposerArticleBtn.getScene().lookup("#rootBorderPane");
                root.setCenter(formView);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void loadCategories() throws SQLException {
        categoryBar.getChildren().clear();

        Label all = new Label("Tous les articles");
        all.setStyle("-fx-text-fill: green; -fx-underline: true; -fx-font-size: 15px; -fx-font-weight: bold;");
        all.setOnMouseClicked(e -> {
            try {
                loadArticles();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        categoryBar.getChildren().add(all);

        for (CategorieArticle c : cateArtService.displayList()) {
            Label cat = new Label(c.getNom_categorie());
            cat.setStyle("-fx-text-fill: #2f4f2f; -fx-font-size: 14px; -fx-cursor: hand;");
            cat.setOnMouseClicked(e -> {
                try {
                    loadArticlesByCategory(c.getId());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });
            categoryBar.getChildren().add(cat);
        }
    }

    private void loadArticles() throws SQLException {
        articleGrid.getChildren().clear();
        List<Article> articles = articleService.displayList();

        int column = 0;
        int row = 0;
        for (Article a : articles) {
            StackPane card = createArticleCard(a);
            articleGrid.add(card, column, row);
            column++;
            if (column == 3) {
                column = 0;
                row++;
            }
        }
    }

    private void loadArticlesByCategory(int categoryId) throws SQLException {
        articleGrid.getChildren().clear();
        int column = 0;
        int row = 0;
        for (Article a : articleService.displayList()) {
            if (a.getCategorie_id() == categoryId) {
                StackPane card = createArticleCard(a);
                articleGrid.add(card, column, row);
                column++;
                if (column == 3) {
                    column = 0;
                    row++;
                }
            }
        }
    }

    private StackPane createArticleCard(Article article) {
        StackPane card = new StackPane();
        card.setPrefSize(400, 320);
        card.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #ccc; " +
                "-fx-border-radius: 10px; " +
                "-fx-background-radius: 10px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        VBox content = new VBox();
        content.setSpacing(10);
        content.setPadding(new Insets(0));

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitHeight(180);
        imageView.setFitWidth(400);
        imageView.setPreserveRatio(false);
        imageView.setImage(new Image(getClass().getResource("/carousel-1.jpg").toExternalForm()));

        // Nom + bouton
        HBox topLine = new HBox();
        topLine.setAlignment(Pos.CENTER_LEFT);
        topLine.setPadding(new Insets(10, 15, 0, 15));
        topLine.setSpacing(10);

        Label name = new Label(article.getNom_article());
        name.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button detailsBtn = new Button("Details");
        detailsBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 15px;");
        detailsBtn.setPrefWidth(100);
        detailsBtn.setPrefHeight(35);

        topLine.getChildren().addAll(name, spacer, detailsBtn);

        // Infos
        Label info = new Label("PU: " + article.getPrix() + " TN/Kg | " + article.getQuantite_article() + " Kg");
        info.setStyle("-fx-font-size: 15px;");
        info.setPadding(new Insets(0, 15, 15, 15));

        content.getChildren().addAll(imageView, topLine, info);
        card.getChildren().add(content);

        return card;
    }
}
