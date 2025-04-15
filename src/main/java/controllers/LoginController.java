package controllers;

import entities.utilisateur;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import utils.MyDataBase;
import utils.Session;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    public void Login(ActionEvent event) throws SQLException {
        String email = emailField.getText();
        String password = passwordField.getText();
        Connection connection = MyDataBase.getInstance().getConx();
        if(!email.isEmpty() && !password.isEmpty()) {
        try {
            // Étape 1 : Vérifier si l'email existe
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

            // Étape 2 : Vérifier le mot de passe
            String storedPassword = emailResult.getString("password");
            storedPassword = storedPassword.replaceFirst("^\\$2y\\$", "\\$2a\\$");
            if (!BCrypt.checkpw(password,storedPassword)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de connexion");
                alert.setContentText("Mot de passe incorrect.");
                alert.show();
                return;
            }else {

                // Étape 3 : Vérifier le statut
                boolean status = emailResult.getBoolean("status");
                if (!status) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Compte banni");
                    alert.setContentText("Votre compte est banni.");
                    alert.show();
                    return;
                }

                // Étape 4 : Vérifier le rôle
                String role = emailResult.getString("roles");
                int id = emailResult.getInt("id");
                //String nom = emailResult.getString("nom");
//                utilisateur user = new utilisateur(id, username, role);
//                Session.getInstance().setCurrentUser(user);

                // Redirection
                Parent page;
                if (role.contains("ROLE_ADMIN")) {
//                page = FXMLLoader.load(getClass().getResource("/dashboardAdmin.fxml"));
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("roles");
                    alert.setContentText("ADMIN.");
                    alert.show();
                } else {
//                page = FXMLLoader.load(getClass().getResource("/Home.fxml"));
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("roles");
                    alert.setContentText("user");
                    alert.show();
                }
            }

//            Scene scene = new Scene(page);
//            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//            stage.setScene(scene);
//            stage.show();

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
        }
    }

}

