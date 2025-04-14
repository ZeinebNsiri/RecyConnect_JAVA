package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import services.EventService;
import utils.MyDataBase;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Test database connection
        try {
            System.out.println("Testing database connection...");
            EventService service = new EventService();
            System.out.println("✅ Found " + service.getAllEvents().size() + " events");
        } catch (Exception e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            throw e;
        }

        // Load main interface
        Parent root = FXMLLoader.load(getClass().getResource("/EventViews/EventList.fxml"));
        primaryStage.setTitle("Event Manager");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }
}