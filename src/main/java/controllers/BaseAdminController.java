
package controllers;

import controllers.Events.EventAddController;
import controllers.Events.EventEditController;
import controllers.workshop.AjouterCours;
import controllers.workshop.ModifierCategorieCours;
import controllers.workshop.ModifierCours;
import entities.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import services.NotificationService;
import utils.Session;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javafx.stage.Popup;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.Node;


public class BaseAdminController {



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
    @FXML private Button notificationButton;
    @FXML private Label notifBadge;

    @FXML
    private StackPane contentPane;

    private final NotificationService notifService = new NotificationService();
    private Popup notifPopup = new Popup();



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
        updateNotifBadge();
        rootBorderPane.setUserData(this);


    }
    public void updateNotifBadge() {
        try {
            int unreadCount = notifService.countUnread();
            notifBadge.setText(String.valueOf(unreadCount));
            notifBadge.setVisible(unreadCount > 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void showNotifications() {
        notifPopup.getContent().clear();

        VBox notifBox = new VBox();
        notifBox.setPadding(new Insets(10));
        notifBox.setSpacing(10);
        notifBox.setStyle("-fx-background-color: white; -fx-border-color: lightgray; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        try {
            List<Notification> notifs = notifService.getUnread();

            HBox titleBar = new HBox();
            titleBar.setSpacing(10);
            titleBar.setAlignment(Pos.CENTER_LEFT);
            titleBar.setPadding(new Insets(10));
            titleBar.setPrefWidth(400);

            Label title = new Label("Vous avez " + notifs.size() + " nouvelles notifications");
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            title.setWrapText(true);
            title.setMaxWidth(Double.MAX_VALUE);
            title.setEllipsisString("");
            HBox.setHgrow(title, Priority.ALWAYS);

            Button voirToutBtn = new Button("Tout voir");
            voirToutBtn.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-background-radius: 20px; -fx-font-weight: bold;");
            voirToutBtn.setMinWidth(90);
            voirToutBtn.setPrefWidth(100);

            voirToutBtn.setOnAction(e -> loadView("/Notifications.fxml"));


            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            titleBar.getChildren().addAll(title, spacer, voirToutBtn);
            notifBox.getChildren().add(titleBar);




            for (Notification notif : notifs) {
                VBox item = new VBox();
                item.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 8; -fx-border-color: #ddd;");
                item.setSpacing(5);

                Label msg = new Label(notif.getMessage());
                msg.setStyle("-fx-text-fill: #333;");
                Label date = new Label(notif.getCreated_at().toString());
                date.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
                item.getChildren().addAll(msg, date);

                notifBox.getChildren().add(item);
            }

            notifPopup.getContent().add(notifBox);
            notifPopup.setAutoHide(true);
            notifPopup.setOnHidden(e -> markNotificationsAsRead());

            notifPopup.show(notificationButton,
                    notificationButton.localToScreen(0, 0).getX(),
                    notificationButton.localToScreen(0, 0).getY() + 30);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void markNotificationsAsRead() {
        try {
            notifService.markAllAsRead();
            updateNotifBadge();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    public void showStatistiquesUserView() {
        loadView("/StatistiquesUtilisateur.fxml");
    }

    @FXML
    public void showDashboardView() {
        loadView("/EventViews/dashboardEvents.fxml");  // Ensure the correct FXML file path
    }

    public void showAddEventView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventViews/EventAdd.fxml"));
            Parent view = loader.load();

            EventAddController controller = loader.getController();
            controller.setContentPane(contentPane); // Pass the reference

            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void showUsersView() {
        loadView("/AffichageUtilisateur.fxml");
    }

    @FXML
    public void showCategorieView() {
        loadView("/affichageCategorieArticle.fxml");
    }


    @FXML
    public void showArticlesView() {
        loadView("/ListeArticleAdmin.fxml");
    }


    public void showStatistiquesView() {
        loadView("/StatistiquesArticle.fxml");
    }

    public void showAjoutCategorieView() {
        loadView("/ajoutCategorieArticle.fxml");
    }


    @FXML
    public void showCommandesView() {
        Label commandesLabel = new Label("📦 Gestion des commandes");
        commandesLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        rootBorderPane.setCenter(commandesLabel);
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
    public void showCategorieWorkshopView() {
        loadView("/workshop/AfficherCategorieCours.fxml");
    }


    @FXML
    public void showWorkshopsView() {
        loadView("/workshop/AfficherCours.fxml");
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
    private void logout() throws IOException {
        // Déconnexion
        Session.getInstance().logout();

        // Charger la nouvelle page Login.fxml
        Parent loginRoot = FXMLLoader.load(getClass().getResource("/Login.fxml"));
        Scene loginScene = new Scene(loginRoot);

        // Récupérer le stage via un vrai Node
        Stage currentStage = (Stage) sceneReference.getScene().getWindow();
        currentStage.setScene(loginScene);
        currentStage.setTitle("Connexion");
        currentStage.show();
    }
    public void showAjoutCategorieViewWithData(CategorieArticle cat) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajoutCategorieArticle.fxml"));
            Parent view = loader.load();


            // Appel du contrôleur d'ajout pour pré-remplir le formulaire
            ajoutCategorieArticle controller = loader.getController();
            controller.loadCategorieData(cat);


            rootBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showModifierCategorieViewWithData(CategorieCours catcours) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/workshop/ModifierCategorieCours.fxml"));
            Parent view = loader.load();


            ModifierCategorieCours ctrl = loader.getController();
            ctrl.setCategorieCours(catcours);
            ctrl.setBaseAdminController(this);


            contentPane.getChildren().setAll(view); // Use contentPane instead of rootBorderPane.setCenter()
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void showAjoutCategorieView2() {
        loadView("/workshop/AjoutCategorieCours.fxml");
    }


    @FXML
    public void showAjouterCoursView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/workshop/AjouterCours.fxml"));
            Parent view = loader.load();
            AjouterCours ctrl = loader.getController();
            ctrl.setBaseAdminController(this); // Set the controller
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Error loading AjouterCours.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void showModifierCoursViewWithData(Cours c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/workshop/ModifierCours.fxml"));
            Parent content = loader.load();
            ModifierCours ctrl = loader.getController();
            ctrl.setCours(c);
            ctrl.setBaseAdminController(this); // Already set, confirming for consistency
            contentPane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showEditEventView(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventViews/EventEdit.fxml"));
            Parent view = loader.load();

            EventEditController editController = loader.getController();
            editController.setEvent(event);

            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
