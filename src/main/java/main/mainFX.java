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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseUser.fxml"));
        Parent root = loader.load(); // this creates a *new* layout instance

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("RecyConnect - Utilisateur");
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
