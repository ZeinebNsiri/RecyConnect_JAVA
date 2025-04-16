package controllers.Reservations;

import entities.Reservation;
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
import services.ReservationService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

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

    private final ReservationService reservationService = new ReservationService();
    private final ObservableList<Reservation> reservations = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()).asObject());
        eventNameColumn.setCellValueFactory(cell -> new SimpleStringProperty("Event ID: " + cell.getValue().getEventId())); // peut être remplacé par lookup du nom de l'événement
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

    private void loadReservations() {
        try {
            List<Reservation> reservationList = reservationService.displayList();
            reservations.setAll(reservationList);
            reservationTable.setItems(reservations);
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
