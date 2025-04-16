package controllers;

import entities.Article;
import entities.CategorieArticle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import services.ArticleService;
import services.CateArtService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;

public class formAjoutArticle {

    @FXML
    private TextField nomArticleField;

    @FXML
    private Label imageArticleField;

    @FXML
    private Button uploadImageButton;

    private File selectedFile;

    @FXML
    private ComboBox<String> categorieComboBox;

    @FXML
    private TextField localisationField;

    @FXML
    private TextField quantiteField;

    @FXML
    private TextField prixField;

    @FXML
    private TextArea descriptionArticleArea;

    @FXML
    private Button confirmerArticleButton;

    @FXML
    private Button annulerArticleButton;

    private final ArticleService articleService = new ArticleService();
    private final CateArtService cateArtService = new CateArtService();

    private List<CategorieArticle> categoriesList;
    private static final String article_IMAGE_DIR = "C:/Users/Admin/Desktop/PI_RecyConnect_TechSquad/public/uploads/photo_dir";


    @FXML
    public void initialize() {
        try {
            categoriesList = cateArtService.displayList();
            ObservableList<String> nomCategories = FXCollections.observableArrayList();
            for (CategorieArticle c : categoriesList) {
                nomCategories.add(c.getNom_categorie());
            }
            categorieComboBox.setItems(nomCategories);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        confirmerArticleButton.setOnAction(event -> {
            try {
                ajouterArticle();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        annulerArticleButton.setOnAction(event -> retourListeArticles());
    }

    private void ajouterArticle() throws SQLException {
        String nom = nomArticleField.getText();
        String image = imageArticleField.getText();
        String categorie = categorieComboBox.getValue();
        String localisation = localisationField.getText();
        String quantiteStr = quantiteField.getText();
        String prixStr = prixField.getText();
        String description = descriptionArticleArea.getText();

        //  Contrôle de saisie
        if (nom.isEmpty() || categorie == null || localisation.isEmpty()
                || quantiteStr.isEmpty() || prixStr.isEmpty() || description.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Champs vides", "Veuillez remplir tous les champs.");
            return;
        }
        if (selectedFile == null || imageArticleField.getText().equals("Aucune image sélectionnée")) {
            showAlert(Alert.AlertType.ERROR, "Image manquante", "Veuillez sélectionner une image pour l'article.");
            return;
        }

        int quantite;
        double prix;

        try {
            quantite = Integer.parseInt(quantiteStr);
            prix = Double.parseDouble(prixStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Format invalide", "Quantité et prix doivent être des nombres valides.");
            return;
        }

        if (quantite < 1) {
            showAlert(Alert.AlertType.ERROR, "Quantité invalide", "La quantité doit être supérieure ou égale à 1.");
            return;
        }

        if (prix < 0) {
            showAlert(Alert.AlertType.ERROR, "Prix invalide", "Le prix doit être supérieur ou égal à 0.");
            return;
        }

        int categorieId = -1;
        for (CategorieArticle c : categoriesList) {
            if (c.getNom_categorie().equals(categorie)) {
                categorieId = c.getId();
                break;
            }
        }

        if (categorieId == -1) {
            showAlert(Alert.AlertType.ERROR, "Catégorie invalide", "Veuillez choisir une catégorie valide.");
            return;
        }

        int utilisateurId = 1; // hatekchi zwari yqued el user

        Article article = new Article(categorieId, utilisateurId, nom, description, quantite, prix, image, localisation);
        articleService.add(article);

        if (selectedFile != null) {
            try {
                File destDir = new File(article_IMAGE_DIR);
                if (!destDir.exists()) destDir.mkdirs();

                File destFile = new File(destDir, selectedFile.getName());
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                article.setImage_article(selectedFile.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        showAlert(Alert.AlertType.INFORMATION, "Succès", "Article ajouté avec succès !");
        retourListeArticles();
    }

    private void resetForm() {
        nomArticleField.clear();

        localisationField.clear();
        quantiteField.clear();
        prixField.clear();
        descriptionArticleArea.clear();
        categorieComboBox.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String titre, String contenu) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(contenu);
        alert.showAndWait();
    }

    private void retourListeArticles() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/listeArticlesUser.fxml"));
            Parent listView = loader.load();
            BorderPane root = (BorderPane) nomArticleField.getScene().lookup("#rootBorderPane");
            root.setCenter(listView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleImageUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());
        if (file != null) {
            selectedFile = file;
            imageArticleField.setText(file.getName()); // affichage du nom
        }
    }

}
