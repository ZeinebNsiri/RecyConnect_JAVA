    // ReservationEditController.java
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

        @FXML private Label nameError;
        @FXML private Label emailError;
        @FXML private Label phoneError;
        @FXML private Label placesError;

        private Reservation reservation;
        private final ReservationService reservationService = new ReservationService();

        @FXML
        public void initialize() {
            placesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        }

        public void setReservation(Reservation reservation) {
            this.reservation = reservation;
            if (reservation != null) {
                nameField.setText(reservation.getNom());
                emailField.setText(reservation.getEmail());
                phoneField.setText(reservation.getNumTel());
                placesSpinner.getValueFactory().setValue(reservation.getNbPlaces());
            }
        }

        @FXML
        private void handleSave() {
            clearErrors();
            boolean isValid = validateFields();

            if (isValid && reservation != null) {
                try {
                    reservation.setNom(nameField.getText().trim());
                    reservation.setEmail(emailField.getText().trim());
                    reservation.setNumTel(phoneField.getText().trim());
                    reservation.setNbPlaces(placesSpinner.getValue());

                    reservationService.update(reservation);
                    showSuccess("Réservation mise à jour avec succès!");
                    closeWindow();
                } catch (Exception e) {
                    showError("Erreur : " + e.getMessage());
                }
            }
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
                placesError.setText("Places > 0");
                placesError.setVisible(true);
                valid = false;
            }



            return valid;
        }

        private void clearErrors() {
            nameError.setVisible(false);
            emailError.setVisible(false);
            phoneError.setVisible(false);
            placesError.setVisible(false);
        }

        @FXML
        private void handleCancel() {
            closeWindow();
        }

        private void closeWindow() {
            controllers.BaseUserController.instance.loadMyReservationsView();
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
