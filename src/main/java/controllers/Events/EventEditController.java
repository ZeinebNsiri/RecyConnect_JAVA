package controllers.Events;

import entities.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import services.EventService;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EventEditController {
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
    private Event event;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public void setEvent(Event event) {
        this.event = event;
        nameField.setText(event.getName() != null ? event.getName() : "");
        locationField.setText(event.getLocation() != null ? event.getLocation() : "");
        datePicker.setValue(event.getDate());
        timeField.setText(event.getTime() != null ? event.getTime().format(timeFormatter) : "");
        endTimeField.setText(event.getEndTime() != null ? event.getEndTime().format(timeFormatter) : "");
        descriptionField.setText(event.getDescription() != null ? event.getDescription() : "");
        capacityField.setText(event.getCapacity() > 0 ? String.valueOf(event.getCapacity()) : "");
        remainingField.setText(event.getRemaining() >= 0 ? String.valueOf(event.getRemaining()) : "");
        linkField.setText(event.getMeetingLink() != null ? event.getMeetingLink() : "");
        coordinatesField.setText(event.getCoordinates() != null ? event.getCoordinates() : "");
        imageLabel.setText(event.getImage() != null ? event.getImage() : "Aucune image");
    }

    @FXML
    private void handleImageBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", ".jpg", ".jpeg", ".png", ".webp")
        );
        selectedImageFile = fileChooser.showOpenDialog(null);

        if (selectedImageFile != null) {
            imageLabel.setText(selectedImageFile.getName());
            imageErrorLabel.setVisible(false);
        }
    }

    @FXML
    private void saveChanges() {
        if (!validateInputs()) {
            return;
        }

        try {
            // Safe text field handling with null checks
            event.setName(getSafeTextFieldValue(nameField));
            event.setLocation(getSafeTextFieldValue(locationField));
            event.setDate(datePicker.getValue());

            // Safe time parsing
            event.setTime(parseTimeField(timeField));
            event.setEndTime(parseTimeField(endTimeField));

            event.setDescription(getSafeTextFieldValue(descriptionField));

            // Safe number parsing
            event.setCapacity(parseNumberField(capacityField));
            event.setRemaining(parseNumberField(remainingField));

            event.setMeetingLink(getSafeTextFieldValue(linkField));
            event.setCoordinates(getSafeTextFieldValue(coordinatesField));

            // Handle image
            if (selectedImageFile != null) {
                Path dest = Paths.get("uploads", selectedImageFile.getName());
                Files.createDirectories(dest.getParent());
                Files.copy(selectedImageFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                event.setImage(selectedImageFile.getName());
            } else if (event.getImage() == null) {
                event.setImage("default.jpg");
            }

            eventService.update(event);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement modifié avec succès.");
            navigateBackToList();
        } catch (SQLException | IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la modification : " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur inattendue", "Une erreur est survenue : " + e.getMessage());
        }
    }

    // Helper methods for safe field handling
    private String getSafeTextFieldValue(TextField field) {
        return field.getText() != null ? field.getText().trim() : "";
    }

    private LocalTime parseTimeField(TextField field) throws DateTimeParseException {
        String text = field.getText();
        return (text != null && !text.trim().isEmpty()) ?
                LocalTime.parse(text.trim(), timeFormatter) : null;
    }

    private int parseNumberField(TextField field) throws NumberFormatException {
        String text = field.getText();
        return (text != null && !text.trim().isEmpty()) ?
                Integer.parseInt(text.trim()) : 0;
    }

    private boolean validateInputs() {
        boolean isValid = true;
        resetErrorLabels();

        // Name validation
        if (getSafeTextFieldValue(nameField).isEmpty()) {
            nameErrorLabel.setText("Le nom est requis.");
            nameErrorLabel.setVisible(true);
            isValid = false;
        }

        // Location validation
        if (getSafeTextFieldValue(locationField).isEmpty()) {
            locationErrorLabel.setText("Le lieu est requis.");
            locationErrorLabel.setVisible(true);
            isValid = false;
        }

        // Date validation
        if (datePicker.getValue() == null) {
            dateErrorLabel.setText("La date est requise.");
            dateErrorLabel.setVisible(true);
            isValid = false;
        }

        // Time validation
        try {
            parseTimeField(timeField);
        } catch (DateTimeParseException e) {
            timeErrorLabel.setText("Heure invalide (format HH:mm).");
            timeErrorLabel.setVisible(true);
            isValid = false;
        }

        // End time validation
        try {
            parseTimeField(endTimeField);
        } catch (DateTimeParseException e) {
            endTimeErrorLabel.setText("Heure de fin invalide (format HH:mm).");
            endTimeErrorLabel.setVisible(true);
            isValid = false;
        }

        // Description validation
        if (getSafeTextFieldValue(descriptionField).isEmpty()) {
            descriptionErrorLabel.setText("La description est requise.");
            descriptionErrorLabel.setVisible(true);
            isValid = false;
        }

        // Capacity validation
        try {
            int capacity = parseNumberField(capacityField);
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

        // Remaining validation
        try {
            int remaining = parseNumberField(remainingField);
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

        // Image validation
        if (selectedImageFile == null && (event.getImage() == null || event.getImage().isEmpty())) {
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
    private void cancelEdit() {
        navigateBackToList();
    }

    private void navigateBackToList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventViews/EventList.fxml"));
            Parent listView = loader.load();

            Scene currentScene = nameField.getScene();
            StackPane contentPane = (StackPane) currentScene.lookup("#contentPane");

            if (contentPane != null) {
                contentPane.getChildren().setAll(listView);
            } else {
                System.err.println("❌ contentPane not found");
                closeForm();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de revenir à la liste des événements.");
            closeForm();
        }
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

