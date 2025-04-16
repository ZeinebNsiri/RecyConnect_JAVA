package controllers.Events;

import entities.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class EventDetailsController {

    @FXML private ImageView eventImageView;
    @FXML private Label eventTitle;
    @FXML private Label eventDateTime;
    @FXML private Label eventDescription;
    @FXML private Label eventPlaces;
    @FXML private Label eventDate;
    @FXML private Label eventTime;
    @FXML private Label eventLocation;
    @FXML private ImageView mapImageView;

    private Event event;

    public void setEvent(Event event) {
        this.event = event;

        // Load event data
        eventTitle.setText(event.getName());
        eventDateTime.setText(event.getDate().format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy")) + " à " + event.getTime());
        eventDescription.setText(event.getDescription());
        eventPlaces.setText("Places disponibles : " + event.getRemaining());

        eventDate.setText("📅 Date : " + event.getDate());
        eventTime.setText("⏰ Heure : " + event.getTime());
        eventLocation.setText("📍 Lieu : " + event.getLocation());

        // Image from upload folder
        File imageFile = new File("uploads/" + event.getImage());
        if (imageFile.exists()) {
            eventImageView.setImage(new Image(imageFile.toURI().toString()));
        }

        // Static map image from OpenStreetMap (can be replaced by browser if interactive map is needed)
       mapImageView.setImage(new Image("https://static-maps.yandex.ru/1.x/?ll=10.16579,36.8065&z=15&l=map&size=300,200&pt=10.16579,36.8065,pm2rdl")); // Replace coordinates dynamically if needed
    }

    @FXML
    private void handleBack() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/EventViews/ListEventsFront.fxml"));
            Stage stage = (Stage) eventTitle.getScene().getWindow();
            stage.setScene(new Scene(view));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
