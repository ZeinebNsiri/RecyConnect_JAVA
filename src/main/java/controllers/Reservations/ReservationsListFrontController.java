package controllers.Reservations;

import entities.Reservation;
import entities.Event;
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
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ReservationsListFrontController {
    @FXML private VBox reservationsContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Label debugLabel;

    private final ReservationService reservationService = new ReservationService();
    private final EventService eventService = new EventService();
    private List<Reservation> allReservations;

    @FXML private void initialize() {
        statusComboBox.getItems().setAll("Active", "Cancelled");
        statusComboBox.setValue("Active");
        loadReservations();
    }

    private void displayFilteredReservations() {
        String keyword = searchField.getText() != null ? searchField.getText().toLowerCase(Locale.ROOT).trim() : "";
        String statusFilter = statusComboBox.getValue();

        reservationsContainer.getChildren().clear();

        allReservations.stream()
                .filter(r -> {
                    try {
                        Event event = eventService.getEventById(r.getEventId());
                        return event != null && event.getName().toLowerCase().contains(keyword);
                    } catch (SQLException e) {
                        return false;
                    }
                })
                .filter(r -> r.getStatus() != null && r.getStatus().equalsIgnoreCase(statusFilter))
                .forEach(r -> {
                    try {
                        Event e = eventService.getEventById(r.getEventId());
                        if (e != null) {
                            reservationsContainer.getChildren().add(createReservationCard(r, e));
                        }
                    } catch (SQLException ex) {
                        showError("Erreur lors de la récupération de l'événement: " + ex.getMessage());
                    }
                });
    }


    private void loadReservations() {
        try {
            allReservations = reservationService.getAllReservations();
            displayFilteredReservations();
        } catch (SQLException e) {
            showError("Erreur lors du chargement des réservations: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        displayFilteredReservations();
    }

    private VBox createReservationCard(Reservation reservation, Event event) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label title = new Label("📌 " + event.getName());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label date = new Label("📅 Date : " + event.getDate());
        Label places = new Label("📦 Places réservées : " + reservation.getNbPlaces());
        places.setStyle("-fx-text-fill: green;");

        Label status = new Label("Statut : " + reservation.getStatus());
        status.setStyle("-fx-text-fill: " +
                (reservation.getStatus().equalsIgnoreCase("annulée") ? "red" : "green") + ";");

        HBox buttons = new HBox(10);
        Button modifyBtn = new Button("✏️ Modifier");
        modifyBtn.setOnAction(e -> handleEdit(reservation));

        Button cancelBtn = new Button("❌ Annuler");

        if (reservation.getStatus().equalsIgnoreCase("annulée")) {
            cancelBtn.setDisable(true);
        } else {
            cancelBtn.setOnAction(e -> handleCancel(reservation));
        }

        buttons.getChildren().addAll(modifyBtn, cancelBtn);
        card.getChildren().addAll(title, date, places, status, buttons);

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
                    loadReservations();
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
            stage.showAndWait();

            // Reload reservations after closing edit window
            loadReservations();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du formulaire d'édition : " + e.getMessage());
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
