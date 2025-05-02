package controllers.Events;

import controllers.BaseUserController;
import entities.Event;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.EventService;

import java.io.IOException;
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
    private ObservableList<Event> allEvents;
    private ObservableList<Event> filteredEvents;

    @FXML
    public void initialize() {
        // Initialize type combo box
        typeComboBox.getItems().addAll("Tous les types", "en ligne", "sur site");
        typeComboBox.setValue("Tous les types");

        try {
            // Load events and convert to observable list
            List<Event> events = eventService.displayList();
            allEvents = FXCollections.observableArrayList(events);
            filteredEvents = FXCollections.observableArrayList(allEvents);

            // Setup listeners for filtered events to update UI
            filteredEvents.addListener((ListChangeListener<Event>) change -> {
                displayEvents(filteredEvents);
            });

            // Add change listeners to all search fields
            setupDynamicSearchListeners();

            // Initial display
            displayEvents(filteredEvents);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error loading events", e.getMessage());
        }
    }

    private void setupDynamicSearchListeners() {
        // Add listeners to all search controls
        searchNameField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        searchLocationField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        searchDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        typeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void applyFilters() {
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

        // Update filtered list
        filteredEvents.clear();
        filteredEvents.addAll(filtered);
    }

    private void displayEvents(List<Event> events) {
        eventFlowPane.getChildren().clear();
        if (events.isEmpty()) {
            Label noResultsLabel = new Label("Aucun événement ne correspond à votre recherche");
            noResultsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d; -fx-padding: 20;");
            eventFlowPane.getChildren().add(noResultsLabel);
        } else {
            for (Event event : events) {
                eventFlowPane.getChildren().add(createEventCard(event));
            }
        }
    }

    @FXML
    private void handleSearch() {
        // This method is kept for backward compatibility with the button
        applyFilters();
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

    @FXML
    private void handleShowRecommendations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventViews/RecommendationView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) eventFlowPane.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load recommendations view: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}