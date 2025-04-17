// BaseAdminController.java
package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class BaseAdminController {

    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private StackPane contentPane;

    @FXML
    public void initialize() {
        System.out.println("✅ BaseAdminController initialized");
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showEvenementView() {
        loadView("/EventViews/EventList.fxml");
    }

    @FXML
    public void showReservationsView() {
        loadView("/ReservationViews/ReservationList.fxml");
    }

    @FXML
    public void showDashboardView() {
        loadView("/AdminViews/Dashboard.fxml"); // You can create this later
    }

    @FXML
    public void showUsersView() {
        loadView("/AdminViews/UserList.fxml"); // Placeholder for user list
    }

    @FXML
    public void showCategorieView() {
        loadView("/AdminViews/CategorieArticle.fxml");
    }

    @FXML
    public void showArticlesView() {
        loadView("/AdminViews/ArticleList.fxml");
    }

    @FXML
    public void showCommandesView() {
        loadView("/AdminViews/CommandeList.fxml");
    }

    @FXML
    public void showCategorieWorkshopView() {
        loadView("/AdminViews/CategorieWorkshop.fxml");
    }

    @FXML
    public void showWorkshopsView() {
        loadView("/AdminViews/WorkshopList.fxml");
    }

    @FXML
    public void showPostsView() {
        loadView("/AdminViews/PostsList.fxml");
    }

}
