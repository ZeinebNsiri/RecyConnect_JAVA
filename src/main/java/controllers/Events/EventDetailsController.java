package controllers.Events;

import controllers.BaseUserController;
import controllers.Events.MapViewerController;
import entities.Event;
import entities.utilisateur;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import controllers.Reservations.EventReservationController;
import services.ReservationService;
import controllers.Events.MapViewerController;

import javax.swing.*;
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
    @FXML private AnchorPane mapSwingContainer;
    @FXML private Button joinMeetingButton;
    @FXML private Label meetingInfoLabel;
    @FXML private HBox meetingContainer;
    @FXML private Label onlineEventLabel;
    @FXML private VBox onlineEventContainer;
    @FXML private VBox mapContainer;
    @FXML private Button registerButton;

    private Event event;
    private boolean isOnlineEvent = false;
    private final ReservationService reservationService = new ReservationService();
    private utilisateur user = utils.Session.getInstance().getCurrentUser();
    private String currentUserName = user.getNom_user(); // replace with logged-in user if needed
    private Timer meetingStatusTimer;

    @FXML
    public void setEvent(Event event) {
        this.event = event;

        isOnlineEvent = "en ligne".equalsIgnoreCase(event.getLocation());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        eventTitle.setText(event.getName());
        eventDateTime.setText(event.getDate().format(dateFormatter) + " à " + event.getTime().format(timeFormatter));
        eventDescription.setText(event.getDescription());
        eventPlaces.setText("Places disponibles : " + event.getRemaining());
        eventDate.setText("📅 Date : " + event.getDate().format(dateFormatter));

        if (event.getEndTime() != null) {
            eventPeriod.setText("⏳ De " + event.getTime().format(timeFormatter) + " à " + event.getEndTime().format(timeFormatter));
        } else {
            eventPeriod.setText("⏳ Début : " + event.getTime().format(timeFormatter));
        }

        eventLocation.setText("📍 Lieu : " + event.getLocation());

        File imageFile = new File("uploads/" + event.getImage());
        if (imageFile.exists()) {
            eventImageView.setImage(new Image(imageFile.toURI().toString()));
        } else {
            eventImageView.setImage(new Image(getClass().getResourceAsStream("/images/default_event.png")));
        }

        setupEventTypeSpecificUI();
    }

    private void setupEventTypeSpecificUI() {
        if (isOnlineEvent) {
            mapContainer.setVisible(false);
            mapContainer.setManaged(false);

            onlineEventContainer.setVisible(true);
            onlineEventContainer.setManaged(true);
            onlineEventLabel.setText("🧑‍💻 Événement en ligne");
            onlineEventLabel.setVisible(true);

            meetingContainer.setVisible(true);
            meetingContainer.setManaged(true);

            updateMeetingStatus();

            if (meetingStatusTimer != null) {
                meetingStatusTimer.cancel();
            }

            meetingStatusTimer = new Timer(true);
            meetingStatusTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> updateMeetingStatus());
                }
            }, 0, 60000);

        } else {
            onlineEventContainer.setVisible(false);
            onlineEventContainer.setManaged(false);
            onlineEventLabel.setVisible(false);
            meetingContainer.setVisible(false);
            meetingContainer.setManaged(false);

            mapContainer.setVisible(true);
            mapContainer.setManaged(true);

            loadMap(event.getCoordinates());
        }

        try {
            boolean isRegistered = reservationService.isUserRegisteredForEvent(event.getId(), currentUserName);
            if (isRegistered) {
                registerButton.setText("✅ Déjà inscrit");
                registerButton.setStyle("-fx-background-color: #198754; -fx-text-fill: white;");
                registerButton.setDisable(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMap(String coordinates) {
        if (coordinates == null || coordinates.isEmpty()) {
            System.out.println("Coordonnées manquantes.");
            return;
        }

        String[] parts = coordinates.split(",");
        if (parts.length != 2) {
            System.out.println("Format de coordonnées invalide : " + coordinates);
            return;
        }

        try {
            double lat = Double.parseDouble(parts[0].trim());
            double lon = Double.parseDouble(parts[1].trim());
            showMap(lat, lon, event.getLocation());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void showMap(double lat, double lon, String name) {
        Platform.runLater(() -> {
            SwingNode swingNode = new SwingNode();
            mapSwingContainer.getChildren().clear();
            mapSwingContainer.getChildren().add(swingNode);

            // Set all anchor constraints to 0
            AnchorPane.setTopAnchor(swingNode, 0.0);
            AnchorPane.setBottomAnchor(swingNode, 0.0);
            AnchorPane.setLeftAnchor(swingNode, 0.0);
            AnchorPane.setRightAnchor(swingNode, 0.0);

            SwingUtilities.invokeLater(() -> {
                MapViewerController mapViewer = new MapViewerController();
                JPanel mapPanel = mapViewer.createMapPanel(lat, lon, name);
                swingNode.setContent(mapPanel);
            });
        });
    }

    @FXML
    private void handleInscription() {
        try {
            if (reservationService.isUserRegisteredForEvent(event.getId(), currentUserName)) {
                showAlert(Alert.AlertType.INFORMATION, "Déjà inscrit", "Vous êtes déjà inscrit à cet événement !");
                return;
            }

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
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }

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

            main.mainFX.getHostServicesInstance().showDocument(event.getMeetingLink());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }

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
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            joinMeetingButton.setVisible(false);

            if (!isRegistered) {
                meetingInfoLabel.setText("Inscrivez-vous à l'événement pour obtenir l'accès au lien de réunion");
                return;
            }

            if (event.getDate().isAfter(today)) {
                meetingInfoLabel.setText("La réunion aura lieu le " + event.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                        " à " + event.getTime().format(timeFormatter));
                return;
            }

            if (event.getDate().isBefore(today)) {
                meetingInfoLabel.setText("Cet événement en ligne est terminé");
                return;
            }

            if (now.isBefore(event.getTime().minusMinutes(15))) {
                meetingInfoLabel.setText("La réunion commencera à " + event.getTime().format(timeFormatter) +
                        ". Le lien sera disponible 15 minutes avant.");
                return;
            }

            if (event.getEndTime() != null && now.isAfter(event.getEndTime())) {
                meetingInfoLabel.setText("Cet événement en ligne est terminé");
                return;
            }

            meetingInfoLabel.setText("La réunion est en cours. Vous pouvez y accéder maintenant.");
            joinMeetingButton.setVisible(true);
            joinMeetingButton.setDisable(false);

        } catch (SQLException e) {
            e.printStackTrace();
            meetingInfoLabel.setText("Erreur lors de la vérification de votre inscription");
        }
    }

    @FXML
    private void handleBack() {
        try {
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
