package controllers;

import entities.Article;
import entities.CategorieArticle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import services.ArticleService;
import services.CateArtService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UserArticle {

    @FXML
    private FlowPane categoryBar;

    @FXML
    private GridPane articleGrid;

    private final ArticleService articleService = new ArticleService();
    private final CateArtService cateArtService = new CateArtService();

    @FXML
    private Button proposerArticleBtn;

    @FXML
    public void initialize() {
        try {
            loadCategories();
            loadArticles();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //  Redirect to the form
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
        // Charger l'image de l'article depuis le dossier public/uploads/photo_dir
        try {
            String imageName = article.getImage_article();
            File imageFile = new File("C:/Users/Admin/Desktop/PI_RecyConnect_TechSquad/public/uploads/photo_dir/" + imageName);
            if (imageFile.exists()) {
                imageView.setImage(new Image(imageFile.toURI().toString()));
            } else {
                // image manquante → image par défaut
                imageView.setImage(new Image(getClass().getResource("/carousel-1.jpg").toExternalForm()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setImage(new Image(getClass().getResource("/carousel-1.jpg").toExternalForm()));
        }

        // Name + buttons
        HBox topLine = new HBox();
        topLine.setAlignment(Pos.CENTER_LEFT);
        topLine.setPadding(new Insets(10, 15, 0, 15));
        topLine.setSpacing(10);

        Label name = new Label(article.getNom_article());
        name.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Modify button
        Button modifyBtn = new Button("Modifier");
        modifyBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white; -fx-font-size: 15px;");
        modifyBtn.setPrefWidth(100);
        modifyBtn.setPrefHeight(35);

        // Delete button
        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 15px;");
        deleteBtn.setPrefWidth(100);
        deleteBtn.setPrefHeight(35);

        topLine.getChildren().addAll(name, spacer, modifyBtn, deleteBtn);

        // Info
        Label info = new Label("PU: " + article.getPrix() + " TN/Kg | " + article.getQuantite_article() + " Kg");
        info.setStyle("-fx-font-size: 15px;");
        info.setPadding(new Insets(0, 15, 15, 15));

        content.getChildren().addAll(imageView, topLine, info);
        card.getChildren().add(content);

        // Event handlers for the buttons
        modifyBtn.setOnAction(e -> {
            try {
                // Charger le formulaire d'ajout avec les données de l'article
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/formAjoutArticle.fxml"));
                Parent formView = loader.load();

                // Récupérer le contrôleur du formulaire d'ajout
                formAjoutArticle formController = loader.getController();

                // Passer l'article à modifier au formulaire
                formController.setArticleToModify(article); // Cette méthode sera ajoutée dans le contrôleur de formAjoutArticle

                // Mettre à jour l'affichage
                BorderPane root = (BorderPane) proposerArticleBtn.getScene().lookup("#rootBorderPane");
                root.setCenter(formView);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });


        deleteBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText(null);
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cet article ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    articleService.delete(article); // Supprime en base
                    loadArticles(); // Recharge toute la grille proprement
                    System.out.println("Article supprimé: " + article.getNom_article());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        return card;
    }

}
