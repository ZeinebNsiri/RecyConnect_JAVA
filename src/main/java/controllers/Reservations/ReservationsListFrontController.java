// ReservationsListFrontController.java
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

public class ReservationsListFrontController {
    @FXML private VBox reservationsContainer;
    @FXML private Label debugLabel;

    private final ReservationService reservationService = new ReservationService();
    private final EventService eventService = new EventService();

    @FXML
    public void initialize() {
        loadReservations();
    }

    private void loadReservations() {
        try {
            reservationsContainer.getChildren().clear();
            List<Reservation> reservations = reservationService.getAllReservations();

            for (Reservation reservation : reservations) {
                try {
                    Event event = eventService.getEventById(reservation.getEventId());
                    if (event == null) continue;

                    VBox card = createReservationCard(reservation, event);
                    reservationsContainer.getChildren().add(card);
                } catch (SQLException e) {
                    showError("Erreur lors du chargement de l'événement: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des réservations: " + e.getMessage());
        }
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
        modifyBtn.setStyle("-fx-background-color: #e0f3ec; -fx-text-fill: #198754; -fx-border-color: #198754;");

        Button cancelBtn = new Button("❌ Annuler");
        cancelBtn.setStyle("-fx-background-color: #fef0f0; -fx-text-fill: #dc3545; -fx-border-color: #dc3545;");

        if (reservation.getStatus().equalsIgnoreCase("annulée")) {
            cancelBtn.setDisable(true);
            Label info = new Label("❗ Cette réservation est annulée.");
            info.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
            card.getChildren().add(info);
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
        confirm.setHeaderText("Êtes-vous sûr de vouloir annuler cette réservation ?");
        confirm.setContentText("Cette action est irréversible.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    reservationService.cancelReservation(reservation.getId());
                    showSuccess("Réservation annulée avec succès!");
                    debugLabel.setText("DEBUG: Réservation ID " + reservation.getId() + " annulée.");
                    loadReservations();
                } catch (SQLException e) {
                    showError("Erreur lors de l'annulation: " + e.getMessage());
                    debugLabel.setText("DEBUG: Échec annulation pour ID " + reservation.getId());
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

            debugLabel.setText("DEBUG: Réservation ID " + reservation.getId() + " mise à jour.");
            loadReservations();
        } catch (IOException e) {
            showError("Erreur lors du chargement de la fenêtre d'édition: " + e.getMessage());
            debugLabel.setText("DEBUG: Échec ouverture édition pour ID " + reservation.getId());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
