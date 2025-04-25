package controllers.workshop;

import controllers.BaseUserController;
import entities.Cours;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import utils.MyDataBase;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;

public class DetailsWorkshopFront {

    @FXML private MediaView mediaView;
    @FXML private ImageView headerImage;
    @FXML private ImageView imageView;
    @FXML private Label titleLabel;
    @FXML private Label categoryLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label breadcrumbLabel;

    @FXML private HBox starContainer;
    @FXML private Label resetLabel;
    @FXML private Button btnRate;

    private int selectedRating = 0;
    private final int userId = 1; // 🔒 static for now
    private Cours currentCours;
    private MediaPlayer mediaPlayer;

    @FXML
    private void initialize() {
        setupStars();
    }

    public void setCours(Cours cours) {
        this.currentCours = cours;

        titleLabel.setText(cours.getTitreCours());
        categoryLabel.setText(cours.getCategorieCours().getNomCategorie());
        descriptionLabel.setText(cours.getDescriptionCours());
        breadcrumbLabel.setText("Accueil / Workshops / " + cours.getTitreCours());

        File imgHeader = new File("uploadsworkshop/" + cours.getImageCours());
        if (imgHeader.exists()) {
            headerImage.setImage(new Image(imgHeader.toURI().toString()));
        }

        // Load existing rating ⭐
        loadExistingRating();

        if (cours.getVideo() != null && !cours.getVideo().isBlank()) {
            File videoFile = new File("uploadsworkshop/" + cours.getVideo());
            if (videoFile.exists()) {
                try {
                    Media media = new Media(videoFile.toURI().toString());
                    mediaPlayer = new MediaPlayer(media);
                    mediaView.setMediaPlayer(mediaPlayer);
                    mediaPlayer.setAutoPlay(true);
                    mediaView.setVisible(true);
                } catch (MediaException e) {
                    mediaView.setVisible(false);
                }
            }
        }
    }


    private void setupStars() {
        starContainer.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            star.setStyle("-fx-font-size: 20px; -fx-text-fill: #ccc; -fx-cursor: hand;");
            final int rating = i;

            star.setOnMouseEntered(e -> updateStarVisuals(rating));
            star.setOnMouseExited(e -> updateStarVisuals(selectedRating));
            star.setOnMouseClicked(e -> {
                selectedRating = rating;
                updateStarVisuals(rating);
            });

            starContainer.getChildren().add(star);
        }
    }

    private void updateStarVisuals(int rating) {
        for (int i = 0; i < 5; i++) {
            Label star = (Label) starContainer.getChildren().get(i);
            star.setStyle(i < rating
                    ? "-fx-font-size: 20px; -fx-text-fill: gold;"
                    : "-fx-font-size: 20px; -fx-text-fill: #ccc;");
        }
    }

    @FXML
    private void handleResetStars() {
        selectedRating = 0;
        updateStarVisuals(0); // Reset stars visually only
    }

    @FXML
    private void handleRatingSubmit() {
        if (currentCours == null) return;

        Connection con = MyDataBase.getInstance().getConx();
        try {
            PreparedStatement check = con.prepareStatement("SELECT id FROM rating WHERE user_id = ? AND cours_id = ?");
            check.setInt(1, userId);
            check.setInt(2, currentCours.getId());
            ResultSet rs = check.executeQuery();

            if (selectedRating == 0) {
                if (rs.next()) {
                    PreparedStatement delete = con.prepareStatement("DELETE FROM rating WHERE user_id = ? AND cours_id = ?");
                    delete.setInt(1, userId);
                    delete.setInt(2, currentCours.getId());
                    delete.executeUpdate();
                    System.out.println("Note supprimée !");
                }
                return;
            }

            if (rs.next()) {
                int ratingId = rs.getInt("id");
                PreparedStatement update = con.prepareStatement("UPDATE rating SET note = ?, date_rate = ? WHERE id = ?");
                update.setInt(1, selectedRating);
                update.setDate(2, Date.valueOf(LocalDate.now()));
                update.setInt(3, ratingId);
                update.executeUpdate();
                System.out.println("Note mise à jour !");
            } else {
                PreparedStatement insert = con.prepareStatement("INSERT INTO rating (user_id, cours_id, note, date_rate) VALUES (?, ?, ?, ?)");
                insert.setInt(1, userId);
                insert.setInt(2, currentCours.getId());
                insert.setInt(3, selectedRating);
                insert.setDate(4, Date.valueOf(LocalDate.now()));
                insert.executeUpdate();
                System.out.println("Note ajoutée !");
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    private void loadExistingRating() {
        Connection con = MyDataBase.getInstance().getConx();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT note FROM rating WHERE user_id = ? AND cours_id = ?");
            ps.setInt(1, userId);
            ps.setInt(2, currentCours.getId());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                selectedRating = rs.getInt("note");
                updateStarVisuals(selectedRating);
            } else {
                selectedRating = 0;
                updateStarVisuals(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
