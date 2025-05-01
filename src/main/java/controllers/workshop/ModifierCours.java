package controllers.workshop;

import controllers.BaseAdminController;
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

    @FXML private ComboBox<CategorieCours> categorieCombo;
    @FXML private Label errorCategorie;
    @FXML private TextField titreField;
    @FXML private Label errorTitre;
    @FXML private TextArea descriptionField;
    @FXML private Label errorDescription;
    @FXML private Label imageLabel;
    @FXML private Label errorImage;
    @FXML private Label videoLabel;
    @FXML private Label errorVideo;
    @FXML private Button browseImageBtn;
    @FXML private Button browseVideoBtn;
    @FXML private Button btnModifier;
    @FXML private Button btnAnnuler;

    private File selectedImageFile;
    private File selectedVideoFile;
    private BaseAdminController baseAdminController; // Store the controller

    private final CoursService coursService = new CoursService();
    private final CategorieCoursService categorieService = new CategorieCoursService();
    private Cours currentCours;
    private String origTitre;
    private String origDescription;
    private CategorieCours origCategorie;
    private String origImage;
    private String origVideo;

    @FXML
    private void initialize() {
        loadCategories();

        categorieCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(CategorieCours object) {
                return object == null ? "" : object.getNomCategorie();
            }
            @Override
            public CategorieCours fromString(String string) {
                return null;
            }
        });
        categorieCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(CategorieCours item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNomCategorie());
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

        origTitre = cours.getTitreCours();
        origDescription = cours.getDescriptionCours();
        origCategorie = cours.getCategorieCours();
        origImage = cours.getImageCours();
        origVideo = cours.getVideo();
    }

    public void setBaseAdminController(BaseAdminController controller) {
        this.baseAdminController = controller;
        System.out.println("BaseAdminController set in ModifierCours: " + (baseAdminController != null));
    }

    @FXML
    private void handleImageBrowse() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.webp")
        );
        File file = fc.showOpenDialog(null);
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
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une vidéo");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Vidéos", "*.mp4", "*.mov", "*.avi", "*.mkv")
        );
        File file = fc.showOpenDialog(null);
        if (file != null) {
            selectedVideoFile = file;
            try {
                Media media = new Media(file.toURI().toString());
                MediaPlayer player = new MediaPlayer(media);
                player.setMute(true);
                player.setAutoPlay(true);
                MediaView mv = new MediaView(player);
                mv.setFitWidth(100);
                mv.setPreserveRatio(true);
                videoLabel.setText("");
                videoLabel.setGraphic(mv);
                errorVideo.setText("");
            } catch (MediaException ex) {
                System.out.println("Erreur création MediaView: " + ex.getMessage());
                videoLabel.setText("Erreur vidéo");
            }
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
        errorImage.setText("");
        errorVideo.setText("");

        boolean sameTitre = titreField.getText().trim().equals(origTitre);
        boolean sameDesc = descriptionField.getText().trim().equals(origDescription);
        boolean sameCat = categorieCombo.getValue() == origCategorie;
        boolean sameImg = selectedImageFile == null;
        boolean sameVid = selectedVideoFile == null;
        if (sameTitre && sameDesc && sameCat && sameImg && sameVid) {
            new Alert(Alert.AlertType.INFORMATION, "Vous n'avez modifié aucun champ.").showAndWait();
            return;
        }

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
                Path dest = Paths.get("uploadsworkshop", selectedImageFile.getName());
                Files.createDirectories(dest.getParent());
                Files.copy(selectedImageFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                currentCours.setImageCours(selectedImageFile.getName());
            }

            if (selectedVideoFile != null) {
                Path dest = Paths.get("uploadsworkshop", selectedVideoFile.getName());
                Files.createDirectories(dest.getParent());
                Files.copy(selectedVideoFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                currentCours.setVideo(selectedVideoFile.getName());
            }

            coursService.update(currentCours);

            new Alert(Alert.AlertType.INFORMATION, "Workshop modifié avec succès !").showAndWait();
            retourAfficherListe();
        } catch (IOException | SQLException ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    @FXML
    private void annulerCours() {
        retourAfficherListe();
    }

    private void retourAfficherListe() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        boolean wasMaximized = stage.isMaximized();

        if (baseAdminController != null) {
            System.out.println("Navigating back to AfficherCours using stored BaseAdminController...");
            baseAdminController.showWorkshopsView();
            stage.setMaximized(wasMaximized);
            return;
        }

        // Fallback: Load a new BaseAdmin.fxml
        System.out.println("BaseAdminController not set, falling back to load a new BaseAdmin.fxml...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseAdmin.fxml"));
            Parent root = loader.load();
            BaseAdminController controller = loader.getController();
            controller.showWorkshopsView();
            stage.setScene(new Scene(root));
            stage.setMaximized(wasMaximized);
            stage.show();
        } catch (IOException ex) {
            System.err.println("Error loading BaseAdmin.fxml: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}