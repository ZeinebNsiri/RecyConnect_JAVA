package controllers.workshop;

import controllers.BaseUserController;
import entities.Cours;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.CoursService;

import java.sql.SQLException;
import java.util.List;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import services.ChatService;
import javafx.application.Platform;


public class AfficherWorkshopsFront {

    @FXML private FlowPane workshopsContainer;
    @FXML private HBox filterButtonsContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> videoComboBox;

    @FXML private VBox chatWindow;
    @FXML private Button chatToggleButton;
    @FXML private VBox chatMessagesContainer;
    @FXML private TextField chatInputField;
    @FXML private Button sendButton;
    @FXML private ScrollPane chatScrollPane;


    private final CoursService coursService = new CoursService();
    private List<Cours> allCourses;
    private Button selectedButton = null;
    private String selectedCategory = null;

    @FXML
    private void initialize() {
        try {
            allCourses = coursService.displayList();
            buildCategoryFilters();
            displayCourses(allCourses);

            videoComboBox.getItems().addAll("-- Tous --", "Avec vidéo", "Sans vidéo");
            videoComboBox.setValue("-- Tous --");


            searchField.textProperty().addListener((obs, oldText, newText) -> applyFilters());
            videoComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());


            chatToggleButton.setOnAction(event -> {
                chatWindow.setVisible(!chatWindow.isVisible());
            });

            sendButton.setOnAction(event -> {
                String userInput = chatInputField.getText().trim();
                if (!userInput.isEmpty()) {
                    addMessage(userInput, true); // true = user
                    chatInputField.clear();
                    new Thread(() -> {
                        String botReply = ChatService.sendMessage(userInput);
                        Platform.runLater(() -> addMessage(botReply, false));
                    }).start();
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void addMessage(String message, boolean isUser) {
        Label msgLabel = new Label(message);
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(500); // Largeur max pour le texte (ajuste si besoin)

        msgLabel.setStyle(
                isUser ?
                        "-fx-background-color: #2c7a4b; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 18 2 18 18;" :
                        "-fx-background-color: #e4e6eb; -fx-text-fill: black; -fx-padding: 10 15; -fx-background-radius: 2 18 18 18;"
        );

        HBox wrapper = new HBox(msgLabel);
        wrapper.setAlignment(isUser ? javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);
        wrapper.setPrefWidth(300); // Fixe la largeur de chaque ligne de message
        chatMessagesContainer.getChildren().add(wrapper);

        // Scroll vers le bas automatiquement
        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
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

    private Button createFilterButton(String label, String category) {
        Button btn = new Button(label);
        btn.setStyle(defaultStyle());

        btn.setOnMouseEntered(e -> {
            if (btn != selectedButton) btn.setStyle(hoverStyle());
        });
        btn.setOnMouseExited(e -> {
            if (btn != selectedButton) btn.setStyle(defaultStyle());
        });

        btn.setOnAction(e -> {
            if (selectedButton != null) selectedButton.setStyle(defaultStyle());
            btn.setStyle(selectedStyle());
            selectedButton = btn;
            selectedCategory = category;
            applyFilters();
        });

        return btn;
    }

    private String defaultStyle() {
        return "-fx-background-color: transparent;" +
                "-fx-text-fill: #2e7d32;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: normal;" +
                "-fx-padding: 5 10 8 10;" +
                "-fx-border-width: 0 0 2 0;" +
                "-fx-border-color: transparent;" +
                "-fx-cursor: hand;";
    }

    private String hoverStyle() {
        return "-fx-background-color: transparent;" +
                "-fx-text-fill: #2e7d32;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: normal;" +
                "-fx-padding: 5 10 8 10;" +
                "-fx-border-width: 0 0 2 0;" +
                "-fx-border-color: #d3eedd;" +
                "-fx-cursor: hand;";
    }

    private String selectedStyle() {
        return "-fx-background-color: transparent;" +
                "-fx-text-fill: #2e7d32;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 5 10 8 10;" +
                "-fx-border-width: 0 0 2 0;" +
                "-fx-border-color: #2e7d32;" +
                "-fx-cursor: hand;";
    }

    @FXML
    private void applyFilters() {
        String title = searchField.getText().trim().toLowerCase();
        String videoFilter = videoComboBox.getValue();

        List<Cours> filtered = allCourses.stream()
                .filter(c -> title.isEmpty() || c.getTitreCours().toLowerCase().contains(title))
                .filter(c -> {
                    if (videoFilter.equals("Avec vidéo"))
                        return c.getVideo() != null && !c.getVideo().isEmpty();
                    if (videoFilter.equals("Sans vidéo"))
                        return c.getVideo() == null || c.getVideo().isEmpty();
                    return true;
                })
                .filter(c -> selectedCategory == null || c.getCategorieCours().getNomCategorie().equals(selectedCategory))
                .toList();

        displayCourses(filtered);
    }

    private void displayCourses(List<Cours> list) {
        workshopsContainer.getChildren().clear();

        for (Cours cours : list) {
            VBox card = new VBox(10);
            card.setPadding(new Insets(10));
            card.setPrefWidth(250);
            card.setStyle("-fx-background-color: white; " +
                    "-fx-background-radius: 12; " +
                    "-fx-border-color: #e0e0e0; " +
                    "-fx-border-radius: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10,0,0,2);");

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


            int avgNote = coursService.getAverageRatingForCours(cours.getId());
            HBox stars = new HBox(5);
            for (int i = 1; i <= 5; i++) {
                Label star = new Label("★");
                star.setStyle(i <= avgNote
                        ? "-fx-text-fill: gold; -fx-font-size: 14px;"
                        : "-fx-text-fill: #ccc; -fx-font-size: 14px;");
                stars.getChildren().add(star);
            }

            Button btnVoirPlus = new Button("Voir plus");
            btnVoirPlus.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 12px;");
            btnVoirPlus.setOnAction(ev -> BaseUserController.instance.showWorkshopDetails(cours));

            card.getChildren().addAll(iv, category, title, description, stars, btnVoirPlus);
            workshopsContainer.getChildren().add(card);
        }
    }
}
