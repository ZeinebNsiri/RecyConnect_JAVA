package controllers.workshop;

import controllers.BaseUserController;
import entities.Cours;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.CoursService;

import java.sql.SQLException;
import java.util.List;

public class AfficherWorkshopsFront {

    @FXML private FlowPane workshopsContainer;
    @FXML private HBox filterButtonsContainer;

    private final CoursService coursService = new CoursService();
    private List<Cours> allCourses;

    @FXML
    private void initialize() {
        try {
            allCourses = coursService.displayList();
            buildCategoryFilters();
            displayCourses(allCourses);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void buildCategoryFilters() {
        filterButtonsContainer.getChildren().clear();

        Button btnAll = createFilterButton("Tous", null);
        filterButtonsContainer.getChildren().add(btnAll);

        allCourses.stream()
                .map(c -> c.getCategorieCours().getNomCategorie())
                .distinct()
                .forEach(cat -> {
                    Button b = createFilterButton(cat, cat);
                    filterButtonsContainer.getChildren().add(b);
                });
    }

    private Button selectedButton = null;

    private Button createFilterButton(String label, String category) {
        Button btn = new Button(label);

        // Default: flat text button
        btn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #2e7d32;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: normal;" +
                        "-fx-padding: 5 10 8 10;" +
                        "-fx-border-width: 0 0 2 0;" +
                        "-fx-border-color: transparent;" +
                        "-fx-cursor: hand;"
        );

        // Hover: underline
        btn.setOnMouseEntered(e -> {
            if (btn != selectedButton) {
                btn.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: #2e7d32;" +
                                "-fx-font-size: 13px;" +
                                "-fx-font-weight: normal;" +
                                "-fx-padding: 5 10 8 10;" +
                                "-fx-border-width: 0 0 2 0;" +
                                "-fx-border-color: #d3eedd;" +
                                "-fx-cursor: hand;"
                );
            }
        });

        // Unhover
        btn.setOnMouseExited(e -> {
            if (btn != selectedButton) {
                btn.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: #2e7d32;" +
                                "-fx-font-size: 13px;" +
                                "-fx-font-weight: normal;" +
                                "-fx-padding: 5 10 8 10;" +
                                "-fx-border-width: 0 0 2 0;" +
                                "-fx-border-color: transparent;" +
                                "-fx-cursor: hand;"
                );
            }
        });

        // On click: toggle filter + update styles
        btn.setOnAction(e -> {
            // Update style of previously selected button
            if (selectedButton != null) {
                selectedButton.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: #2e7d32;" +
                                "-fx-font-size: 13px;" +
                                "-fx-font-weight: normal;" +
                                "-fx-padding: 5 10 8 10;" +
                                "-fx-border-width: 0 0 2 0;" +
                                "-fx-border-color: transparent;" +
                                "-fx-cursor: hand;"
                );
            }

            // Set active style
            btn.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: #2e7d32;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 5 10 8 10;" +
                            "-fx-border-width: 0 0 2 0;" +
                            "-fx-border-color: #2e7d32;" +
                            "-fx-cursor: hand;"
            );

            selectedButton = btn;

            if (category == null) {
                displayCourses(allCourses);
            } else {
                List<Cours> filtered = allCourses.stream()
                        .filter(c -> c.getCategorieCours().getNomCategorie().equals(category))
                        .toList();
                displayCourses(filtered);
            }
        });

        return btn;
    }




    private void displayCourses(List<Cours> list) {
        workshopsContainer.getChildren().clear();

        for (Cours cours : list) {
            VBox card = new VBox(10);
            card.setPadding(new Insets(10));
            card.setPrefWidth(250);
            card.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-background-radius: 12; " +
                            "-fx-border-color: #e0e0e0; " +
                            "-fx-border-radius: 12; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10,0,0,2);"
            );

            ImageView iv = cours.getImageView();
            iv.setFitWidth(230);
            iv.setFitHeight(130);
            iv.setPreserveRatio(false);

            Label title = new Label(cours.getTitreCours());
            title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #014421;");

            Label category = new Label(cours.getCategorieCours().getNomCategorie());
            category.setStyle("-fx-background-color: #dff3e4; -fx-text-fill: #28a745; -fx-padding: 2 6; -fx-background-radius: 4;");

            String desc = cours.getDescriptionCours();
            if (desc.length() > 90) desc = desc.substring(0, 90) + "...";
            Label description = new Label(desc);
            description.setWrapText(true);
            description.setPrefHeight(60);
            description.setStyle("-fx-font-size: 12px; -fx-text-fill: #444;");

            Button btnVoirPlus = new Button("Voir plus");
            btnVoirPlus.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 12px;");
            btnVoirPlus.setOnAction(ev -> {
                BaseUserController.instance.showWorkshopDetails(cours);
            });

            card.getChildren().addAll(iv, category, title, description, btnVoirPlus);
            workshopsContainer.getChildren().add(card);
        }
    }
}
