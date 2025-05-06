package controllers;

import controllers.workshop.DetailsWorkshopFront;
import entities.Event;
import entities.utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
    public static BaseUserController instance;


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
        instance = this;
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

    @FXML
    public void showWorkshopsView() {
        loadView("/workshop/AfficherWorkshopsFront.fxml");
    }


    public void showWorkshopDetails(entities.Cours cours) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/workshop/DetailsWorkshopFront.fxml")
            );
            Parent view = loader.load();


            // configure le controller avec le cours
            DetailsWorkshopFront ctrl = loader.getController();
            ctrl.setCours(cours);


            // injecte dans le centre
            rootBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void showEventsView() {
        loadView("/EventViews/ListEventsFront.fxml");
    }
    public void loadMyReservationsView() {


        try {
            Parent view = FXMLLoader.load(getClass().getResource("/ReservationViews/ReservationsListFront.fxml"));
            rootBorderPane.setCenter(view);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void showEventDetails(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventViews/EventDetails.fxml"));
            Parent view = loader.load();

            controllers.Events.EventDetailsController controller = loader.getController();
            controller.setEvent(event);

            rootBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void showRecommendationsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventViews/RecommendationView.fxml"));
            Parent view = loader.load();
            rootBorderPane.setCenter(view);
        } catch (IOException e) {
            // Simple error handling without dedicated showAlert
            new Alert(Alert.AlertType.ERROR,
                    "Failed to load recommendations: " + e.getMessage()).show();
            e.printStackTrace();
        }
    }


    public void showEditReservation(entities.Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReservationViews/ReservationEdit.fxml"));
            Parent view = loader.load();

            // Pass the reservation to the controller
            controllers.Reservations.ReservationEditController controller = loader.getController();
            controller.setReservation(reservation);

            rootBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}