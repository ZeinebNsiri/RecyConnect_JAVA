package controllers.workshop;

import entities.CategorieCours;
import entities.Cours;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
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

public class ModifierCours {

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
    private Button btnModifier;
    @FXML
    private Button btnAnnuler;

    private File selectedVideoFile;
    private File selectedImageFile;

    private final CoursService coursService = new CoursService();
    private final CategorieCoursService categorieService = new CategorieCoursService();

    private Cours currentCours;

    @FXML
    private void initialize() {
        loadCategories();

        categorieCombo.setConverter(new StringConverter<CategorieCours>() {
            @Override
            public String toString(CategorieCours object) {
                return (object == null) ? "" : object.getNomCategorie();
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


    public void setCours(Cours cours) {
        this.currentCours = cours;
        titreField.setText(cours.getTitreCours());
        descriptionField.setText(cours.getDescriptionCours());
        if (cours.getImageCours() != null && !cours.getImageCours().isEmpty()) {
            imageLabel.setText("");
            imageLabel.setGraphic(cours.getImageView());
        } else {
            imageLabel.setText("Aucune image choisie");
        }
        if (cours.getVideo() != null && !cours.getVideo().isEmpty()) {
            videoLabel.setText("");
            videoLabel.setGraphic(cours.getVideoView());
        } else {
            videoLabel.setText("Aucune vidéo choisie");
        }
        if (cours.getCategorieCours() != null) {
            categorieCombo.setValue(cours.getCategorieCours());
        }
    }

    @FXML
    private void handleImageBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.webp")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedImageFile = file;
            ImageView iv = new ImageView(new javafx.scene.image.Image(file.toURI().toString()));
            iv.setFitWidth(100);
            iv.setPreserveRatio(true);
            imageLabel.setText("");
            imageLabel.setGraphic(iv);
            errorImage.setText("");
        } else {
            if (currentCours.getImageCours() != null && !currentCours.getImageCours().isEmpty()) {
                imageLabel.setText("");
                imageLabel.setGraphic(currentCours.getImageView());
            } else {
                imageLabel.setText("Aucune image choisie");
            }
        }
    }

    @FXML
    private void handleVideoBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une vidéo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Vidéos", "*.mp4", "*.mov", "*.avi", "*.mkv")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedVideoFile = file;
            try {
                Media media = new Media(file.toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setMute(true);
                mediaPlayer.setAutoPlay(true);
                MediaView mv = new MediaView(mediaPlayer);
                mv.setFitWidth(100);
                mv.setPreserveRatio(true);
                videoLabel.setText("");
                videoLabel.setGraphic(mv);
            } catch (MediaException e) {
                System.out.println("Erreur lors de la création du MediaView: " + e.getMessage());
                videoLabel.setText("Erreur vidéo");
            }
            errorVideo.setText("");
        } else {
            if (currentCours.getVideo() != null && !currentCours.getVideo().isEmpty()) {
                videoLabel.setText("");
                videoLabel.setGraphic(currentCours.getVideoView());
            } else {
                videoLabel.setText("Aucune vidéo choisie");
            }
        }
    }

    @FXML
    private void updateCours() {
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
        if (!valid) return;

        try {
            currentCours.setTitreCours(titreField.getText().trim());
            currentCours.setDescriptionCours(descriptionField.getText().trim());
            currentCours.setCategorieCours(categorieCombo.getValue());
            if (selectedImageFile != null) {
                File uploadDir = new File("uploadsworkshop");
                if (!uploadDir.exists()) uploadDir.mkdirs();
                Path dest = Paths.get("uploadsworkshop", selectedImageFile.getName());
                Files.copy(selectedImageFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                currentCours.setImageCours(selectedImageFile.getName());
            }
            if (selectedVideoFile != null) {
                File uploadDir = new File("uploadsworkshop");
                if (!uploadDir.exists()) uploadDir.mkdirs();
                Path dest = Paths.get("uploadsworkshop", selectedVideoFile.getName());
                Files.copy(selectedVideoFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                currentCours.setVideo(selectedVideoFile.getName());
            }
            coursService.update(currentCours);
            showAlert("Succès", "Workshop modifié avec succès !", Alert.AlertType.INFORMATION);
            retourAfficherListe();
        } catch (IOException e) {
            showAlert("Erreur Fichier", "Impossible de copier un fichier : " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (SQLException e) {
            showAlert("Erreur BD", "Impossible de modifier le cours : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void annulerCours() {
        retourAfficherListe();
    }

    private void retourAfficherListe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/workshop/AfficherCours.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnAnnuler.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
