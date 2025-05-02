package controllers.Events;

import controllers.BaseUserController;
import entities.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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
import java.util.Timer;
import java.util.TimerTask;

public class EventDetailsController {

    @FXML private ImageView eventImageView;
    @FXML private Label eventTitle;
    @FXML private Label eventDateTime;
    @FXML private Label eventDescription;
    @FXML private Label eventPlaces;
    @FXML private Label eventDate;
    @FXML private Label eventPeriod;
    @FXML private Label eventLocation;
    @FXML private WebView mapWebView;
    @FXML private Button joinMeetingButton;
    @FXML private Label meetingInfoLabel;
    @FXML private HBox meetingContainer;
    @FXML private Label onlineEventLabel;
    @FXML private VBox mapContainer;
    @FXML private VBox onlineEventContainer;
    @FXML private Button registerButton;

    private Event event;
    private final ReservationService reservationService = new ReservationService();
    private String currentUserName = "amal"; // Hardcoded for now, should be set from login system
    private Timer meetingStatusTimer;
    private boolean isOnlineEvent = false;

    @FXML
    public void setEvent(Event event) {
        this.event = event;

        // Determine if this is an online event based on location being "en ligne"
        isOnlineEvent = "en ligne".equalsIgnoreCase(event.getLocation());

        // Format dates with proper patterns
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Set basic event information
        eventTitle.setText(event.getName());
        eventDateTime.setText(event.getDate().format(dateFormatter) + " à " + event.getTime().format(timeFormatter));
        eventDescription.setText(event.getDescription());
        eventPlaces.setText("Places disponibles : " + event.getRemaining());

        // Set detailed event information
        eventDate.setText("📅 Date : " + event.getDate().format(dateFormatter));

        if (event.getEndTime() != null) {
            eventPeriod.setText("⏳ De " + event.getTime().format(timeFormatter) + " à " + event.getEndTime().format(timeFormatter));
        } else {
            eventPeriod.setText("⏳ Début : " + event.getTime().format(timeFormatter));
        }

        eventLocation.setText("📍 Lieu : " + event.getLocation());

        // Set event image
        File imageFile = new File("uploads/" + event.getImage());
        if (imageFile.exists()) {
            Image image = new Image(imageFile.toURI().toString());
            eventImageView.setImage(image);
        } else {
            // Set a default image
            eventImageView.setImage(new Image(getClass().getResourceAsStream("/images/default_event.png")));
        }

        // Handle online vs on-site event display
        setupEventTypeSpecificUI();
    }

    /**
 * Configure the UI based on whether this is an online or on-site event
 */
private void setupEventTypeSpecificUI() {
    if (isOnlineEvent) {
        // This is an online event
        mapContainer.setVisible(false);
        mapContainer.setManaged(false);

        onlineEventContainer.setVisible(true);
        onlineEventContainer.setManaged(true);
        onlineEventLabel.setText("🧑‍💻 Événement en ligne");
        onlineEventLabel.setVisible(true);

        // Setup meeting info and button
        meetingContainer.setVisible(true);
        meetingContainer.setManaged(true);

        // Initialize meeting status
        updateMeetingStatus();

        // Set a timer to update the meeting status every minute
        if (meetingStatusTimer != null) {
            meetingStatusTimer.cancel();
        }
        meetingStatusTimer = new Timer(true);
        meetingStatusTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> updateMeetingStatus());
            }
        }, 0, 60000); // Update every minute
    } else {
        // This is an on-site event
        onlineEventContainer.setVisible(false);
        onlineEventContainer.setManaged(false);
        onlineEventLabel.setVisible(false);
        meetingContainer.setVisible(false);
        meetingContainer.setManaged(false);

        mapContainer.setVisible(true);
        mapContainer.setManaged(true);

        // Load map for on-site events
        loadMap(event.getCoordinates());
    }

    // Check registration status to update UI accordingly
    try {
        boolean isRegistered = reservationService.isUserRegisteredForEvent(event.getId(), currentUserName);
        if (isRegistered) {
            registerButton.setText("✅ Déjà inscrit");
            registerButton.setStyle("-fx-background-color: #198754; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20; -fx-opacity: 0.8;");
            registerButton.setDisable(true);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

/**
 * Updates the meeting status information based on current time and registration status
 */
private void updateMeetingStatus() {
    try {
        if (!isOnlineEvent || event.getMeetingLink() == null || event.getMeetingLink().isEmpty()) {
            meetingContainer.setVisible(false);
            meetingContainer.setManaged(false);
            return;
        }

        boolean isRegistered = reservationService.isUserRegisteredForEvent(event.getId(), currentUserName);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Default state is hidden button
        joinMeetingButton.setVisible(false);

        // Update meeting information based on registration and time status
        if (!isRegistered) {
            meetingInfoLabel.setText("Inscrivez-vous à l'événement pour obtenir l'accès au lien de réunion");
            return;
        }

        if (event.getDate().isAfter(today)) {
            meetingInfoLabel.setText("La réunion aura lieu le " + event.getDate().format(dateFormatter) +
                    " à " + event.getTime().format(timeFormatter));
            return;
        }

        if (event.getDate().isBefore(today)) {
            meetingInfoLabel.setText("Cet événement en ligne est terminé");
            return;
        }

        // Event is today, check the time
        if (now.isBefore(event.getTime().minusMinutes(15))) {
            meetingInfoLabel.setText("La réunion commencera à " + event.getTime().format(timeFormatter) +
                    ". Le lien sera disponible 15 minutes avant.");
            return;
        }

        if (event.getEndTime() != null && now.isAfter(event.getEndTime())) {
            meetingInfoLabel.setText("Cet événement en ligne est terminé");
            return;
        }

        // All conditions passed, user can join the meeting
        meetingInfoLabel.setText("La réunion est en cours. Vous pouvez y accéder maintenant.");
        joinMeetingButton.setVisible(true);
        joinMeetingButton.setDisable(false);

    } catch (SQLException e) {
        e.printStackTrace();
        meetingInfoLabel.setText("Erreur lors de la vérification de votre inscription");
    }
}

/**
 * Loads the map with event coordinates
 */
private void loadMap(String coordinates) {
    if (coordinates == null || coordinates.isEmpty()) {
        // Show no map available message
        WebEngine webEngine = mapWebView.getEngine();
        webEngine.loadContent("<html><body style='display:flex;justify-content:center;align-items:center;height:100%;font-family:Arial;color:#6c757d;'>" +
                "Aucune information de localisation disponible</body></html>");
        return;
    }

    String[] parts = coordinates.split(",");
    if (parts.length != 2) {
        return;
    }

    String lat = parts[0].trim();
    String lon = parts[1].trim();

    // Create a better-styled map with improved markers and interactions
    String mapHtml = "<html><head>"
            + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
            + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.3/dist/leaflet.css'/>"
            + "<script src='https://unpkg.com/leaflet@1.9.3/dist/leaflet.js'></script>"
            + "<style>body{margin:0;padding:0;} #map{width:100%;height:100%;border-radius:10px;}</style>"
            + "</head>"
            + "<body>"
            + "<div id='map'></div>"
            + "<script>"
            + "var map = L.map('map', {zoomControl: true}).setView([" + lat + "," + lon + "], 15);"
            + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {attribution: '&copy; OpenStreetMap contributors', maxZoom: 19}).addTo(map);"
            + "var eventIcon = L.icon({iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png', shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png', iconSize: [25, 41], iconAnchor: [12, 41], popupAnchor: [1, -34], shadowSize: [41, 41]});"
            + "var marker = L.marker([" + lat + "," + lon + "], {icon: eventIcon}).addTo(map);"
            + "marker.bindPopup('<b>" + event.getName() + "</b><br>" + event.getLocation() + "').openPopup();"
            + "map.on('click', function(e) { marker.openPopup(); });"
            + "</script>"
            + "</body></html>";

    WebEngine webEngine = mapWebView.getEngine();
    webEngine.loadContent(mapHtml);
}

/**
 * Handles the Join Online Event button click
 */
@FXML
private void handleJoinOnlineEvent() {
    try {
        if (!isOnlineEvent || event.getMeetingLink() == null || event.getMeetingLink().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Lien non disponible", "Cet événement n'a pas de lien de réunion disponible.");
            return;
        }

        if (!reservationService.isUserRegisteredForEvent(event.getId(), currentUserName)) {
            showAlert(Alert.AlertType.WARNING, "Non inscrit", "Vous devez d'abord réserver pour accéder à cet événement.");
            return;
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Check if the event is happening today
        if (event.getDate().isBefore(today)) {
            showAlert(Alert.AlertType.WARNING, "Événement passé", "Cet événement a déjà eu lieu le " +
                    event.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".");
            return;
        }

        if (event.getDate().isAfter(today)) {
            showAlert(Alert.AlertType.INFORMATION, "Événement à venir", "L'événement est prévu pour le " +
                    event.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".");
            return;
        }

        // Event is today, check time
        if (now.isBefore(event.getTime().minusMinutes(15))) {
            showAlert(Alert.AlertType.INFORMATION, "Pas encore commencé",
                    "L'événement commencera à " + event.getTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                            ". Vous pourrez y accéder 15 minutes avant le début.");
            return;
        }

        if (event.getEndTime() != null && now.isAfter(event.getEndTime())) {
            showAlert(Alert.AlertType.WARNING, "Événement terminé", "Cet événement est déjà terminé.");
            return;
        }

        // Open the meeting link in the browser
        main.mainFX.getHostServicesInstance().showDocument(event.getMeetingLink());

    } catch (SQLException e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur lors de la vérification de votre inscription.");
    } catch (Exception e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
    }
}

/**
 * Handles the registration button click
 */
@FXML
private void handleInscription() {
    try {
        if (reservationService.isUserRegisteredForEvent(event.getId(), currentUserName)) {
            showAlert(Alert.AlertType.INFORMATION, "Déjà inscrit", "Vous êtes déjà inscrit à cet événement !");
            return;
        }

        // If event is in the past, don't allow registration
        if (event.getDate().isBefore(LocalDate.now()) ||
                (event.getDate().isEqual(LocalDate.now()) && event.getEndTime() != null &&
                        LocalTime.now().isAfter(event.getEndTime()))) {
            showAlert(Alert.AlertType.WARNING, "Événement passé", "Impossible de s'inscrire à un événement passé.");
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
        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement du formulaire d'inscription.");
    }
}

/**
 * Handles the back button click
 */
@FXML
private void handleBack() {
    try {
        // Cancel any running timer before navigating away
        if (meetingStatusTimer != null) {
            meetingStatusTimer.cancel();
            meetingStatusTimer = null;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseUser.fxml"));
        Parent root = loader.load();

        BaseUserController baseUserController = loader.getController();
        baseUserController.showEventsView();

        Stage stage = (Stage) eventTitle.getScene().getWindow();
        stage.setScene(new Scene(root));
    } catch (IOException e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du retour à la liste des événements.");
    }
}

/**
 * Shows an alert dialog with the given information
 */
private void showAlert(Alert.AlertType type, String title, String message) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}
}