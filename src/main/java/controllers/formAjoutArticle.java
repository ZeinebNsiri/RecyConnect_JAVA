package controllers;

import entities.Article;
import entities.CategorieArticle;
import entities.utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.json.JSONArray;
import org.json.JSONObject;
import services.ArticleService;
import services.CateArtService;
import services.UserService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.List;

public class formAjoutArticle {

    @FXML private TextField nomArticleField;
    @FXML private Label imageArticleField;
    @FXML private Button uploadImageButton;
    @FXML private ComboBox<String> categorieComboBox;
    @FXML private TextField localisationField;
    @FXML private TextField quantiteField;
    @FXML private TextField prixField;
    @FXML private TextArea descriptionArticleArea;
    @FXML private Button confirmerArticleButton;
    @FXML private Button annulerArticleButton;

    private File selectedFile;
    private final ArticleService articleService = new ArticleService();
    private final CateArtService cateArtService = new CateArtService();
    private List<CategorieArticle> categoriesList;
    private static final String article_IMAGE_DIR = "C:/Users/Admin/Desktop/PI_RecyConnect_TechSquad/public/uploads/photo_dir";
    private Article articleToModify = null;
    private final UserService userService = new UserService();


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

        if (nom.isEmpty() || categorie == null || localisation.isEmpty() ||
                quantiteStr.isEmpty() || prixStr.isEmpty() || description.isEmpty()) {
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
            showAlert(Alert.AlertType.ERROR, "Quantité invalide", "La quantité doit être ≥ 1.");
            return;
        }

        if (prix < 0) {
            showAlert(Alert.AlertType.ERROR, "Prix invalide", "Le prix doit être ≥ 0.");
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

        utilisateur user = userService.getUserById(9);
        int utilisateurId = user.getId();// Simulé

        Article article = new Article(categorieId, utilisateurId, nom, description, quantite, prix, image, localisation);

        if (articleToModify != null) {
            article.setId(articleToModify.getId());

            // Si une nouvelle image est sélectionnée pendant la modification
            if (selectedFile != null && !imageArticleField.getText().equals(articleToModify.getImage_article())) {
                // Vérifier l'image avec Sightengine
                if (!isImageSafeWithSightengine(selectedFile)) {
                    showAlert(Alert.AlertType.ERROR, "Image inappropriée", "L'image contient un contenu interdit.");
                    return;
                }
            }

            articleService.update(article);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Article modifié avec succès !");
        } else {
            // Ajout
            if (selectedFile == null || imageArticleField.getText().equals("Aucune image sélectionnée")) {
                showAlert(Alert.AlertType.ERROR, "Image manquante", "Veuillez sélectionner une image.");
                return;
            }

            if (!isImageSafeWithSightengine(selectedFile)) {
                showAlert(Alert.AlertType.ERROR, "Image inappropriée", "L'image contient un contenu interdit.");
                return;
            }

            articleService.add(article);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Article ajouté avec succès !");
        }


        retourListeArticles();
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
            imageArticleField.setText(file.getName());
        }
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

    private void showAlert(Alert.AlertType type, String titre, String contenu) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(contenu);
        alert.showAndWait();
    }

    private boolean isImageSafeWithSightengine(File imageFile) {
        try {
            String boundary = "===" + System.currentTimeMillis() + "===";
            String apiUser = "1497660233";
            String apiSecret = "4dQ6LFhYPK2GWfwyvdg8y8Tyj9DRcVJd";

            URL url = new URL("https://api.sightengine.com/1.0/check.json");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.setDoOutput(true);

            OutputStream output = con.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true);

            // Ajouter le fichier image
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"media\"; filename=\"")
                    .append(imageFile.getName()).append("\"\r\n");
            writer.append("Content-Type: ").append(Files.probeContentType(imageFile.toPath())).append("\r\n\r\n");
            writer.flush();
            Files.copy(imageFile.toPath(), output);
            output.flush();
            writer.append("\r\n").flush();

            // Ajouter les autres champs
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"api_user\"\r\n\r\n")
                    .append(apiUser).append("\r\n");

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"api_secret\"\r\n\r\n")
                    .append(apiSecret).append("\r\n");

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"models\"\r\n\r\n")
                    .append("nudity-2.1,weapon,alcohol,recreational_drug,violence,text-content").append("\r\n");

            writer.append("--").append(boundary).append("--").append("\r\n");
            writer.close();

            // Lire la réponse
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            // Parser la réponse
            JSONObject json = new JSONObject(response.toString());
            System.out.println("🧠 Résultat modération Sightengine : " + json.toString(2));

            // Vérification nudité
            JSONObject nudity = json.optJSONObject("nudity");
            if (nudity != null) {
                double erotica = nudity.optDouble("erotica", 0);
                double suggestive = nudity.optDouble("suggestive", 0);
                double verySuggestive = nudity.optDouble("very_suggestive", 0);

                if (erotica > 0.4 || suggestive > 0.5 || verySuggestive > 0.5) {
                    return false;
                }
            }

            // Vérification armes
            JSONObject weapon = json.optJSONObject("weapon");
            if (weapon != null) {
                JSONObject classes = weapon.optJSONObject("classes");
                for (String key : classes.keySet()) {
                    if (classes.getDouble(key) > 0.3) {
                        return false;
                    }
                }
            }

            // Drogue, alcool, violence
            if (json.optJSONObject("alcohol").optDouble("prob", 0) > 0.3 ||
                    json.optJSONObject("recreational_drug").optDouble("prob", 0) > 0.3 ||
                    json.optJSONObject("violence").optDouble("prob", 0) > 0.3) {
                return false;
            }

            // Texte offensif
            JSONObject text = json.optJSONObject("text");
            if (text != null) {
                for (String key : text.keySet()) {
                    JSONArray arr = text.optJSONArray(key);
                    if (arr != null && arr.length() > 0) {
                        return false;
                    }
                }
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }




    public void setArticleToModify(Article article) {
        this.articleToModify = article;
        nomArticleField.setText(article.getNom_article());
        localisationField.setText(article.getLocalisation_article());
        quantiteField.setText(String.valueOf(article.getQuantite_article()));
        prixField.setText(String.valueOf(article.getPrix()));
        descriptionArticleArea.setText(article.getDescription_article());

        for (CategorieArticle c : categoriesList) {
            if (c.getId() == article.getCategorie_id()) {
                categorieComboBox.setValue(c.getNom_categorie());
                break;
            }
        }

        String imagePath = article_IMAGE_DIR + "/" + article.getImage_article();
        File imageFile = new File(imagePath);

        if (imageFile.exists()) {
            imageArticleField.setText(article.getImage_article());
        } else {
            imageArticleField.setText("Aucune image sélectionnée");
        }
    }
}
