
package controllers;

import entities.CategorieArticle;
import entities.Notification;
import entities.utilisateur;
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
        Label dashboardLabel = new Label("📊 Tableau de bord");
        dashboardLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        rootBorderPane.setCenter(dashboardLabel);
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


}
