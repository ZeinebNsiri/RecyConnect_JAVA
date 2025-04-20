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
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
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

    // Labels servant à afficher la preview image / vidéo
    @FXML
    private Label imageLabel;
    @FXML
    private Label errorImage;
    @FXML
    private Label videoLabel;
    @FXML
    private Label errorVideo;

    @FXML
    private Button browseImageBtn;
    @FXML
    private Button browseVideoBtn;
    @FXML
    private Button btnModifier;
    @FXML
    private Button btnAnnuler;

    private File selectedImageFile;
    private File selectedVideoFile;

    private final CoursService coursService = new CoursService();
    private final CategorieCoursService categorieService = new CategorieCoursService();

    // Le cours en cours de modification
    private Cours currentCours;

    // Champs mémorisant l'état initial
    private String origTitre;
    private String origDescription;
    private CategorieCours origCategorie;
    private String origImage;
    private String origVideo;

    @FXML
    private void initialize() {
        // Charge la liste des catégories
        loadCategories();

        // Affiche uniquement le nom dans le ComboBox
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

    /** Appelé par AfficherCours pour pré‐remplir le formulaire */
    public void setCours(Cours cours) {
        this.currentCours = cours;

        // Remplissage des champs
        titreField.setText(cours.getTitreCours());
        descriptionField.setText(cours.getDescriptionCours());

        // Preview image
        if (cours.getImageCours() != null && !cours.getImageCours().isEmpty()) {
            imageLabel.setText("");
            imageLabel.setGraphic(cours.getImageView());
        } else {
            imageLabel.setText("Aucune image choisie");
        }

        // Preview vidéo
        if (cours.getVideo() != null && !cours.getVideo().isEmpty()) {
            videoLabel.setText("");
            videoLabel.setGraphic(cours.getVideoView());
        } else {
            videoLabel.setText("Aucune vidéo choisie");
        }

        // Sélection de la catégorie
        if (cours.getCategorieCours() != null) {
            categorieCombo.setValue(cours.getCategorieCours());
        }

        // --- Mémorisation de l'état initial ---
        origTitre       = cours.getTitreCours();
        origDescription = cours.getDescriptionCours();
        origCategorie   = cours.getCategorieCours();
        origImage       = cours.getImageCours();
        origVideo       = cours.getVideo();
    }

    @FXML
    private void handleImageBrowse() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.jpg","*.jpeg","*.png","*.webp")
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
            // Restaure preview d'origine
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
                new FileChooser.ExtensionFilter("Vidéos", "*.mp4","*.mov","*.avi","*.mkv")
        );
        File file = fc.showOpenDialog(null);
        if (file != null) {
            selectedVideoFile = file;
            try {
                javafx.scene.media.Media media = new javafx.scene.media.Media(file.toURI().toString());
                javafx.scene.media.MediaPlayer player = new javafx.scene.media.MediaPlayer(media);
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
            // Restaure preview d'origine
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
        // Réinitialisation des erreurs
        errorCategorie.setText("");
        errorTitre.setText("");
        errorDescription.setText("");
        errorImage.setText("");
        errorVideo.setText("");

        // --- Si rien n’a changé, on stoppe ---
        boolean sameTitre = titreField.getText().trim().equals(origTitre);
        boolean sameDesc  = descriptionField.getText().trim().equals(origDescription);
        boolean sameCat   = categorieCombo.getValue() == origCategorie;
        boolean sameImg   = selectedImageFile == null;
        boolean sameVid   = selectedVideoFile == null;
        if (sameTitre && sameDesc && sameCat && sameImg && sameVid) {
            new Alert(Alert.AlertType.INFORMATION, "Vous n'avez modifié aucun champ.").showAndWait();
            return;
        }

        // Validation minimale
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
            // Mise à jour de l'objet
            currentCours.setTitreCours(titreField.getText().trim());
            currentCours.setDescriptionCours(descriptionField.getText().trim());
            currentCours.setCategorieCours(categorieCombo.getValue());

            // Gestion du nouveau fichier image
            if (selectedImageFile != null) {
                Path dest = Paths.get("uploadsworkshop", selectedImageFile.getName());
                Files.createDirectories(dest.getParent());
                Files.copy(selectedImageFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                currentCours.setImageCours(selectedImageFile.getName());
            }
            // Gestion du nouveau fichier vidéo
            if (selectedVideoFile != null) {
                Path dest = Paths.get("uploadsworkshop", selectedVideoFile.getName());
                Files.createDirectories(dest.getParent());
                Files.copy(selectedVideoFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                currentCours.setVideo(selectedVideoFile.getName());
            }

            // Appel du service
            coursService.update(currentCours);

            new Alert(Alert.AlertType.INFORMATION, "Workshop modifié avec succès !").showAndWait();
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
        try {
            // 1) Charger le shell
            FXMLLoader shellLoader = new FXMLLoader(
                    getClass().getResource("/BaseAdmin.fxml")
            );
            Parent shellRoot = shellLoader.load();
            BaseAdminController shell = shellLoader.getController();

            // 2) Afficher la liste des cours (la même méthode que pour ton bouton “Afficher”)
            shell.showWorkshopsView();

            // 3) Réafficher
            Stage stage = (Stage) btnAnnuler.getScene().getWindow();
            stage.setScene(new Scene(shellRoot, 1000, 600));
            stage.setTitle("Liste des Workshops");
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
