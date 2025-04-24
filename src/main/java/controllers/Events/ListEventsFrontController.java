package controllers.Events;

import controllers.BaseUserController;
import entities.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import services.EventService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ListEventsFrontController {

    @FXML private FlowPane eventFlowPane;
    @FXML private TextField searchNameField;
    @FXML private TextField searchLocationField;
    @FXML private DatePicker searchDatePicker;
    @FXML private ComboBox<String> typeComboBox;

    private final EventService eventService = new EventService();
    private List<Event> allEvents;

    @FXML
    public void initialize() {
        typeComboBox.getItems().addAll("Tous les types", "en ligne", "sur site");
        typeComboBox.setValue("Tous les types");

        try {
            allEvents = eventService.displayList();
            displayEvents(allEvents);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayEvents(List<Event> events) {
        eventFlowPane.getChildren().clear();
        for (Event event : events) {
            eventFlowPane.getChildren().add(createEventCard(event));
        }
    }

    @FXML
    private void handleSearch() {
        String nameKeyword = searchNameField.getText().toLowerCase(Locale.ROOT).trim();
        String locationKeyword = searchLocationField.getText().toLowerCase(Locale.ROOT).trim();
        LocalDate selectedDate = searchDatePicker.getValue();
        String selectedType = typeComboBox.getValue();

        List<Event> filtered = allEvents.stream()
                .filter(e -> e.getName().toLowerCase(Locale.ROOT).contains(nameKeyword))
                .filter(e -> e.getLocation().toLowerCase(Locale.ROOT).contains(locationKeyword))
                .filter(e -> selectedDate == null || e.getDate().isEqual(selectedDate))
                .filter(e -> {
                    if ("Tous les types".equals(selectedType)) return true;
                    if ("en ligne".equals(selectedType)) return e.getLocation().toLowerCase().contains("en ligne");
                    if ("sur site".equals(selectedType)) return !e.getLocation().toLowerCase().contains("en ligne");
                    return true;
                })
                .collect(Collectors.toList());

        displayEvents(filtered);
    }

    private Node createEventCard(Event event) {
        Image image;
        try {
            image = new Image("file:uploads/" + event.getImage(), 200, 150, true, true);
        } catch (Exception e) {
            image = new Image("file:uploads/default.jpg", 200, 150, true, true);
        }

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(220);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);

        Label name = new Label("🎫 " + event.getName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #014421;");

        Label location = new Label("📍 " + event.getLocation());
        Label date = new Label("📅 " + event.getDate().toString());
        Label time = new Label("⏰ " + event.getTime().toString());
        Label available = new Label("✔ " + event.getRemaining() + " places");
        available.setStyle("-fx-text-fill: #198754; -fx-font-weight: bold;");

        Button reserveBtn = new Button("Réserver ma place");
        reserveBtn.setStyle("-fx-background-color: #198754; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 6 14;");
        reserveBtn.setOnAction(e -> BaseUserController.instance.showEventDetails(event));

        VBox card = new VBox(10);
        card.setPrefWidth(240);
        card.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.08), 5, 0, 0, 3);");

        card.getChildren().addAll(imageView, name, location, date, time, available, reserveBtn);
        return card;
    }

    @FXML
    private void handleShowMyReservations() {
        BaseUserController.instance.loadMyReservationsView();
    }
}
