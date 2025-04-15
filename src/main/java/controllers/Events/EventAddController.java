package controllers.Events;

import entities.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import services.EventService;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EventAddController {
    @FXML private TextField nameField;
    @FXML private TextField locationField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField endTimeField;
    @FXML private TextField descriptionField;
    @FXML private TextField capacityField;
    @FXML private TextField remainingField;
    @FXML private TextField linkField;
    @FXML private TextField coordinatesField;
    @FXML private Label imageLabel;
    @FXML private Label nameErrorLabel;
    @FXML private Label locationErrorLabel;
    @FXML private Label dateErrorLabel;
    @FXML private Label timeErrorLabel;
    @FXML private Label endTimeErrorLabel;
    @FXML private Label descriptionErrorLabel;
    @FXML private Label capacityErrorLabel;
    @FXML private Label remainingErrorLabel;
    @FXML private Label imageErrorLabel;

    private File selectedImageFile;
    private final EventService eventService = new EventService();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private void handleImageBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.webp")
        );
        selectedImageFile = fileChooser.showOpenDialog(null);

        if (selectedImageFile != null) {
            imageLabel.setText(selectedImageFile.getName());
            imageErrorLabel.setVisible(false);
        } else {
            imageLabel.setText("Aucune image choisie");
        }
    }

    @FXML
    private void saveEvent() {
        if (!validateInputs()) {
            return;
        }

        try {
            Event event = new Event();
            event.setName(nameField.getText().trim());
            event.setLocation(locationField.getText().trim());
            event.setDate(datePicker.getValue());
            event.setTime(LocalTime.parse(timeField.getText().trim(), timeFormatter));
            event.setEndTime(LocalTime.parse(endTimeField.getText().trim(), timeFormatter));
            event.setDescription(descriptionField.getText().trim());
            event.setCapacity(Integer.parseInt(capacityField.getText().trim()));
            event.setRemaining(Integer.parseInt(remainingField.getText().trim()));
            event.setMeetingLink(linkField.getText().trim());
            event.setCoordinates(coordinatesField.getText().trim());

            if (selectedImageFile != null) {
                Path dest = Paths.get("uploads", selectedImageFile.getName());
                Files.createDirectories(dest.getParent());
                Files.copy(selectedImageFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                event.setImage(selectedImageFile.getName());
            } else {
                event.setImage("default.png");
            }

            eventService.add(event);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement ajouté avec succès.");
            closeForm();
        } catch (SQLException | IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout : " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;
        resetErrorLabels();

        if (nameField.getText().trim().isEmpty()) {
            nameErrorLabel.setText("Le nom est requis.");
            nameErrorLabel.setVisible(true);
            isValid = false;
        }

        if (locationField.getText().trim().isEmpty()) {
            locationErrorLabel.setText("Le lieu est requis.");
            locationErrorLabel.setVisible(true);
            isValid = false;
        }

        if (datePicker.getValue() == null) {
            dateErrorLabel.setText("La date est requise.");
            dateErrorLabel.setVisible(true);
            isValid = false;
        }

        try {
            LocalTime.parse(timeField.getText().trim(), timeFormatter);
        } catch (DateTimeParseException e) {
            timeErrorLabel.setText("Heure invalide (format HH:mm).");
            timeErrorLabel.setVisible(true);
            isValid = false;
        }

        try {
            LocalTime.parse(endTimeField.getText().trim(), timeFormatter);
        } catch (DateTimeParseException e) {
            endTimeErrorLabel.setText("Heure de fin invalide (format HH:mm).");
            endTimeErrorLabel.setVisible(true);
            isValid = false;
        }

        if (descriptionField.getText().trim().isEmpty()) {
            descriptionErrorLabel.setText("La description est requise.");
            descriptionErrorLabel.setVisible(true);
            isValid = false;
        }

        try {
            int capacity = Integer.parseInt(capacityField.getText().trim());
            if (capacity <= 0) {
                capacityErrorLabel.setText("Capacité doit être positive.");
                capacityErrorLabel.setVisible(true);
                isValid = false;
            }
        } catch (NumberFormatException e) {
            capacityErrorLabel.setText("Capacité invalide.");
            capacityErrorLabel.setVisible(true);
            isValid = false;
        }

        try {
            int remaining = Integer.parseInt(remainingField.getText().trim());
            if (remaining < 0) {
                remainingErrorLabel.setText("Places restantes ne peuvent être négatives.");
                remainingErrorLabel.setVisible(true);
                isValid = false;
            }
        } catch (NumberFormatException e) {
            remainingErrorLabel.setText("Places restantes invalides.");
            remainingErrorLabel.setVisible(true);
            isValid = false;
        }

        if (selectedImageFile == null) {
            imageErrorLabel.setText("Une image doit être sélectionnée.");
            imageErrorLabel.setVisible(true);
            isValid = false;
        }

        return isValid;
    }

    private void resetErrorLabels() {
        nameErrorLabel.setVisible(false);
        locationErrorLabel.setVisible(false);
        dateErrorLabel.setVisible(false);
        timeErrorLabel.setVisible(false);
        endTimeErrorLabel.setVisible(false);
        descriptionErrorLabel.setVisible(false);
        capacityErrorLabel.setVisible(false);
        remainingErrorLabel.setVisible(false);
        imageErrorLabel.setVisible(false);
    }

    @FXML
    private void cancelForm() {
        closeForm();
    }

    private void closeForm() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}