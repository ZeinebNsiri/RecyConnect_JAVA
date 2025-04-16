package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class BaseUserController {

    @FXML
    private BorderPane rootBorderPane;

    @FXML
    public void initialize() {
        loadView("/formAjoutArticle.fxml"); // Charge dynamiquement le body
    }

    @FXML
    public void showArticleView() {
        loadView("/listeArticlesUser.fxml");
    }

    public void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            rootBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}