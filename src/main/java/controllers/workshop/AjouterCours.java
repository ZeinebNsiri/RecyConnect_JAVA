package controllers.workshop;

import entities.CategorieCours;
import entities.Cours;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import services.CategorieCoursService;
import services.CoursService;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.List;

public class AjouterCours {

    @FXML
    private ComboBox<CategorieCours> categorieCombo;
    @FXML
    private Label errorCategorie;

    @FXML
    private TextField titreField;
    @FXML
    private Label errorTitre;

    @FXML
    private TextArea descriptionField;
    @FXML
    private Label errorDescription;

    @FXML
    private Label videoLabel;
    @FXML
    private Label errorVideo;

    @FXML
    private Label imageLabel;
    @FXML
    private Label errorImage;

    @FXML
    private Button browseVideoBtn;
    @FXML
    private Button browseImageBtn;
    @FXML
    private Button btnAjouterCours;
    @FXML
    private Button btnAnnuler;

    private File selectedVideoFile;
    private File selectedImageFile;

    private final CoursService coursService = new CoursService();
    private final CategorieCoursService categorieService = new CategorieCoursService();

    @FXML
    private void initialize() {
        loadCategories();

        categorieCombo.setConverter(new StringConverter<CategorieCours>() {
            @Override
            public String toString(CategorieCours object) {
                return object == null ? "" : object.getNomCategorie();
            }
            @Override
            public CategorieCours fromString(String string) {
                return null;
            }
        });

        categorieCombo.setButtonCell(new ListCell<CategorieCours>() {
            @Override
            protected void updateItem(CategorieCours item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.getNomCategorie());
                }
            }
        });
    }


    private void loadCategories() {
        try {
            List<CategorieCours> list = categorieService.displayList();
            categorieCombo.getItems().setAll(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVideoBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une vidéo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Vidéos", "*.mp4", "*.mov", "*.avi", "*.mkv")
        );
        selectedVideoFile = fileChooser.showOpenDialog(null);
        if (selectedVideoFile != null) {
            videoLabel.setText(selectedVideoFile.getName());
            errorVideo.setText("");
        } else {
            videoLabel.setText("Aucune vidéo choisie");
        }
    }

    @FXML
    private void handleImageBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.webp")
        );
        selectedImageFile = fileChooser.showOpenDialog(null);
        if (selectedImageFile != null) {
            imageLabel.setText(selectedImageFile.getName());
            errorImage.setText("");
        } else {
            imageLabel.setText("Aucune image choisie");
        }
    }

    @FXML
    private void ajouterCours() {
        errorCategorie.setText("");
        errorTitre.setText("");
        errorDescription.setText("");
        errorVideo.setText("");
        errorImage.setText("");

        boolean valid = true;
        if (categorieCombo.getValue() == null) {
            errorCategorie.setText("Veuillez choisir une catégorie.");
            valid = false;
        }
        if (titreField.getText().trim().isEmpty()) {
            errorTitre.setText("Le titre est obligatoire.");
            valid = false;
        }
        if (descriptionField.getText().trim().isEmpty()) {
            errorDescription.setText("La description est obligatoire.");
            valid = false;
        }
        if (selectedImageFile == null) {
            errorImage.setText("Veuillez choisir une image.");
            valid = false;
        }
        if (!valid) return;

        try {
            String imageCours = "default.png";
            if (selectedImageFile != null) {
                File uploadDir = new File("uploadsworkshop");
                if (!uploadDir.exists()) uploadDir.mkdirs();
                Path destImg = Paths.get("uploadsworkshop", selectedImageFile.getName());
                Files.copy(selectedImageFile.toPath(), destImg, StandardCopyOption.REPLACE_EXISTING);
                imageCours = selectedImageFile.getName();
            }

            String titreCours = titreField.getText().trim();
            CategorieCours chosenCategorie = categorieCombo.getValue();
            String descCours = descriptionField.getText().trim();

            String video = "";
            if (selectedVideoFile != null) {
                File uploadDir = new File("uploadsworkshop");
                if (!uploadDir.exists()) uploadDir.mkdirs();
                Path destVideo = Paths.get("uploadsworkshop", selectedVideoFile.getName());
                Files.copy(selectedVideoFile.toPath(), destVideo, StandardCopyOption.REPLACE_EXISTING);
                video = selectedVideoFile.getName();
            }


            Cours cours = new Cours(
                    0,
                    imageCours,
                    titreCours,
                    chosenCategorie,
                    descCours,
                    video
            );

            coursService.add(cours);

            showAlert("Succès", "Cours ajouté avec succès !", Alert.AlertType.CONFIRMATION);
            //retourAfficherListe();

        } catch (IOException e) {
            showAlert("Erreur Fichier", "Impossible de copier un fichier : " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (SQLException e) {
            showAlert("Erreur BD", "Impossible d'ajouter le cours : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void annulerCours() {
//        retourAfficherListe();
    }

//    private void retourAfficherListe() {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/workshop/"));
//            Parent root = loader.load();
//            Stage stage = (Stage) btnAnnuler.getScene().getWindow();
//            stage.setScene(new Scene(root));
//            stage.show();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
