// ReservationAddController.java
package controllers.Reservations;

import entities.Reservation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ReservationService;

public class ReservationAddController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Spinner<Integer> placesSpinner;
    @FXML private ComboBox<String> statusCombo;

    @FXML private Label nameError;
    @FXML private Label emailError;
    @FXML private Label phoneError;
    @FXML private Label placesError;
    @FXML private Label statusError;

    private final ReservationService reservationService = new ReservationService();

    @FXML
    public void initialize() {
        placesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        statusCombo.getItems().addAll("active", "confirmée", "annulée");
    }

    @FXML
    private void handleSave() {
        clearErrors();

        boolean isValid = validateFields();

        if (isValid) {
            try {
                Reservation res = new Reservation(
                        nameField.getText().trim(),
                        emailField.getText().trim(),
                        phoneField.getText().trim(),
                        placesSpinner.getValue(),
                        "", // demandes_speciales optionnel
                        1 // eventId par défaut (changer si vous avez une logique dynamique)
                );
                res.setStatus(statusCombo.getValue());

                reservationService.add(res);
                showSuccess("Réservation ajoutée avec succès!");
                closeWindow();

            } catch (Exception e) {
                showError("Erreur lors de l'ajout : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateFields() {
        boolean valid = true;

        if (nameField.getText().trim().isEmpty()) {
            nameError.setText("Nom requis");
            nameError.setVisible(true);
            valid = false;
        }

        if (emailField.getText().trim().isEmpty() || !emailField.getText().contains("@")) {
            emailError.setText("Email invalide");
            emailError.setVisible(true);
            valid = false;
        }

        if (!phoneField.getText().matches("\\d{8,15}")) {
            phoneError.setText("Téléphone invalide");
            phoneError.setVisible(true);
            valid = false;
        }

        if (placesSpinner.getValue() <= 0) {
            placesError.setText("Nombre de places > 0");
            placesError.setVisible(true);
            valid = false;
        }

        if (statusCombo.getValue() == null) {
            statusError.setText("Statut requis");
            statusError.setVisible(true);
            valid = false;
        }

        return valid;
    }

    private void clearErrors() {
        nameError.setVisible(false);
        emailError.setVisible(false);
        phoneError.setVisible(false);
        placesError.setVisible(false);
        statusError.setVisible(false);
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
