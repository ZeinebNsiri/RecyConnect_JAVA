package controllers.Events;

import entities.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.EventService;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.io.IOException;

public class EventFormController {

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
    @FXML private Label descriptionErrorLabel;
    @FXML private Label imageLabel;
    @FXML private Label nameErrorLabel;
    @FXML private Label locationErrorLabel;
    @FXML private Label dateErrorLabel;
    @FXML private Label timeErrorLabel;
    @FXML private Label endTimeErrorLabel;
    @FXML private Label capacityErrorLabel;
    @FXML private Label remainingErrorLabel;
    @FXML private Label imageErrorLabel;

    private File selectedImageFile;
    private final EventService eventService = new EventService();
    private Event event;

    public void setEvent(Event event) {
        this.event = event;

        if (event != null) {
            nameField.setText(event.getName());
            locationField.setText(event.getLocation());
            datePicker.setValue(event.getDate());
            timeField.setText(event.getTime().toString());
            endTimeField.setText(event.getEndTime().toString());
            descriptionField.setText(event.getDescription());
            capacityField.setText(String.valueOf(event.getCapacity()));
            remainingField.setText(String.valueOf(event.getRemaining()));
            linkField.setText(event.getMeetingLink());
            coordinatesField.setText(event.getCoordinates());
            imageLabel.setText(event.getImage() != null ? event.getImage() : "Aucune image choisie");
        }
    }

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
        boolean valid = true;

        // Reset all error labels
        nameErrorLabel.setVisible(false);
        locationErrorLabel.setVisible(false);
        dateErrorLabel.setVisible(false);
        timeErrorLabel.setVisible(false);
        endTimeErrorLabel.setVisible(false);
        capacityErrorLabel.setVisible(false);
        remainingErrorLabel.setVisible(false);
        imageErrorLabel.setVisible(false);
        descriptionErrorLabel.setVisible(false);


        // === VALIDATION ===

        if (nameField.getText().isEmpty()) {
            nameErrorLabel.setText("Le nom de l'événement est requis.");
            nameErrorLabel.setVisible(true);
            valid = false;
        } else if (nameField.getText().length() > 255) {
            nameErrorLabel.setText("Le nom ne peut pas dépasser 255 caractères.");
            nameErrorLabel.setVisible(true);
            valid = false;
        }

        if (locationField.getText().isEmpty()) {
            locationErrorLabel.setText("Le lieu est requis.");
            locationErrorLabel.setVisible(true);
            valid = false;
        }

        if (datePicker.getValue() == null) {
            dateErrorLabel.setText("La date de l'événement est requise.");
            dateErrorLabel.setVisible(true);
            valid = false;
        } else if (datePicker.getValue().isBefore(LocalDate.now())) {
            dateErrorLabel.setText("La date doit être aujourd’hui ou future.");
            dateErrorLabel.setVisible(true);
            valid = false;
        }

        if (timeField.getText().isEmpty() || !timeField.getText().matches("\\d{2}:\\d{2}")) {
            timeErrorLabel.setText("Heure invalide (format hh:mm).");
            timeErrorLabel.setVisible(true);
            valid = false;
        }

        if (endTimeField.getText().isEmpty() || !endTimeField.getText().matches("\\d{2}:\\d{2}")) {
            endTimeErrorLabel.setText("Heure de fin invalide (format hh:mm).");
            endTimeErrorLabel.setVisible(true);
            valid = false;
        }
        if (descriptionField.getText().isEmpty()) {
            descriptionErrorLabel.setText("La description est requise.");
            descriptionErrorLabel.setVisible(true);
            valid = false;
        }

        if (capacityField.getText().isEmpty() || !capacityField.getText().matches("\\d+") || Integer.parseInt(capacityField.getText()) <= 0) {
            capacityErrorLabel.setText("Capacité invalide.");
            capacityErrorLabel.setVisible(true);
            valid = false;
        }

        if (remainingField.getText().isEmpty() || !remainingField.getText().matches("\\d+")) {
            remainingErrorLabel.setText("Valeur invalide pour places restantes.");
            remainingErrorLabel.setVisible(true);
            valid = false;
        }

        // Check for image in create mode
        if (event == null && selectedImageFile == null) {
            imageErrorLabel.setText("Veuillez choisir une image.");
            imageErrorLabel.setVisible(true);
            valid = false;
        }

        if (!valid) return;

        // === SAVE LOGIC ===
        try {
            if (event == null) event = new Event();

            event.setName(nameField.getText());
            event.setLocation(locationField.getText());
            event.setDate(datePicker.getValue());
            event.setTime(LocalTime.parse(timeField.getText()));
            event.setEndTime(LocalTime.parse(endTimeField.getText()));
            event.setDescription(descriptionField.getText());

            if (selectedImageFile != null) {
                File uploadDir = new File("uploads");
                if (!uploadDir.exists()) uploadDir.mkdirs();

                Path dest = Paths.get("uploads", selectedImageFile.getName());
                Files.copy(selectedImageFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                event.setImage(selectedImageFile.getName());
            } else if (event.getImage() != null) {
                event.setImage(event.getImage());
            } else {
                event.setImage("default.png");
            }

            event.setCapacity(Integer.parseInt(capacityField.getText()));
            event.setRemaining(Integer.parseInt(remainingField.getText()));
            event.setMeetingLink(linkField.getText());
            event.setCoordinates(coordinatesField.getText());

            if (event.getId() == 0) {
                eventService.addEvent(event);
            } else {
                eventService.updateEvent(event);
            }

            closeForm();

        } catch (SQLException | IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).show();
        }
    }

    @FXML
    private void cancelForm() {
        closeForm();
    }

    private void closeForm() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
