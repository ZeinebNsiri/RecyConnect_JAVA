package controllers;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Pane;

public class BaseUserController extends Application {

    @FXML
    private BorderPane rootBorderPane;

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/BaseUser.fxml"));
            Scene scene = new Scene(root);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mescommandes.fxml"));
            Parent mesCommandes = loader.load();
            rootBorderPane.setCenter(mesCommandes);


            // Créer un effet de blur sur un Pane ici
            Pane profileMenu = new Pane(); // Normalement tu récupères le vrai pane de ton FXML
            BoxBlur blur = new BoxBlur();
            blur.setWidth(10);
            blur.setHeight(10);
            blur.setIterations(3);

            profileMenu.setEffect(blur);

            primaryStage.setScene(scene);
            primaryStage.setTitle("Page Utilisateur");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur de chargement de BaseUser.fxml");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
