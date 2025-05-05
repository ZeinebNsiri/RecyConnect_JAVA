package controllers;

import entities.utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import utils.Session;

import java.io.IOException;

public class BaseUserController {

    @FXML
    private BorderPane rootBorderPane;
    @FXML
    private MenuButton userMenuButton;
    @FXML
    private MenuItem profilMenuItem;
    @FXML
    private MenuItem logoutItem;
    @FXML
    private Label sceneReference;

    @FXML
    public void initialize() {
        utilisateur user = Session.getInstance().getCurrentUser();
        if (user != null) {
            String fullName = user.getPrenom() + " " + user.getNom_user();
            userMenuButton.setText(fullName);
        }
        profilMenuItem.setOnAction(e -> showProfileView());
        logoutItem.setOnAction(e -> {
            try {
                logout();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

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
    private void showProfileView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile.fxml"));
            Parent profileRoot = loader.load();
            rootBorderPane.setCenter(profileRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void logout() throws IOException {

        Session.getInstance().logout();


        Parent loginRoot = FXMLLoader.load(getClass().getResource("/Login.fxml"));
        Scene loginScene = new Scene(loginRoot);


        Stage currentStage = (Stage) sceneReference.getScene().getWindow();
        currentStage.setScene(loginScene);
        currentStage.setTitle("Connexion");
        currentStage.show();
    }
}