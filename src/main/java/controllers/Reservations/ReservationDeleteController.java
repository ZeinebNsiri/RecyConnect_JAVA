package controllers.Reservations;

import entities.Reservation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ReservationService;

public class ReservationDeleteController {

    @FXML private Label nameLabel;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;

    private Reservation reservation;
    private final ReservationService reservationService = new ReservationService();

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
        if (reservation != null) {
            nameLabel.setText("Supprimer la réservation de : " + reservation.getName());
        } else {
            nameLabel.setText("Réservation inconnue.");
        }
    }

    @FXML
    private void confirmDelete() {
        if (reservation != null) {
            try {
                reservationService.delete(reservation);
                closeWindow();
            } catch (Exception e) {
                showAlert("Erreur", "Échec de la suppression : " + e.getMessage());
            }
        }
    }

    @FXML
    private void cancelDelete() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
