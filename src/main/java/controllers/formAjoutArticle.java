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

    private Article articleToModify = null;

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

        int utilisateurId = 1; // Simulateur de l'utilisateur actuel

        Article article = new Article(categorieId, utilisateurId, nom, description, quantite, prix, image, localisation);

        // Si l'article à modifier existe (articleToModify n'est pas nul), on effectue une mise à jour
        if (articleToModify != null) {
            article.setId(articleToModify.getId()); // Conserver l'ID de l'article à modifier
            articleService.update(article);  // Mettre à jour l'article dans la base de données
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Article modifié avec succès !");
        } else {
            if (selectedFile == null || imageArticleField.getText().equals("Aucune image sélectionnée")) {
                showAlert(Alert.AlertType.ERROR, "Image manquante", "Veuillez sélectionner une image pour l'article.");
                return;
            }
            articleService.add(article);  // Si l'article à modifier n'existe pas, on l'ajoute
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Article ajouté avec succès !");
        }

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

    public void setArticleToModify(Article article) {
        this.articleToModify = article;  // Conserver l'article à modifier
        nomArticleField.setText(article.getNom_article());
        localisationField.setText(article.getLocalisation_article());
        quantiteField.setText(String.valueOf(article.getQuantite_article()));
        prixField.setText(String.valueOf(article.getPrix()));
        descriptionArticleArea.setText(article.getDescription_article());

        // Mettre à jour la catégorie
        for (CategorieArticle c : categoriesList) {
            if (c.getId() == article.getCategorie_id()) {
                categorieComboBox.setValue(c.getNom_categorie());
                break;
            }
        }

        // Mettre à jour l'image
        String imagePath = "C:/Users/Admin/Desktop/PI_RecyConnect_TechSquad/public/uploads/photo_dir/" + article.getImage_article();
        File imageFile = new File(imagePath);

        if (imageFile.exists()) {
            imageArticleField.setText(article.getImage_article());  // Affiche le nom de l'image dans le label
        } else {
            imageArticleField.setText("Aucune image sélectionnée");  // Si pas d'image
        }
    }


}
