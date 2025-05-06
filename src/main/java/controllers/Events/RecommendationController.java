package controllers.Events;

import controllers.BaseUserController;
import entities.Event;
import entities.utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import services.EventRecommender;
import services.EventService;
import services.ReservationService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecommendationController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private TilePane cardContainer;

    private final EventService eventService = new EventService();
    private final ReservationService reservationService = new ReservationService();
    private final EventRecommender recommender = new EventRecommender();
    private utilisateur user = utils.Session.getInstance().getCurrentUser();
    private final String currentUserEmail = user.getEmail();
    @FXML
    public void initialize() {
        try {
            welcomeLabel.setText("Bienvenue, " + currentUserEmail);
            loadRecommendations();
        } catch (Exception e) {
            showAlert("Erreur d'initialisation", "Une erreur est survenue lors de l'initialisation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadRecommendations() {
        try {
            // Clear existing cards first
            if (cardContainer != null) {
                cardContainer.getChildren().clear();
            }

            // Get all events
            List<Event> allEvents;
            try {
                allEvents = eventService.getAllEvents();
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de charger les événements: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            // Get user reservations
            List<Integer> userEventIds;
            try {
                userEventIds = reservationService.getReservationsByUser(currentUserEmail)
                        .stream()
                        .map(r -> r.getEventId())
                        .collect(Collectors.toList());
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de charger vos réservations: " + e.getMessage());
                e.printStackTrace();
                userEventIds = new ArrayList<>(); // Empty list as fallback
            }

            // Get recommendations
            List<Event> recommendations;
            try {
                recommendations = recommender.recommendEvents(allEvents, userEventIds, 10);
            } catch (Exception e) {
                showAlert("Erreur", "Échec des recommandations: " + e.getMessage());
                e.printStackTrace();
                recommendations = new ArrayList<>(); // Empty list as fallback
            }

            // Display recommendations
            if (recommendations.isEmpty()) {
                Label noRecommendationsLabel = new Label("Aucune recommandation disponible pour le moment");
                noRecommendationsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d; -fx-padding: 20;");
                cardContainer.getChildren().add(noRecommendationsLabel);
            } else {
                for (Event event : recommendations) {
                    try {
                        VBox card = createEventCard(event);
                        cardContainer.getChildren().add(card);
                    } catch (Exception e) {
                        System.err.println("Error creating card for event " + event.getId() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            showAlert("Erreur", "Échec du chargement des recommandations: " + e.getMessage());
            e.printStackTrace();
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

        Button seeMore = new Button("Voir plus");
        seeMore.setStyle("-fx-background-color: #198754; -fx-text-fill: white; -fx-font-weight: bold;");

        // Use BaseUserController to show details
        seeMore.setOnAction(e -> {
            try {
                if (BaseUserController.instance != null) {
                    BaseUserController.instance.showEventDetails(event);
                } else {
                    showAlert("Erreur", "BaseUserController n'est pas initialisé");
                }
            } catch (Exception ex) {
                showAlert("Erreur", "Impossible d'afficher les détails: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        card.getChildren().addAll(title, desc, date, time,seeMore);
        return card;
    }

    @FXML
    private void handleBackToEvents() {
        try {
            if (BaseUserController.instance != null) {
                BaseUserController.instance.showEventsView();
            } else {
                showAlert("Erreur de navigation", "BaseUserController n'est pas initialisé");
            }
        } catch (Exception e) {
            showAlert("Erreur de navigation", "Impossible de retourner à la liste des événements: " + e.getMessage());
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