package controllers;

import entities.Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import services.EventService;
import java.sql.SQLException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.IOException;

public class EventController {
    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> nameColumn;
    @FXML private TableColumn<Event, String> locationColumn;
    @FXML private TableColumn<Event, String> dateColumn;
    @FXML private TableColumn<Event, Void> actionsColumn;

    private final EventService eventService = new EventService();
    private final ObservableList<Event> events = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup column bindings
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Add buttons to table
        addButtonsToTable();

        // Load data
        refreshEvents();
    }


    private void addButtonsToTable() {
        actionsColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Event, Void> call(final TableColumn<Event, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");

                    {
                        editBtn.setOnAction(event -> {
                            Event eventData = getTableView().getItems().get(getIndex());
                            handleEdit(eventData);
                        });

                        deleteBtn.setOnAction(event -> {
                            Event eventData = getTableView().getItems().get(getIndex());
                            handleDelete(eventData);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox buttons = new HBox(5, editBtn, deleteBtn);
                            setGraphic(buttons);
                        }
                    }
                };
            }
        });
    }

    @FXML
    private void handleAdd() {
        openEventForm(null);
    }

    private void handleEdit(Event event) {
        openEventForm(event);
    }

    private void handleDelete(Event event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Delete Event");
        alert.setContentText("Are you sure you want to delete " + event.getName() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (eventService.deleteEvent(event.getId())) {
                        refreshEvents();
                        showAlert("Success", "Event deleted successfully", Alert.AlertType.INFORMATION);
                    }
                } catch (SQLException e) {
                    showAlert("Error", "Failed to delete event: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void openEventForm(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventForm.fxml"));
            Parent root = loader.load();

            EventFormController controller = loader.getController();
            if (event != null) {
                controller.setEvent(event);
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(event == null ? "Add New Event" : "Edit Event");
            stage.showAndWait();

            refreshEvents();
        } catch (IOException e) {
            showAlert("Error", "Failed to load form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void refreshEvents() {
        try {
            events.setAll(eventService.getAllEvents());
            eventTable.setItems(events);
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load events: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}