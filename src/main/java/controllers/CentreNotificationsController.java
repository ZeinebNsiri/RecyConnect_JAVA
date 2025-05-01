package controllers;

import entities.Notification;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import services.NotificationService;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class CentreNotificationsController implements Initializable {

    @FXML
    private FlowPane notifContainer;

    private final NotificationService notificationService = new NotificationService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            List<Notification> notifs = notificationService.displayList();
            for (Notification notif : notifs) {
                VBox card = createNotifCard(notif);
                notifContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createNotifCard(Notification notif) {
        VBox card = new VBox();
        card.setSpacing(5);
        card.setPadding(new Insets(15));
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 1);");

        Label msg = new Label(notif.getMessage());
        msg.setWrapText(true);
        msg.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        msg.setMaxWidth(Double.MAX_VALUE);

        Label date = new Label(notif.getCreated_at().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        date.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        card.getChildren().addAll(msg, date);
        return card;
    }

}
