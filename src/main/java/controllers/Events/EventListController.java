package controllers.Events;

import entities.Event;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.time.LocalTime;
import java.util.List;

public class EventListController {

    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> nameColumn;
    @FXML private TableColumn<Event, String> locationColumn;
    @FXML private TableColumn<Event, String> dateColumn;
    @FXML private TableColumn<Event, Void> actionsColumn;
    @FXML private TableColumn<Event, Integer> idColumn;
    @FXML private TableColumn<Event, ImageView> imageColumn;
    @FXML private TableColumn<Event, String> timeColumn;

    private final EventService eventService = new EventService();
    private final ObservableList<Event> events = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

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

        addActionsToTable();
        loadEvents();
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
            eventTable.setItems(events);
        } catch (SQLException e) {
            showAlert("Erreur", "Chargement échoué: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddEvent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventViews/EventAdd.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un événement");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadEvents();
        } catch (IOException e) {
            showAlert("Erreur", "Chargement du formulaire échoué: " + e.getMessage());
        }
    }

    private void openEditForm(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventViews/EventEdit.fxml"));
            Parent root = loader.load();
            EventEditController controller = loader.getController();
            controller.setEvent(event);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier l'événement");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadEvents();
        } catch (IOException e) {
            showAlert("Erreur", "Chargement du formulaire échoué: " + e.getMessage());
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
}
