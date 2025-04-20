package controllers.workshop;

import controllers.BaseUserController;
import entities.Cours;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import services.CoursService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherWorkshopsFront {

    @FXML
    private FlowPane workshopsContainer;

    private final CoursService coursService = new CoursService();

    @FXML
    private void initialize() {
        loadWorkshops();
    }

    private void loadWorkshops() {
        try {
            List<Cours> list = coursService.displayList();
            workshopsContainer.getChildren().clear();

            for (Cours cours : list) {
                VBox card = new VBox(10);
                card.setPadding(new Insets(10));
                card.setStyle(
                        "-fx-background-color: white; " +
                                "-fx-background-radius: 8; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5,0,0,2);"
                );


                ImageView iv = cours.getImageView();
                iv.setFitWidth(280);
                iv.setPreserveRatio(true);


                Label title = new Label(cours.getTitreCours());
                title.setStyle(
                        "-fx-font-size: 18px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-text-fill: #014421;"
                );


                String desc = cours.getDescriptionCours();
                if (desc.length() > 100) desc = desc.substring(0, 100) + "...";
                Label description = new Label(desc);
                description.setWrapText(true);
                description.setStyle("-fx-font-size: 12px; -fx-text-fill: #444;");


                Button btnVoirPlus = new Button("Voir plus");
                btnVoirPlus.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                btnVoirPlus.setOnAction(ev -> {
                    // on passe direct au BaseUserController pour injecter le détail
                    BaseUserController.instance.showWorkshopDetails(cours);
                });


                card.getChildren().addAll(iv, title, description, btnVoirPlus);
                workshopsContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
