package main;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class mainFX extends Application {
    // --- HostServices pour ouvrir des liens web dans l'application ---
    private static HostServices hostServices;
    @Override
    public void start(Stage stage) throws Exception {
        // Initialiser HostServices
        hostServices = getHostServices();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Login.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Recyconnect");
        stage.show();

    }
    // --- Permet aux autres classes d'accéder à HostServices ---
    public static HostServices getHostServicesInstance() {
        return hostServices;}
}
