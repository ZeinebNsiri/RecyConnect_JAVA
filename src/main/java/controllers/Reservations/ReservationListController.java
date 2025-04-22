package controllers.Reservations;

import entities.Reservation;
import entities.Event;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final ObservableList<Reservation> filteredReservations = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
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

        statusColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));
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

        addActionsToTable();
        loadReservations();
        setupFilterUI();
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
        Set<String> statuses = new HashSet<>();
        reservations.forEach(r -> statuses.add(r.getStatus()));
        statusFilterCombo.getItems().clear();
        statusFilterCombo.getItems().add("Tous les statuts");
        statusFilterCombo.getItems().addAll(statuses);
        statusFilterCombo.setValue("Tous les statuts");
    }

    @FXML
    private void handleSearch() {
        String eventKeyword = eventSearchField.getText().toLowerCase(Locale.ROOT).trim();
        String userKeyword = userSearchField.getText().toLowerCase(Locale.ROOT).trim();
        String statusSelected = statusFilterCombo.getValue();

        List<Reservation> result = reservations.stream()
                .filter(res -> res.getNom().toLowerCase(Locale.ROOT).contains(userKeyword))
                .filter(res -> {
                    try {
                        String eventName = eventService.getEventById(res.getEventId()).getName().toLowerCase(Locale.ROOT);
                        return eventName.contains(eventKeyword);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .filter(res -> {
                    if (statusSelected.equals("Tous les statuts")) return true;
                    return res.getStatus().equalsIgnoreCase(statusSelected);
                })
                .collect(Collectors.toList());

        filteredReservations.setAll(result);
        reservationTable.setItems(filteredReservations);
    }

    private void loadReservations() {
        try {
            List<Reservation> reservationList = reservationService.displayList();
            reservations.setAll(reservationList);
            filteredReservations.setAll(reservationList);
            reservationTable.setItems(filteredReservations);
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
