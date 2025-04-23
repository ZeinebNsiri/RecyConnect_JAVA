package controllers.workshop;

import controllers.BaseUserController;
import entities.Cours;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.io.File;

public class DetailsWorkshopFront {

    @FXML private MediaView mediaView;
    @FXML private ImageView headerImage;
    @FXML private Label titleLabel;
    @FXML private Label categoryLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label breadcrumbLabel;

    private MediaPlayer mediaPlayer;
    private Cours currentCours;

    public void setCours(Cours cours) {
        this.currentCours = cours;

        titleLabel.setText(cours.getTitreCours());
        categoryLabel.setText(cours.getCategorieCours().getNomCategorie());
        descriptionLabel.setText(cours.getDescriptionCours());
        breadcrumbLabel.setText("Accueil / Workshops / " + cours.getTitreCours());

        // Image d'en-tête
        File imgHeader = new File("uploadsworkshop/" + cours.getImageCours());
        if (imgHeader.exists()) {
            headerImage.setImage(new Image(imgHeader.toURI().toString()));
        }

        // Gestion de la vidéo
        if (cours.getVideo() != null && !cours.getVideo().trim().isEmpty()) {
            File videoFile = new File("uploadsworkshop/" + cours.getVideo());
            if (videoFile.exists()) {
                try {
                    Media media = new Media(videoFile.toURI().toASCIIString());
                    mediaPlayer = new MediaPlayer(media);
                    mediaView.setMediaPlayer(mediaPlayer);
                    mediaPlayer.setAutoPlay(true);
                    mediaView.setVisible(true);
                } catch (MediaException e) {
                    System.out.println("Erreur lecture vidéo : " + e.getMessage());
                    mediaView.setVisible(false);
                }
            } else {
                mediaView.setVisible(false);
            }
        } else {
            mediaView.setVisible(false);
        }
    }

    @FXML
    private void handleBack(javafx.event.ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        BaseUserController.instance.showWorkshopsView();
    }
}
