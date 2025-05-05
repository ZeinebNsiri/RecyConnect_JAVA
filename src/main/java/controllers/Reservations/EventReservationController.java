package controllers.Reservations;

import controllers.BaseUserController;
import entities.Event;
import entities.Reservation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import services.ReservationService;
import services.EventService;

import java.io.IOException;
import java.util.regex.Pattern;

public class EventReservationController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField placesField;
    @FXML private TextArea specialRequestField;

    @FXML private Label nameErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label phoneErrorLabel;
    @FXML private Label placesErrorLabel;

    @FXML private Label titleLabel;
    @FXML private Label eventInfoLabel;
    @FXML private Label remainingPlacesLabel;
    @FXML private Button submitButton;

    private Event event;
    private final ReservationService reservationService = new ReservationService();

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+\\d{1,3}[- ]?)?\\d{8,15}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} .'-]+$");

    public void setEvent(Event event) {
        this.event = event;
        updateUI();
        setupFieldValidators();
    }

    private void updateUI() {
        titleLabel.setText("Inscription à l'événement : " + event.getName());
        remainingPlacesLabel.setText("\ud83d\udce6 Places restantes : " + event.getRemaining());
    }

    private void setupFieldValidators() {
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!NAME_PATTERN.matcher(newVal).matches() && !newVal.isEmpty()) {
                nameErrorLabel.setText("Nom invalide (caractères spéciaux non autorisés)");
                nameField.setStyle("-fx-border-color: #ff4444;");
            } else {
                nameErrorLabel.setText("");
                nameField.setStyle("");
            }
            updateSubmitButtonState();
        });

        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!EMAIL_PATTERN.matcher(newVal).matches() && !newVal.isEmpty()) {
                emailErrorLabel.setText("Format email invalide (exemple@domaine.com)");
                emailField.setStyle("-fx-border-color: #ff4444;");
            } else {
                emailErrorLabel.setText("");
                emailField.setStyle("");
            }
            updateSubmitButtonState();
        });

        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!PHONE_PATTERN.matcher(newVal).matches() && !newVal.isEmpty()) {
                phoneErrorLabel.setText("Format téléphone invalide (8-15 chiffres)");
                phoneField.setStyle("-fx-border-color: #ff4444;");
            } else {
                phoneErrorLabel.setText("");
                phoneField.setStyle("");
            }
            updateSubmitButtonState();
        });

        placesField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if (!newVal.isEmpty()) {
                    int places = Integer.parseInt(newVal);
                    if (places <= 0) {
                        placesErrorLabel.setText("Doit être > 0");
                        placesField.setStyle("-fx-border-color: #ff4444;");
                    } else if (places > event.getRemaining()) {
                        placesErrorLabel.setText("Seulement " + event.getRemaining() + " places disponibles");
                        placesField.setStyle("-fx-border-color: #ff4444;");
                    } else {
                        placesErrorLabel.setText("");
                        placesField.setStyle("");
                    }
                } else {
                    placesErrorLabel.setText("");
                    placesField.setStyle("");
                }
            } catch (NumberFormatException e) {
                placesErrorLabel.setText("Nombre invalide");
                placesField.setStyle("-fx-border-color: #ff4444;");
            }
            updateSubmitButtonState();
        });

        placesField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (!event.getCharacter().matches("\\d")) {
                event.consume();
            }
        });
    }

    private void updateSubmitButtonState() {
        boolean isValid =
                !nameField.getText().trim().isEmpty() &&
                        !emailField.getText().trim().isEmpty() &&
                        !phoneField.getText().trim().isEmpty() &&
                        !placesField.getText().trim().isEmpty() &&
                        nameErrorLabel.getText().isEmpty() &&
                        emailErrorLabel.getText().isEmpty() &&
                        phoneErrorLabel.getText().isEmpty() &&
                        placesErrorLabel.getText().isEmpty();

        submitButton.setDisable(!isValid);
    }

    @FXML
    private void handleSubmit() {
        if (!validateAllFields()) return;

        try {
            Reservation reservation = new Reservation(
                    nameField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    Integer.parseInt(placesField.getText().trim()),
                    specialRequestField.getText().trim(),
                    event.getId()
            );

            reservationService.add(reservation);
            new EventService().decrementRemainingPlaces(event.getId(), reservation.getNbPlaces());

            showSuccessAlert("\u2705 Réservation enregistrée avec succès!");

            // Redirect to reservations list right after success message
            navigateToReservationsListFront();
        } catch (Exception e) {
            showErrorAlert("\u274c Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateAllFields() {
        boolean isValid = true;

        if (nameField.getText().trim().isEmpty()) {
            nameErrorLabel.setText("Le nom est requis");
            isValid = false;
        }

        if (emailField.getText().trim().isEmpty()) {
            emailErrorLabel.setText("L'email est requis");
            isValid = false;
        } else if (!EMAIL_PATTERN.matcher(emailField.getText()).matches()) {
            emailErrorLabel.setText("Format email invalide");
            isValid = false;
        }

        if (phoneField.getText().trim().isEmpty()) {
            phoneErrorLabel.setText("Le téléphone est requis");
            isValid = false;
        } else if (!PHONE_PATTERN.matcher(phoneField.getText()).matches()) {
            phoneErrorLabel.setText("Format téléphone invalide");
            isValid = false;
        }

        if (placesField.getText().trim().isEmpty()) {
            placesErrorLabel.setText("Le nombre de places est requis");
            isValid = false;
        } else {
            try {
                int places = Integer.parseInt(placesField.getText().trim());
                if (places <= 0) {
                    placesErrorLabel.setText("Doit être > 0");
                    isValid = false;
                } else if (places > event.getRemaining()) {
                    placesErrorLabel.setText("Seulement " + event.getRemaining() + " places disponibles");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                placesErrorLabel.setText("Nombre invalide");
                isValid = false;
            }
        }

        return isValid;
    }

    private void navigateToReservationsListFront() throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseUser.fxml"));
            Parent root = loader.load();
            BaseUserController baseuserController = loader.getController();
            baseuserController.loadMyReservationsView();
            phoneField.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventViews/EventDetails.fxml"));
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            showErrorAlert("Erreur de navigation: " + e.getMessage());
        }
    }
}