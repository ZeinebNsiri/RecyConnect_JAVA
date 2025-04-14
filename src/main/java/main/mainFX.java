package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class mainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/EventViews/EventList.fxml")); // 👈 Your FXML
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 1000, 800);
        stage.setScene(scene);
        stage.setTitle("Gestion des Événements");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
