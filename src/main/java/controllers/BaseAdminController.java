
package controllers;

import entities.utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import utils.Session;

import java.io.IOException;

public class BaseAdminController {

    @FXML
    private BorderPane rootBorderPane;
    @FXML
    private MenuButton userMenuButton;
    @FXML
    private MenuItem profilMenuItem;


    @FXML
    public void initialize() {
        utilisateur user = Session.getInstance().getCurrentUser();
        if (user != null) {
            String fullName = user.getPrenom() + " " + user.getNom_user();
            userMenuButton.setText(fullName);
        }
        profilMenuItem.setOnAction(e -> showProfileView());
    }

    @FXML
    public void showCategorieView() {
        loadView("/categorie.fxml");
    }

    @FXML
    public void showDashboardView() {
        Label dashboardLabel = new Label("📊 Tableau de bord");
        dashboardLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        rootBorderPane.setCenter(dashboardLabel);
    }

    @FXML
    public void showUsersView() {
        loadView("/AffichageUtilisateur.fxml");
    }

    @FXML
    public void showArticlesView() {
        Label articlesLabel = new Label("🛒 Gestion des articles");
        articlesLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        rootBorderPane.setCenter(articlesLabel);
    }

    @FXML
    public void showCommandesView() {
        Label commandesLabel = new Label("📦 Gestion des commandes");
        commandesLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        rootBorderPane.setCenter(commandesLabel);
    }

    @FXML
    public void showEvenementView() {
        Label evenementLabel = new Label("🗓️ Gestion des événements");
        evenementLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        rootBorderPane.setCenter(evenementLabel);
    }

    @FXML
    public void showReservationsView() {
        Label reservationsLabel = new Label("📋 Gestion des réservations");
        reservationsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        rootBorderPane.setCenter(reservationsLabel);
    }

    @FXML
    public void showCategorieWorkshopView() {
        Label categorieWorkshopLabel = new Label("📈 Catégories des workshops");
        categorieWorkshopLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        rootBorderPane.setCenter(categorieWorkshopLabel);
    }

    @FXML
    public void showWorkshopsView() {
        Label workshopsLabel = new Label("📊 Liste des workshops");
        workshopsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        rootBorderPane.setCenter(workshopsLabel);
    }

    @FXML
    public void showPostsView() {
        Label postsLabel = new Label("📝 Gestion des posts");
        postsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        rootBorderPane.setCenter(postsLabel);
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
}
