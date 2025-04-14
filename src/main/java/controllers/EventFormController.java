package controllers;

import entities.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.EventService;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class EventFormController {
    // Form fields
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField locationField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField imageField;
    @FXML private TextField capacityField;
    @FXML private TextField meetingLinkField;
    @FXML private TextField coordinatesField;
    @FXML private TextField endTimeField;

    private final EventService eventService = new EventService();
    private Event currentEvent; // For edit mode

    // Initialize form (called automatically by JavaFX)
    @FXML
    public void initialize() {
        // Set default time values
        timeField.setText("10:00");
        endTimeField.setText("12:00");
    }

    // Set event data when editing
    public void setEvent(Event event) {
        this.currentEvent = event;
        nameField.setText(event.getName());
        descriptionField.setText(event.getDescription());
        locationField.setText(event.getLocation());
        datePicker.setValue(event.getDate());
        timeField.setText(event.getTime().toString());
        imageField.setText(event.getImage());
        capacityField.setText(String.valueOf(event.getCapacity()));
        meetingLinkField.setText(event.getMeetingLink());
        coordinatesField.setText(event.getCoordinates());
        endTimeField.setText(event.getEndTime().toString());
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) return;

        try {
            Event event = new Event(
                    (currentEvent != null) ? currentEvent.getId() : 0,
                    nameField.getText(),
                    descriptionField.getText(),
                    locationField.getText(),
                    datePicker.getValue(),
                    LocalTime.parse(timeField.getText()),
                    imageField.getText(),
                    Integer.parseInt(capacityField.getText()),
                    Integer.parseInt(capacityField.getText()), // Initial remaining = capacity
                    meetingLinkField.getText(),
                    coordinatesField.getText(),
                    LocalTime.parse(endTimeField.getText())
            );

            if (currentEvent == null) {
                // Add new event
                int newId = eventService.addEvent(event);
                System.out.println("Added new event with ID: " + newId);
            } else {
                // Update existing event
                if (eventService.updateEvent(event)) {
                    System.out.println("Updated event ID: " + event.getId());
                }
            }
            closeWindow();

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to save event: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", "Invalid input: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        if (nameField.getText().isBlank()) {
            errors.append("- Event name is required\n");
        }
        if (datePicker.getValue() == null) {
            errors.append("- Date is required\n");
        }
        if (!timeField.getText().matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            errors.append("- Time must be in HH:MM format\n");
        }
        if (!endTimeField.getText().matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            errors.append("- End time must be in HH:MM format\n");
        }
        if (!capacityField.getText().matches("\\d+")) {
            errors.append("- Capacity must be a number\n");
        }

        if (errors.length() > 0) {
            showAlert("Validation Error", errors.toString(), Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void closeWindow() {
        ((Stage) nameField.getScene().getWindow()).close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}