package main;

import entities.Event;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class mainFX extends Application {
    public static Event currentViewedEvent;

    // --- HostServices pour ouvrir des liens web dans l'application ---
    private static HostServices hostServices;

    // --- Choix manuel du rôle à démarrage ---
    // Change cette variable ici pour choisir la vue à ouvrir :
    private static final String ROLE = "ADMIN"; // mettre "ADMIN" ou "USER"

    @Override
    public void start(Stage stage) throws Exception {
        // Initialiser HostServices
        hostServices = getHostServices();

        // Définir le chemin du fichier FXML selon le rôle
        String fxmlFile;
        if ("ADMIN".equalsIgnoreCase(ROLE)) {
            fxmlFile = "/BaseAdmin.fxml";
        } else {
            fxmlFile = "/BaseUser.fxml";
        }

        // Charger le fichier FXML correspondant
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        // Créer la scène
        Scene scene = new Scene(root);
        stage.setScene(scene);

        // Définir le titre de la fenêtre selon le rôle
        if ("ADMIN".equalsIgnoreCase(ROLE)) {
            stage.setTitle("RecyConnect - Interface Admin");
        } else {
            stage.setTitle("RecyConnect - Interface Utilisateur");
        }

        // Afficher la fenêtre
        stage.show();
    }

    // --- Permet aux autres classes d'accéder à HostServices ---
    public static HostServices getHostServicesInstance() {
        return hostServices;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
