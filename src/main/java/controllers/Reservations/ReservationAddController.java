package controllers.Reservations;

import entities.Reservation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ReservationService;

public class ReservationAddController {
/*
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Spinner<Integer> placesSpinner;
    @FXML private ComboBox<String> statusCombo;

    private final ReservationService reservationService = new ReservationService();

    @FXML
    public void initialize() {
        placesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        statusCombo.getItems().addAll("Pending", "Confirmed", "Cancelled");
    }

    @FXML
    private void handleSave() {
        try {
            Reservation r = new Reservation();
            r.setName(nameField.getText());
            r.setEmail(emailField.getText());
            r.setPhone(phoneField.getText());
            r.setPlaces(placesSpinner.getValue());
            r.setStatus(statusCombo.getValue());
            r.setEventId(0); // Set manually or dynamically if needed

            reservationService.add(r);
            closeWindow();
        } catch (Exception e) {
            showAlert("Erreur", "Échec de l'ajout : " + e.getMessage());
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
    }*/
}
