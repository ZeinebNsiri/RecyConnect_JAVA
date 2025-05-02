package controllers.Reservations;

import entities.Reservation;
import entities.Event;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.ReservationService;
import services.EventService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ReservationsListFrontController {
    @FXML private VBox reservationsContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Label debugLabel;

    private final ReservationService reservationService = new ReservationService();
    private final EventService eventService = new EventService();
    private ObservableList<Reservation> allReservations;
    private ObservableList<Reservation> filteredReservations;
    private Map<Integer, Event> eventsCache = new HashMap<>();

    @FXML
    private void initialize() {
        // Setup status filter options
        statusComboBox.getItems().setAll("Active", "Cancelled", "All");
        statusComboBox.setValue("Active");

        try {
            // Load reservations
            List<Reservation> reservations = reservationService.getAllReservations();
            allReservations = FXCollections.observableArrayList(reservations);
            filteredReservations = FXCollections.observableArrayList(allReservations);

            // Cache events to avoid repeated database calls
            cacheEvents();

            // Add listener to update UI when filtered list changes
            filteredReservations.addListener((ListChangeListener<Reservation>) change -> {
                displayReservations(filteredReservations);
            });

            // Set up dynamic search listeners
            setupDynamicSearchListeners();

            // Initial filter application
            applyFilters();
        } catch (SQLException e) {
            showError("Erreur lors du chargement des réservations: " + e.getMessage());
        }
    }

    private void cacheEvents() {
        try {
            List<Event> events = eventService.getAllEvents();
            for (Event event : events) {
                eventsCache.put(event.getId(), event);
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des événements: " + e.getMessage());
        }
    }

    private void setupDynamicSearchListeners() {
        // Add search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // Add status combo box listener
        statusComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void applyFilters() {
        String keyword = searchField.getText() != null ? searchField.getText().toLowerCase(Locale.ROOT).trim() : "";
        String statusFilter = statusComboBox.getValue();

        List<Reservation> filtered = allReservations.stream()
                .filter(r -> {
                    Event event = eventsCache.get(r.getEventId());
                    if (event == null) return false;

                    // Search in event name, user name, and email
                    return event.getName().toLowerCase().contains(keyword) ||
                            r.getNom().toLowerCase().contains(keyword) ||
                            r.getEmail().toLowerCase().contains(keyword);
                })
                .filter(r -> {
                    if ("All".equalsIgnoreCase(statusFilter)) return true;
                    return r.getStatus() != null && r.getStatus().equalsIgnoreCase(statusFilter);
                })
                .collect(Collectors.toList());

        // Update filtered list
        filteredReservations.clear();
        filteredReservations.addAll(filtered);
    }

    private void displayReservations(List<Reservation> reservations) {
        reservationsContainer.getChildren().clear();

        if (reservations.isEmpty()) {
            Label noResultsLabel = new Label("Aucune réservation ne correspond à votre recherche");
            noResultsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d; -fx-padding: 20;");
            reservationsContainer.getChildren().add(noResultsLabel);
        } else {
            for (Reservation reservation : reservations) {
                Event event = eventsCache.get(reservation.getEventId());
                if (event != null) {
                    reservationsContainer.getChildren().add(createReservationCard(reservation, event));
                }
            }
        }
    }

    @FXML
    private void handleSearch() {
        // Method kept for backward compatibility with the search button
        applyFilters();
    }

    private VBox createReservationCard(Reservation reservation, Event event) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label title = new Label("📌 " + event.getName());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label userInfo = new Label("👤 " + reservation.getNom() + " (" + reservation.getEmail() + ")");
        Label date = new Label("📅 Date : " + event.getDate());
        Label places = new Label("📦 Places réservées : " + reservation.getNbPlaces());
        places.setStyle("-fx-text-fill: green;");

        Label status = new Label("Statut : " + reservation.getStatus());
        status.setStyle("-fx-text-fill: " +
                (reservation.getStatus().equalsIgnoreCase("cancelled") ? "red" : "green") + ";");

        HBox buttons = new HBox(10);
        Button modifyBtn = new Button("✏️ Modifier");
        modifyBtn.setStyle("-fx-background-color: #0d6efd; -fx-text-fill: white;");
        modifyBtn.setOnAction(e -> handleEdit(reservation));

        Button cancelBtn = new Button("❌ Annuler");
        cancelBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

        if (reservation.getStatus().equalsIgnoreCase("cancelled")) {
            cancelBtn.setDisable(true);
        } else {
            cancelBtn.setOnAction(e -> handleCancel(reservation));
        }

        buttons.getChildren().addAll(modifyBtn, cancelBtn);
        card.getChildren().addAll(title, userInfo, date, places, status, buttons);

        return card;
    }

    private void handleCancel(Reservation reservation) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Voulez-vous annuler cette réservation ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    reservationService.cancelReservation(reservation.getId());
                    showSuccess("Réservation annulée !");

                    // Update the reservation status in our lists
                    reservation.setStatus("cancelled");

                    // Refresh the display
                    applyFilters();
                } catch (SQLException e) {
                    showError("Erreur: " + e.getMessage());
                }
            }
        });
    }

    private void handleEdit(Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReservationViews/ReservationEdit.fxml"));
            Parent root = loader.load();

            ReservationEditController controller = loader.getController();
            controller.setReservation(reservation);

            Stage stage = new Stage();
            stage.setTitle("Modifier la réservation");
            stage.setScene(new Scene(root));

            // Add listener to detect when edit window is closed
            stage.setOnHidden(event -> {
                reloadReservations(); // Refresh data after edit
            });

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du formulaire d'édition : " + e.getMessage());
        }
    }

    private void reloadReservations() {
        try {
            allReservations.clear();
            allReservations.addAll(reservationService.getAllReservations());
            cacheEvents(); // Refresh events cache
            applyFilters(); // Reapply current filters
        } catch (SQLException e) {
            showError("Erreur lors du rechargement des réservations: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText(message);
        alert.showAndWait();
    }
}