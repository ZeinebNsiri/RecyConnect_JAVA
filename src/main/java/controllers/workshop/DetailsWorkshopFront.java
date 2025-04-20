package controllers.workshop;

import controllers.BaseUserController;
import entities.Cours;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class DetailsWorkshopFront {

    @FXML private MediaView mediaView;
    @FXML private Label titleLabel;
    @FXML private Label categoryLabel;
    @FXML private Label descriptionLabel;

    private Cours currentCours;


    public void setCours(Cours cours) {
        this.currentCours = cours;


        titleLabel.setText(cours.getTitreCours());
        categoryLabel.setText(cours.getCategorieCours().getNomCategorie());
        descriptionLabel.setText(cours.getDescriptionCours());


        if (cours.getVideo() != null && !cours.getVideo().trim().isEmpty()) {
            File f = new File("uploadsworkshop/" + cours.getVideo());
            if (f.exists()) {
                try {
                    Media media = new Media(f.toURI().toString());
                    MediaPlayer player = new MediaPlayer(media);
                    mediaView.setMediaPlayer(player);
                    player.setAutoPlay(true);
                } catch (MediaException e) {
                    System.out.println("Impossible de lire la vidéo: " + e.getMessage());
                }
            }
        }
    }


    @FXML
    private void handleBack(javafx.event.ActionEvent ev) {
        // on recharge juste la vue liste dans le même BorderPane
        BaseUserController.instance.showWorkshopsView();
    }
}
