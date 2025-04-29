package controllers.Events;

import controllers.BaseUserController;
import entities.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import controllers.Reservations.EventReservationController;
import services.ReservationService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
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
    @FXML private Label eventPeriod;
    @FXML private WebView mapWebView; // ✅ Correct one (remove mapImageView)

    private Event event;
    private final ReservationService reservationService = new ReservationService();
    private String currentUserName = "amal"; // ✅ Hardcoded for now

    @FXML
    public void setEvent(Event event) {
        this.event = event;

        eventTitle.setText(event.getName());
        eventDateTime.setText(event.getDate().format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy")) + " à " + event.getTime());
        eventDescription.setText(event.getDescription());
        eventPlaces.setText("Places disponibles : " + event.getRemaining());

        eventDate.setText("📅 Date : " + event.getDate());
        eventTime.setText("⏰ Heure : " + event.getTime());
        eventLocation.setText("📍 Lieu : " + event.getLocation());

        if (event.getEndTime() != null) {
            eventPeriod.setText("⏳ De " + event.getTime() + " à " + event.getEndTime());
        } else {
            eventPeriod.setText("⏳ Début : " + event.getTime());
        }

        // Set Image
        File imageFile = new File("uploads/" + event.getImage());
        if (imageFile.exists()) {
            eventImageView.setImage(new Image(imageFile.toURI().toString()));
        }

        // Add meeting link if exists
        String meetingUrl = event.getMeetingLink();
        if (meetingUrl != null && !meetingUrl.isEmpty()) {
            Hyperlink meetLink = new Hyperlink("🧑‍💻 Rejoindre l'événement en ligne");
            meetLink.setOnAction(e -> handleJoinOnlineEvent());
            ((VBox) eventLocation.getParent()).getChildren().add(meetLink);
        }

        // Load the Map
        loadMap(event.getCoordinates());
    }

    private void loadMap(String coordinates) {
        if (coordinates == null || coordinates.isEmpty()) {
            return;
        }

        String[] parts = coordinates.split(",");
        if (parts.length != 2) {
            return;
        }

        String lat = parts[0].trim();
        String lon = parts[1].trim();

        String mapHtml = "<html><head>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.3/dist/leaflet.css'/>"
                + "<script src='https://unpkg.com/leaflet@1.9.3/dist/leaflet.js'></script>"
                + "</head>"
                + "<body style='margin:0;'>"
                + "<div id='map' style='width:100%;height:100%;'></div>"
                + "<script>"
                + "var map = L.map('map').setView([" + lat + "," + lon + "], 15);"
                + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {maxZoom: 19}).addTo(map);"
                + "L.marker([" + lat + "," + lon + "]).addTo(map).bindPopup('Emplacement').openPopup();"
                + "</script>"
                + "</body></html>";

        WebEngine webEngine = mapWebView.getEngine();
        webEngine.loadContent(mapHtml);
    }

    private void handleJoinOnlineEvent() {
        try {
            if (!reservationService.isUserRegisteredForEvent(event.getId(), currentUserName)) {
                showAlert(Alert.AlertType.WARNING, "Non inscrit", "Vous devez d'abord réserver pour accéder à cet événement.");
                return;
            }

            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            if (!event.getDate().equals(today)) {
                showAlert(Alert.AlertType.INFORMATION, "Hors date", "L'événement est prévu pour le " + event.getDate() + ".");
                return;
            }

            if (now.isBefore(event.getTime())) {
                showAlert(Alert.AlertType.INFORMATION, "Pas encore commencé", "L'événement commencera à " + event.getTime() + ".");
                return;
            }

            if (now.isAfter(event.getEndTime())) {
                showAlert(Alert.AlertType.WARNING, "Événement terminé", "Cet événement est déjà terminé.");
                return;
            }

            main.mainFX.getHostServicesInstance().showDocument(event.getMeetingLink());

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur lors de la vérification de votre inscription.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue.");
        }
    }

    @FXML
    private void handleInscription() {
        try {
            if (reservationService.isUserRegisteredForEvent(event.getId(), currentUserName)) {
                showAlert(Alert.AlertType.INFORMATION, "Déjà inscrit", "Vous êtes déjà inscrit à cet événement !");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReservationViews/EventReservationForm.fxml"));
            Parent root = loader.load();

            EventReservationController controller = loader.getController();
            controller.setEvent(event);

            Stage stage = (Stage) eventTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur lors de la vérification de votre inscription.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseUser.fxml"));
            Parent root = loader.load();

            BaseUserController baseUserController = loader.getController();
            baseUserController.showEventsView();

            Stage stage = (Stage) eventTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
