package controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;

public class AcceuilController {

    @FXML
    private ImageView imageView;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Rectangle overlay;

    private List<String> descriptions;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private StackPane carouselPane;

    private List<Image> images;
    private int currentIndex = 0;

    @FXML
    public void initialize() {
        try {
            images = Arrays.asList(
                    new Image(getClass().getResource("/images/bg1.jpg").toExternalForm()),

                    new Image(getClass().getResource("/images/bg3.jpg").toExternalForm())


            );

            descriptions = Arrays.asList(
                    "Bienvenue sur RecyConnect",

                    "Recycler, Réutiliser, Réinventer"

            );

            if (!images.isEmpty()) {
                imageView.setImage(images.get(currentIndex));
                descriptionLabel.setText(descriptions.get(currentIndex));
                imageView.setPreserveRatio(true); // Garder le ratio (optionnel)
                imageView.setSmooth(true);

                // Adapter l'image à la largeur de la scène (fenêtre entière)
                scrollPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        imageView.fitWidthProperty().bind(newScene.widthProperty());
                    }
                });



                imageView.boundsInParentProperty().addListener((obs, oldBounds, newBounds) -> {
                    overlay.setWidth(newBounds.getWidth());
                    overlay.setHeight(newBounds.getHeight());
                });

                startAutoCarousel();
            }

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des images : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startAutoCarousel() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(4), e -> showNextImage()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    @FXML
    private void showPreviousImage() {
        if (images != null && !images.isEmpty()) {
            currentIndex = (currentIndex - 1 + images.size()) % images.size();
            imageView.setImage(images.get(currentIndex));
        }
    }

    @FXML
    private void showNextImage() {
        if (images != null && !images.isEmpty()) {
            currentIndex = (currentIndex + 1) % images.size();
            imageView.setImage(images.get(currentIndex));
            descriptionLabel.setText(descriptions.get(currentIndex));
        }
    }
}