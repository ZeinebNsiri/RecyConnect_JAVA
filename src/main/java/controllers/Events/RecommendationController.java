package controllers.Events;

import entities.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import services.EventRecommender;
import services.EventService;
import services.ReservationService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

public class RecommendationController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private TilePane cardContainer; // Use TilePane for wrapping layout

    private final EventService eventService = new EventService();
    private final ReservationService reservationService = new ReservationService();
    private final EventRecommender recommender = new EventRecommender();
    private final String currentUserEmail = "eljaziamal@gmail.com"; // Replace with dynamic user later

    @FXML
    public void initialize() {
        welcomeLabel.setText("Bienvenue, " + currentUserEmail);

        try {
            List<Event> allEvents = eventService.getAllEvents();
            List<Integer> userEventIds = reservationService.getReservationsByUser(currentUserEmail)
                    .stream().map(r -> r.getEventId()).collect(Collectors.toList());

            List<Event> recommendations = recommender.recommendEvents(allEvents, userEventIds, 10);

            for (Event event : recommendations) {
                VBox card = createEventCard(event);
                cardContainer.getChildren().add(card);
            }

        } catch (SQLException | IOException e) {
            showAlert("Erreur", "Échec du chargement des recommandations :\n" + e.getMessage());
        }
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        card.setPrefWidth(300);

        Label title = new Label(event.getName());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #198754;");

        Label desc = new Label(event.getDescription());
        desc.setWrapText(true);
        desc.setMaxHeight(60);

        Label date = new Label("📅 Date : " + event.getDate());
        Label time = new Label("⏰ Heure : " + event.getTime());
        Label location = new Label("📍 Lieu : " + event.getLocation());

        Button seeMore = new Button("Voir plus");
        seeMore.setStyle("-fx-background-color: #198754; -fx-text-fill: white; -fx-font-weight: bold;");
        seeMore.setOnAction(e -> openDetails(event));

        card.getChildren().addAll(title, desc, date, time, location, seeMore);
        return card;
    }

    private void openDetails(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventViews/EventDetails.fxml"));
            Parent root = loader.load();

            EventDetailsController controller = loader.getController();
            controller.setEvent(event);

            Stage stage = (Stage) cardContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger les détails :\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
