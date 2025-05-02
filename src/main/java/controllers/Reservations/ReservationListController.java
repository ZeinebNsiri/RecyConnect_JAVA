package controllers.Reservations;

import entities.Reservation;
import entities.Event;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import services.EventService;
import services.ReservationService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class ReservationListController {

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, Integer> idColumn;
    @FXML private TableColumn<Reservation, String> eventNameColumn;
    @FXML private TableColumn<Reservation, String> userColumn;
    @FXML private TableColumn<Reservation, String> emailColumn;
    @FXML private TableColumn<Reservation, String> phoneColumn;
    @FXML private TableColumn<Reservation, String> specialRequestsColumn;
    @FXML private TableColumn<Reservation, String> statusColumn;
    @FXML private TableColumn<Reservation, Integer> placesColumn;
    @FXML private TableColumn<Reservation, Void> actionsColumn;

    @FXML private TextField eventSearchField;
    @FXML private TextField userSearchField;
    @FXML private ComboBox<String> statusFilterCombo;

    private final ReservationService reservationService = new ReservationService();
    private final EventService eventService = new EventService();
    private final ObservableList<Reservation> reservations = FXCollections.observableArrayList();
    private FilteredList<Reservation> filteredReservations;

    // Define status constants to ensure consistency
    private static final String STATUS_ALL = "Tous les statuts";
    private static final String STATUS_ACTIVE = "Active";
    private static final String STATUS_CANCELLED = "Cancelled";

    @FXML
    public void initialize() {
        // Configure table columns
        setupTableColumns();

        // Initialize filtered list
        filteredReservations = new FilteredList<>(reservations, p -> true);
        reservationTable.setItems(filteredReservations);

        // Add actions to the table
        addActionsToTable();

        // Load data
        loadReservations();

        // Setup filter UI and listeners
        setupFilterUI();
        setupSearchListeners();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()).asObject());

        eventNameColumn.setCellValueFactory(cell -> {
            try {
                String name = eventService.getEventById(cell.getValue().getEventId()).getName();
                return new SimpleStringProperty(name);
            } catch (Exception e) {
                return new SimpleStringProperty("Événement introuvable");
            }
        });

        userColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNom()));
        emailColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEmail()));
        phoneColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNumTel()));
        specialRequestsColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getdemandes_speciales()));

        // Standardize status display to have consistent capitalization
        statusColumn.setCellValueFactory(cell -> {
            String rawStatus = cell.getValue().getStatus();
            String normalizedStatus = normalizeStatusValue(rawStatus);
            return new SimpleStringProperty(normalizedStatus);
        });

        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    setStyle("-fx-background-color: " +
                            (status.equalsIgnoreCase("active") ? "#198754" : "#dc3545") +
                            "; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: CENTER;");
                }
            }
        });

        placesColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getNbPlaces()).asObject());
    }

    private void setupSearchListeners() {
        // Add listeners to search fields for real-time filtering
        eventSearchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter());
        userSearchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter());
        statusFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter());
    }

    private void updateFilter() {
        filteredReservations.setPredicate(createFilterPredicate());
    }

    private Predicate<Reservation> createFilterPredicate() {
        return reservation -> {
            String eventFilter = eventSearchField.getText().toLowerCase().trim();
            String userFilter = userSearchField.getText().toLowerCase().trim();
            String statusFilter = statusFilterCombo.getValue();

            // Since Reservation only has eventId, not eventName, we need to get the event name
            String eventName = getEventNameById(reservation.getEventId());

            // Check if event name matches filter
            boolean matchEvent = eventFilter.isEmpty() ||
                    (eventName != null && eventName.toLowerCase().contains(eventFilter));

            // Check if user name matches filter
            boolean matchUser = userFilter.isEmpty() ||
                    reservation.getNom().toLowerCase().contains(userFilter);

            // Check if status matches filter (handle "Tous les statuts" as "show all")
            boolean matchStatus = statusFilter == null ||
                    statusFilter.equals(STATUS_ALL) ||
                    statusFilter.equalsIgnoreCase(normalizeStatusValue(reservation.getStatus()));

            // Return true only if all conditions match
            return matchEvent && matchUser && matchStatus;
        };
    }

    // Helper method to get event name by ID
    private String getEventNameById(int eventId) {
        try {
            Event event = eventService.getEventById(eventId);
            return event != null ? event.getName() : "";
        } catch (Exception e) {
            System.err.println("Error getting event name: " + e.getMessage());
            return "";
        }
    }

    private void addActionsToTable() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox box = new HBox(deleteBtn);

            {
                deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold;");
                deleteBtn.setOnAction(e -> openDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void setupFilterUI() {
        // Clear existing items
        statusFilterCombo.getItems().clear();

        // Add standard items first
        statusFilterCombo.getItems().add(STATUS_ALL);

        // Collect unique status values from reservations with normalized capitalization
        Set<String> uniqueStatuses = new HashSet<>();
        for (Reservation r : reservations) {
            uniqueStatuses.add(normalizeStatusValue(r.getStatus()));
        }

        // Add normalized status values to combo box
        List<String> sortedStatuses = new ArrayList<>(uniqueStatuses);
        sortedStatuses.sort(String::compareTo); // Sort alphabetically
        statusFilterCombo.getItems().addAll(sortedStatuses);

        // Set default selection
        statusFilterCombo.setValue(STATUS_ALL);
    }

    /**
     * Normalizes status values to have consistent capitalization
     * @param status The raw status value from the database
     * @return Normalized status value with proper capitalization
     */
    private String normalizeStatusValue(String status) {
        if (status == null) return "";

        // Convert to lowercase for case-insensitive comparison
        String lowerStatus = status.toLowerCase();

        // Return the standardized version based on the lowercase value
        if (lowerStatus.equals("active")) {
            return STATUS_ACTIVE;
        } else if (lowerStatus.equals("cancelled")) {
            return STATUS_CANCELLED;
        } else {
            // For any other status, capitalize first letter
            return status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
        }
    }

    @FXML
    private void handleSearch() {
        // This method is kept for backward compatibility with the button
        // But filtering is now handled automatically by the listeners
        updateFilter();
    }

    private void loadReservations() {
        try {
            List<Reservation> reservationList = reservationService.displayList();
            reservations.setAll(reservationList);
            updateFilter(); // Apply current filters to the new data
            setupFilterUI(); // Reload filter options once data is loaded
        } catch (SQLException e) {
            showAlert("Erreur", "Chargement échoué : " + e.getMessage());
        }
    }

    private void openDelete(Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReservationViews/ReservationDelete.fxml"));
            Parent root = loader.load();
            ReservationDeleteController controller = loader.getController();
            controller.setReservation(reservation);

            Stage stage = new Stage();
            stage.setTitle("Supprimer la réservation");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadReservations();
        } catch (IOException e) {
            showAlert("Erreur", "Chargement du formulaire échoué : " + e.getMessage());
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.show();
    }
}