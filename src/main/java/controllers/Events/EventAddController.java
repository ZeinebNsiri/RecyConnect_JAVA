package controllers.Events;

import controllers.Events.MapPickerResult;
import entities.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.EventService;
import services.ImageGenerationService;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
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
    @FXML private Label nameErrorLabel;
    @FXML private Label locationErrorLabel;
    @FXML private Label dateErrorLabel;
    @FXML private Label timeErrorLabel;
    @FXML private Label endTimeErrorLabel;
    @FXML private Label descriptionErrorLabel;
    @FXML private Label capacityErrorLabel;
    @FXML private Label remainingErrorLabel;

    private final EventService eventService = new EventService();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

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
            String meetRoomName = event.getName().trim()
                    .replaceAll("[^a-zA-Z0-9\\s]", "") // Remove any non-alphanumeric characters
                    .replaceAll("\\s+", "-")          // Replace spaces with hyphens
                    + "-" + System.currentTimeMillis();
            event.setMeetingLink("https://meet.jit.si/" + meetRoomName);            event.setMeetingLink("https://meet.jit.si/" + meetRoomName);
            event.setCoordinates(coordinatesField.getText().trim());
// Image generation using Modelslab
            String prompt = "A professional event image for: " + event.getName() + ". " + event.getDescription();
            String imageFileName = "event_" + System.currentTimeMillis() + ".png";
            String savePath = "uploads/" + imageFileName;

            try {
                Files.createDirectories(Paths.get("uploads"));
                String generatedFile = ImageGenerationService.generateImage(prompt, savePath);
                if (Files.exists(Paths.get(generatedFile))) {
                    event.setImage(imageFileName);  // Store only the filename in the database
                } else {
                    event.setImage("default.png");  // Default image if generation fails
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (e.getMessage() != null) {
                    if (e.getMessage().contains("403")) {
                        showAlert(Alert.AlertType.ERROR, "Access Denied",
                                "Your API key might be invalid or expired.");
                    } else if (e.getMessage().contains("429")) {
                        showAlert(Alert.AlertType.WARNING, "Rate Limit Reached",
                                "Too many requests. Please wait and try again.");
                    } else if (e.getMessage().contains("503")) {
                        showAlert(Alert.AlertType.WARNING, "Service Unavailable",
                                "The image generation service is temporarily unavailable.");
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Image Generation",
                                "Could not generate custom image. Using default image instead.");
                    }
                }
                event.setImage("default.png");
            }



            eventService.add(event);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Event added successfully!");
            closeForm();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add event: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;
        resetErrorLabels();

        if (nameField.getText().trim().isEmpty()) {
            nameErrorLabel.setText("Name is required.");
            nameErrorLabel.setVisible(true);
            isValid = false;
        }

        if (locationField.getText().trim().isEmpty()) {
            locationErrorLabel.setText("Location is required.");
            locationErrorLabel.setVisible(true);
            isValid = false;
        }

        if (datePicker.getValue() == null) {
            dateErrorLabel.setText("Date is required.");
            dateErrorLabel.setVisible(true);
            isValid = false;
        }

        try {
            LocalTime.parse(timeField.getText().trim(), timeFormatter);
        } catch (DateTimeParseException e) {
            timeErrorLabel.setText("Invalid time (format HH:mm).");
            timeErrorLabel.setVisible(true);
            isValid = false;
        }

        try {
            LocalTime.parse(endTimeField.getText().trim(), timeFormatter);
        } catch (DateTimeParseException e) {
            endTimeErrorLabel.setText("Invalid end time (format HH:mm).");
            endTimeErrorLabel.setVisible(true);
            isValid = false;
        }

        if (descriptionField.getText().trim().isEmpty()) {
            descriptionErrorLabel.setText("Description is required.");
            descriptionErrorLabel.setVisible(true);
            isValid = false;
        }

        try {
            int capacity = Integer.parseInt(capacityField.getText().trim());
            if (capacity <= 0) {
                capacityErrorLabel.setText("Capacity must be positive.");
                capacityErrorLabel.setVisible(true);
                isValid = false;
            }
        } catch (NumberFormatException e) {
            capacityErrorLabel.setText("Invalid capacity.");
            capacityErrorLabel.setVisible(true);
            isValid = false;
        }

        try {
            int remaining = Integer.parseInt(remainingField.getText().trim());
            if (remaining < 0) {
                remainingErrorLabel.setText("Remaining seats cannot be negative.");
                remainingErrorLabel.setVisible(true);
                isValid = false;
            }
        } catch (NumberFormatException e) {
            remainingErrorLabel.setText("Invalid remaining seats.");
            remainingErrorLabel.setVisible(true);
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

    @FXML
    private void handleOpenMap() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventViews/MapPicker.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Choose a location");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (!MapPickerResult.selectedCoordinates.isEmpty()) {
                coordinatesField.setText(MapPickerResult.selectedCoordinates);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
