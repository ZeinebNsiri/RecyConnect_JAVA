package controllers.Events;

import entities.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import services.EventService;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.io.IOException;
import java.net.URL;
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

        // Charger image depuis dossier upload (chemin relatif local)
        Image image;
        try {
            image = new Image("file:uploads/" + event.getImage(), 200, 150, true, true);
        } catch (Exception e) {
            image = new Image("file:uploads/default.jpg", 200, 150, true, true); // fallback si image manquante
        }

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(200);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        VBox card = new VBox(10);
        card.setStyle("-fx-padding: 15; -fx-border-color: #ccc; -fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8;");
        card.setPrefWidth(220);

        Label name = new Label(event.getName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label location = new Label("📍 " + event.getLocation());
        Label date = new Label("📅 " + event.getDate().toString());
        Label time = new Label("⏰ " + event.getTime().toString());

        Button reserveBtn = new Button("Réserver");

        reserveBtn.setOnAction(e -> {
            try {
                URL fxmlLocation = getClass().getResource("/EventViews/EventDetails.fxml");
                System.out.println("✅ FXML path: " + fxmlLocation);

                if (fxmlLocation == null) {
                    throw new RuntimeException("❌ Fichier EventDetails.fxml introuvable !");
                }

                FXMLLoader loader = new FXMLLoader(fxmlLocation);
                Parent root = loader.load();

                EventDetailsController controller = loader.getController();
                controller.setEvent(event);

                Stage stage = (Stage) reserveBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        reserveBtn.setStyle("-fx-background-color: #198754; -fx-text-fill: white;");

        card.getChildren().addAll(imageView, name, location, date, time, reserveBtn);
        return card;
    }
}
