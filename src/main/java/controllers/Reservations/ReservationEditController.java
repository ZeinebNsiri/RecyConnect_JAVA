package controllers.Reservations;

import entities.Reservation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ReservationService;

public class ReservationEditController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Spinner<Integer> placesSpinner;
    @FXML private ComboBox<String> statusCombo;

    private Reservation reservation;
    private final ReservationService reservationService = new ReservationService();

    @FXML
    public void initialize() {
        placesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        statusCombo.getItems().addAll("Pending", "Confirmed", "Cancelled");
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
        if (reservation != null) {
            nameField.setText(reservation.getName());
            emailField.setText(reservation.getEmail());
            phoneField.setText(reservation.getPhone());
            placesSpinner.getValueFactory().setValue(reservation.getPlaces());
            statusCombo.setValue(reservation.getStatus());
        }
    }

    @FXML
    private void handleSave() {
        try {
            reservation.setName(nameField.getText());
            reservation.setEmail(emailField.getText());
            reservation.setPhone(phoneField.getText());
            reservation.setPlaces(placesSpinner.getValue());
            reservation.setStatus(statusCombo.getValue());

            reservationService.update(reservation);
            closeWindow();
        } catch (Exception e) {
            showAlert("Erreur", "Mise à jour échouée : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}