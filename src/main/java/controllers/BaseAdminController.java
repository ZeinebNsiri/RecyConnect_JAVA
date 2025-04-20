// BaseAdminController.java
package controllers;

import controllers.workshop.ModifierCategorieCours;
import controllers.workshop.ModifierCours;
import entities.CategorieCours;
import entities.Cours;
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
        loadView("/workshop/AfficherCategorieCours.fxml");
    }

    @FXML
    public void showWorkshopsView() {
        loadView("/workshop/AfficherCours.fxml");
    }

    @FXML
    public void showPostsView() {
        loadView("/AdminViews/PostsList.fxml");
    }

    public void showModifierCategorieViewWithData(CategorieCours catcours) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/workshop/ModifierCategorieCours.fxml")
            );
            Parent view = loader.load();

            // pré‑remplir le formulaire
            ModifierCategorieCours ctrl = loader.getController();
            ctrl.setCategorieCours(catcours);

            // injecter dans le shell
            rootBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showAjoutCategorieView() {
        loadView("/workshop/AjoutCategorieCours.fxml");
    }

    @FXML
    public void showAjouterCoursView() {
        loadView("/workshop/AjouterCours.fxml");
    }

    public void showModifierCoursViewWithData(Cours c) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/workshop/ModifierCours.fxml")
            );
            Parent content = loader.load();
            ModifierCours ctrl = loader.getController();
            ctrl.setCours(c);
            contentPane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





}