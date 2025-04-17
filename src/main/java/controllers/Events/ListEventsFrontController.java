package controllers.Events;

import controllers.BaseUserController;
import entities.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import services.EventService;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class ListEventsFrontController {

    @FXML
    private FlowPane eventFlowPane;

    private final EventService eventService = new EventService();

    @FXML
    public void initialize() {
        try {
            List<Event> events = eventService.displayList();
            for (Event event : events) {
                Node card = createEventCard(event);
                eventFlowPane.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Node createEventCard(Event event) {
        // Image loading
        Image image;
        try {
            image = new Image("file:uploads/" + event.getImage(), 200, 150, true, true);
        } catch (Exception e) {
            image = new Image("file:uploads/default.jpg", 200, 150, true, true);
        }

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(220);
        imageView.setFitHeight(140);
        imageView.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
        imageView.setPreserveRatio(true);

        // Titre
        Label name = new Label("🎫 " + event.getName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #014421;");

        // Infos
        Label location = new Label("📍 " + event.getLocation());
        Label date = new Label("📅 " + event.getDate().toString());
        Label time = new Label("⏰ " + event.getTime().toString());
        Label available = new Label("✔ " + event.getRemaining() + " places");
        available.setStyle("-fx-text-fill: #198754; -fx-font-weight: bold;");

        // Bouton Réserver
        Button reserveBtn = new Button("Réserver ma place");
        reserveBtn.setStyle("""
        -fx-background-color: #198754;
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-background-radius: 8;
        -fx-padding: 6 14;
        -fx-cursor: hand;
    """);

        reserveBtn.setOnAction(e -> {
            BaseUserController.instance.showEventDetails(event);
        });

        // VBox contenant tout
        VBox card = new VBox(10);
        card.setPrefWidth(240);
        card.setStyle("""
        -fx-padding: 15;
        -fx-background-color: white;
        -fx-border-color: #e0e0e0;
        -fx-border-radius: 12;
        -fx-background-radius: 12;
        -fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.08), 5, 0, 0, 3);
    """);

        card.getChildren().addAll(imageView, name, location, date, time, available, reserveBtn);
        return card;
    }

}
