package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class mainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        /*
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/BaseAdmin.fxml"));
        Parent root = fxmlLoader.load();
        stage.setTitle("RecyConnect Admin Dashboard");
*/
        // === Interface Front Utilisateur (événements) ===
         FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/EventViews/ListEventsFront.fxml"));
         Parent root = fxmlLoader.load();
         stage.setTitle("RecyConnect - Événements");

        Scene scene = new Scene(root, 1000, 800);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
