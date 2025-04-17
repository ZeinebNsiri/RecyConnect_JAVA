package controllers;

import entities.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

public class BaseUserController {

    public static BaseUserController instance; // Static access

    @FXML
    private BorderPane rootBorderPane;

    @FXML
    public void initialize() {
        instance = this; // Initialize static reference
    }

    @FXML
    public void showEventsView() {
        loadView("/EventViews/ListEventsFront.fxml");
    }

    public void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            rootBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void loadMyReservationsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReservationViews/ReservationsListFront.fxml"));
            Parent view = loader.load();
            rootBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showEventDetails(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventViews/EventDetails.fxml"));
            Parent view = loader.load();

            controllers.Events.EventDetailsController controller = loader.getController();
            controller.setEvent(event);

            rootBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    public void showArticleView() {
        System.out.println("✅ Nos produits clicked!");
        loadView("/ArticleViews/ListArticlesFront.fxml");
    }
}
