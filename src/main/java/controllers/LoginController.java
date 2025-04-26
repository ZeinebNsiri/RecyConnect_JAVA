package controllers;

import entities.utilisateur;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.mindrot.jbcrypt.BCrypt;
import services.FaceRecognitionService;
import services.UtilisateurService;
import utils.MyDataBase;
import utils.Session;
import utils.WebcamUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button togglePasswordVisibility;
    @FXML private ImageView backgroundImage;

    @FXML
    public void initialize() {
        // Lier les champs visible/caché
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());

        togglePasswordVisibility.setOnAction(e -> toggleVisibility(passwordField, passwordVisibleField));

        // Charger l’image de fond
        File imageFile = new File("C:/Users/azizz/OneDrive/Bureau/Recyconnect/public/login_signup/images/auth/Login_bg8.jpg");
        if (imageFile.exists()) {
            Image img = new Image(imageFile.toURI().toString());
            backgroundImage.setImage(img);
        }
    }

    private void toggleVisibility(PasswordField hidden, TextField visible) {
        boolean isVisible = visible.isVisible();
        visible.setVisible(!isVisible);
        visible.setManaged(!isVisible);
        hidden.setVisible(isVisible);
        hidden.setManaged(isVisible);
    }

    @FXML
    void signupD(MouseEvent event) throws IOException {
        Parent page = FXMLLoader.load(getClass().getResource("/Signup.fxml"));
        Scene scene = new Scene(page);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void Login(ActionEvent event) throws SQLException, IOException {
        String email = emailField.getText();
        String password = passwordField.getText();
        Connection connection = MyDataBase.getInstance().getConx();
        if(!email.isEmpty() && !password.isEmpty()) {
            try {
                // email existe
                String emailQuery = "SELECT * FROM utilisateur WHERE email = ?";
                PreparedStatement emailStmt = connection.prepareStatement(emailQuery);
                emailStmt.setString(1, email);
                ResultSet emailResult = emailStmt.executeQuery();

                if (!emailResult.next()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur de connexion");
                    alert.setContentText("Email inexistant.");
                    alert.show();
                    return;
                }

                // Étape 2 :  mot de passe
                String storedPassword = emailResult.getString("password");
                storedPassword = storedPassword.replaceFirst("^\\$2y\\$", "\\$2a\\$");
                if (!BCrypt.checkpw(password,storedPassword)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur de connexion");
                    alert.setContentText("Mot de passe incorrect.");
                    alert.show();
                    return;
                }else {

                    // Étape 3 :  statut
                    boolean status = emailResult.getBoolean("status");
                    if (!status) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Compte banni");
                        alert.setContentText("Votre compte est banni.");
                        alert.show();
                        return;
                    }

                    // Étape 4 :  rôle
                    String role = emailResult.getString("roles");
                    int id = emailResult.getInt("id");


                    utilisateur user = new utilisateur(id,emailResult.getString(2),emailResult.getString(4),emailResult.getString(5),emailResult.getString("roles"),emailResult.getString(6),emailResult.getString(7),emailResult.getString(8),emailResult.getBoolean(10),emailResult.getString(9),emailResult.getString(11));
                   Session.getInstance().setCurrentUser(user);

                    // Redirection
                    Parent page;
                    if (role.contains("ROLE_ADMIN")) {
                        page = FXMLLoader.load(getClass().getResource("/BaseAdmin.fxml"));
                        Scene scene = new Scene(page);
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(scene);
                        stage.show();
                    } else {
                        page = FXMLLoader.load(getClass().getResource("/BaseUser.fxml"));
                        Scene scene = new Scene(page);
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(scene);
                        stage.show();
                    }



                }
            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur base de données");
                alert.setContentText("Une erreur est survenue lors de la connexion.");
                alert.show();
            }
        }else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de connexion");
            alert.setContentText("champs vides.");
            alert.show();
        }
    }

    @FXML
    private void loginWithFace(ActionEvent event) throws Exception {
        // Créer et afficher un indicateur de chargement JavaFX
        Stage loadingStage = createLoadingStage("Authentification par reconnaissance faciale...");
        loadingStage.show();

        // Créer une tâche en arrière-plan avec Task de JavaFX
        Task<Boolean> task = new Task<Boolean>() {
            private utilisateur matchedUser = null;

            @Override
            protected Boolean call() throws Exception {
                File capturedImage = WebcamUtils.captureImage();
                if (capturedImage == null) {
                    return false;
                }

                FaceRecognitionService faceService = new FaceRecognitionService();
                List<utilisateur> users = new UtilisateurService().displayList();

                for (utilisateur user : users) {
                    if (user.getFace_image() != null) {
                        System.out.println(faceService.compareFaces(user.getFace_image(), capturedImage));

                        if (faceService.compareFaces(user.getFace_image(), capturedImage)) {
                            matchedUser = user;
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            protected void succeeded() {
                // Fermer la fenêtre de chargement
                Platform.runLater(() -> {
                    loadingStage.close();

                    try {
                        Boolean success = this.getValue();

                        if (success && matchedUser != null) {
                            if (!matchedUser.isStatus()) {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Compte banni");
                                alert.setContentText("Votre compte est banni.");
                                alert.show();
                                return;
                            }

                            Session.getInstance().setCurrentUser(matchedUser);

                            // Redirection
                            String role = matchedUser.getRoles();
                            String fxml = role.contains("ADMIN") ? "/BaseAdmin.fxml" : "/BaseUser.fxml";
                            Parent page = FXMLLoader.load(getClass().getResource(fxml));
                            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                            stage.setScene(new Scene(page));
                            stage.show();
                        } else {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Échec de l'authentification");
                            alert.setContentText("Aucune correspondance trouvée pour votre visage.");
                            alert.show();
                        }
                    } catch (Exception e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur");
                        alert.setContentText("Une erreur est survenue: " + e.getMessage());
                        alert.show();
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    loadingStage.close();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setContentText("Une erreur est survenue lors de la reconnaissance faciale.");
                    alert.show();
                });
            }
        };

        // Démarrer la tâche dans un thread séparé
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    private Stage createLoadingStage(String message) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white; -fx-border-color: #cccccc;");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(50, 50);

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px;");

        root.getChildren().addAll(progressIndicator, messageLabel);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);

        // Centre la fenêtre sur l'écran
        stage.centerOnScreen();

        return stage;
    }

}

