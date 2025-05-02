package controllers;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Pane;
import org.controlsfx.control.Notifications;
import javafx.geometry.Pos;
import javafx.util.Duration;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;



public class BaseUserController extends Application {

    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private StackPane mainContent;

    @FXML
    private Label notificationIcon;


    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseUser.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur lié
            BaseUserController controller = loader.getController();

            // Charger dynamiquement le contenu central
            FXMLLoader subLoader = new FXMLLoader(getClass().getResource("/mescommandes.fxml"));
            Parent mesCommandes = subLoader.load();

            // Maintenant on peut accéder au rootBorderPane via le contrôleur
            controller.rootBorderPane.setCenter(mesCommandes);

            // Exemple de blur (si tu as un vrai pane à blur)
            // controller.profileMenu.setEffect(new BoxBlur(10, 10, 3));

            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Page Utilisateur");
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur de chargement de BaseUser.fxml");
        }
    }
    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            Notifications.create()
                    .title("Bienvenue !")
                    .text("Bonjour Mohamed Aziz, bon retour sur RecyConnect.")
                    .position(Pos.TOP_RIGHT)
                    .hideAfter(Duration.seconds(5))
                    .showInformation();
        });
        animateNotificationIcon();
    }


    private void setContent(Node content) {
        Platform.runLater(() -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), mainContent);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                mainContent.getChildren().setAll(content);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), mainContent);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
        });
    }
    private void animateNotificationIcon() {
        ScaleTransition st = new ScaleTransition(Duration.seconds(1), notificationIcon);
        st.setFromX(1);
        st.setFromY(1);
        st.setToX(1.2);
        st.setToY(1.2);
        st.setCycleCount(ScaleTransition.INDEFINITE);
        st.setAutoReverse(true);
        st.play();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
