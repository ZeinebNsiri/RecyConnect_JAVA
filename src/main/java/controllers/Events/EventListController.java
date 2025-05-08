package controllers.Events;

import controllers.BaseAdminController;
import entities.Event;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.EventService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Predicate;

public class EventListController {

    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> nameColumn;
    @FXML private TableColumn<Event, String> locationColumn;
    @FXML private TableColumn<Event, String> dateColumn;
    @FXML private TableColumn<Event, Void> actionsColumn;
    @FXML private TableColumn<Event, Integer> idColumn;
    @FXML private TableColumn<Event, ImageView> imageColumn;
    @FXML private TableColumn<Event, String> timeColumn;
    @FXML private TextField searchNameField;
    @FXML private TextField searchLocationField;
    @FXML private DatePicker searchDatePicker;
    @FXML private ComboBox<String> searchTypeCombo;

    private final EventService eventService = new EventService();
    private final ObservableList<Event> events = FXCollections.observableArrayList();
    private FilteredList<Event> filteredEvents;

    @FXML
    public void initialize() {
        eventTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // Set up table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Initialize FilteredList
        filteredEvents = new FilteredList<>(events, p -> true);

        // Set up search field listeners
        setupSearchListeners();

        // Image column configuration
        imageColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(ImageView item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Event event = getTableView().getItems().get(getIndex());
                    setGraphic(event.getImageView());
                }
            }
        });

        // Time column configuration
        timeColumn.setCellValueFactory(cellData -> {
            LocalTime time = cellData.getValue().getTime();
            return new SimpleStringProperty(time != null ? time.toString() : "");
        });

        timeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.substring(0, Math.min(item.length(), 5)));
            }
        });

        // Add action buttons to table
        addActionsToTable();

        // Load events and set filtered list to table
        loadEvents();
        eventTable.setItems(filteredEvents);
    }

    private void setupSearchListeners() {
        // Add listeners to search fields for real-time filtering
        searchNameField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter());
        searchLocationField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter());
        searchDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter());
        searchTypeCombo.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter());
    }

    private void updateFilter() {
        filteredEvents.setPredicate(createFilterPredicate());
    }

    private Predicate<Event> createFilterPredicate() {
        return event -> {
            String nameFilter = searchNameField.getText().toLowerCase().trim();
            String locationFilter = searchLocationField.getText().toLowerCase().trim();
            LocalDate dateFilter = searchDatePicker.getValue();
            String typeFilter = searchTypeCombo.getValue();

            // Check if name matches filter
            boolean matchName = nameFilter.isEmpty() ||
                    event.getName().toLowerCase().contains(nameFilter);

            // Check if location matches filter
            boolean matchLocation = locationFilter.isEmpty() ||
                    event.getLocation().toLowerCase().contains(locationFilter);

            // Check if date matches filter
            boolean matchDate = dateFilter == null ||
                    event.getDate().equals(dateFilter);

            // Check if type matches filter (assuming online events contain "en ligne" in location)
            boolean matchType = typeFilter == null || typeFilter.isEmpty() ||
                    (typeFilter.equals("en ligne") && event.getLocation().toLowerCase().contains("en ligne")) ||
                    (typeFilter.equals("sur site") && !event.getLocation().toLowerCase().contains("en ligne"));

            // Return true only if all conditions match
            return matchName && matchLocation && matchDate && matchType;
        };
    }

    private void addActionsToTable() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox box = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

                editBtn.setOnAction(event -> {
                    Event e = getTableView().getItems().get(getIndex());
                    openEditForm(e);
                });

                deleteBtn.setOnAction(event -> {
                    Event e = getTableView().getItems().get(getIndex());
                    openDeleteConfirmationDialog(e);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadEvents() {
        try {
            List<Event> eventList = eventService.displayList();
            events.setAll(eventList);
            updateFilter(); // Apply current filters to the new data
        } catch (SQLException e) {
            showAlert("Erreur", "Chargement échoué: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddEvent() {
        Stage stage = (Stage) eventTable.getScene().getWindow();
        boolean wasMaximized = stage.isMaximized();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseAdmin.fxml"));
            Parent root = loader.load();
            BaseAdminController controller = loader.getController();

            controller.showAddEventView(); // you’ll add this method

            stage.setScene(new Scene(root));
            stage.setMaximized(wasMaximized);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la vue d'ajout.");
        }
    }

    private void openEditForm(Event event) {
        Stage stage = (Stage) eventTable.getScene().getWindow();
        boolean wasMaximized = stage.isMaximized();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseAdmin.fxml"));
            Parent root = loader.load();

            BaseAdminController controller = loader.getController();
            controller.showEditEventView(event);  // Call method to inject event into edit view

            stage.setScene(new Scene(root));
            stage.setMaximized(wasMaximized);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la vue de modification.");
        }
    }


    private void openDeleteConfirmationDialog(Event event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer l'événement suivant ?");
        confirm.setContentText("Nom: " + event.getName() + "\nLieu: " + event.getLocation() + "\nDate: " + event.getDate());

        ButtonType confirmBtn = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(confirmBtn, cancelBtn);

        confirm.showAndWait().ifPresent(type -> {
            if (type == confirmBtn) {
                try {
                    eventService.delete(event);
                    loadEvents();
                    showAlert("Succès", "Événement supprimé avec succès.");
                } catch (SQLException e) {
                    showAlert("Erreur", "Échec de la suppression : " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleSearch() {
        // This method is kept for backward compatibility with the button
        // But filtering is now handled automatically by the listeners
        updateFilter();
    }
}