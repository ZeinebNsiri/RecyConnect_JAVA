
package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class BaseAdminController {

    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private void showCategorieView() {
        loadView("/categorie.fxml");
    }

    @FXML
    private void showDashboardView() {
        Label dashboardLabel = new Label("📊 Tableau de bord");
        dashboardLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        rootBorderPane.setCenter(dashboardLabel);
    }

    @FXML
    private void showUsersView() {
        loadView("/utilisateur.fxml");
    }

    @FXML
    private void showArticlesView() {
        Label articlesLabel = new Label("🛒 Gestion des articles");
        articlesLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        rootBorderPane.setCenter(articlesLabel);
    }

    @FXML
    private void showCommandesView() {
        Label commandesLabel = new Label("📦 Gestion des commandes");
        commandesLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        rootBorderPane.setCenter(commandesLabel);
    }

    @FXML
    private void showEvenementView() {
        Label evenementLabel = new Label("🗓️ Gestion des événements");
        evenementLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        rootBorderPane.setCenter(evenementLabel);
    }

    @FXML
    private void showReservationsView() {
        Label reservationsLabel = new Label("📋 Gestion des réservations");
        reservationsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        rootBorderPane.setCenter(reservationsLabel);
    }

    @FXML
    private void showCategorieWorkshopView() {
        Label categorieWorkshopLabel = new Label("📈 Catégories des workshops");
        categorieWorkshopLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        rootBorderPane.setCenter(categorieWorkshopLabel);
    }

    @FXML
    private void showWorkshopsView() {
        Label workshopsLabel = new Label("📊 Liste des workshops");
        workshopsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        rootBorderPane.setCenter(workshopsLabel);
    }

    @FXML
    private void showPostsView() {
        Label postsLabel = new Label("📝 Gestion des posts");
        postsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        rootBorderPane.setCenter(postsLabel);
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            rootBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
