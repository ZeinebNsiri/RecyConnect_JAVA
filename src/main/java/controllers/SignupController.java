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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.UtilisateurService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class SignupController {

    @FXML private RadioButton radioParticulier;
    @FXML private RadioButton radioProfessionnel;
    @FXML private VBox formParticulier;
    @FXML private VBox formProfessionnel;
    @FXML private ImageView backgroundImage;

    // Champs mot de passe (Particulier)
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button togglePasswordVisibility;

    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordVisibleField;
    @FXML private Button toggleConfirmVisibility;

    // Champs mot de passe (Professionnel)
    @FXML private PasswordField proPasswordField;
    @FXML private TextField proPasswordVisibleField;
    @FXML private Button toggleProPasswordVisibility;

    @FXML private PasswordField proConfirmField;
    @FXML private TextField proConfirmVisibleField;
    @FXML private Button toggleProConfirmVisibility;

    private ToggleGroup typeGroup;

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField telField;
    @FXML private TextField emailField;

    @FXML private TextField proNomField;
    @FXML private TextField matriculeField;
    @FXML private TextField proTelField;
    @FXML private TextField proEmailField;


    @FXML
    public void initialize() {
        // Groupe radio
        typeGroup = new ToggleGroup();
        radioParticulier.setToggleGroup(typeGroup);
        radioProfessionnel.setToggleGroup(typeGroup);

        radioParticulier.setSelected(true);
        switchForm(true);

        radioParticulier.setOnAction(e -> switchForm(true));
        radioProfessionnel.setOnAction(e -> switchForm(false));

        // Chargement de l'image de fond depuis le dossier uploads
        File imageFile = new File("C:/Users/azizz/OneDrive/Bureau/Recyconnect/public/login_signup/images/auth/Login_bg8.jpg");
        if (imageFile.exists()) {
            Image img = new Image(imageFile.toURI().toString());
            backgroundImage.setImage(img);
        } else {
            System.out.println("Image non trouvée : " + imageFile.getAbsolutePath());
        }

        // Champs particulier : liaison + toggle
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
        confirmPasswordVisibleField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
        togglePasswordVisibility.setOnAction(e -> toggleVisibility(passwordField, passwordVisibleField));
        toggleConfirmVisibility.setOnAction(e -> toggleVisibility(confirmPasswordField, confirmPasswordVisibleField));

        // Champs professionnel : liaison + toggle
        proPasswordVisibleField.textProperty().bindBidirectional(proPasswordField.textProperty());
        proConfirmVisibleField.textProperty().bindBidirectional(proConfirmField.textProperty());
        toggleProPasswordVisibility.setOnAction(e -> toggleVisibility(proPasswordField, proPasswordVisibleField));
        toggleProConfirmVisibility.setOnAction(e -> toggleVisibility(proConfirmField, proConfirmVisibleField));
    }

    private void switchForm(boolean isParticulier) {
        formParticulier.setVisible(isParticulier);
        formParticulier.setManaged(isParticulier);
        formProfessionnel.setVisible(!isParticulier);
        formProfessionnel.setManaged(!isParticulier);
    }

    private void toggleVisibility(PasswordField hiddenField, TextField visibleField) {
        boolean isVisible = visibleField.isVisible();
        visibleField.setVisible(!isVisible);
        visibleField.setManaged(!isVisible);
        hiddenField.setVisible(isVisible);
        hiddenField.setManaged(isVisible);
    }

    @FXML
    void Sigup(ActionEvent event) throws SQLException {
        String motDePasse = passwordField.getText();
        String confirmerMotDePasse = confirmPasswordField.getText();
        String motDePassePro = proPasswordField.getText();
        String confirmerMotDePassePro = proConfirmField.getText();
        if (!ControleVide()){
            if (radioParticulier.isSelected()) {
                String nom = nomField.getText();
                String prenom = prenomField.getText();
                String tel = telField.getText();
                String email = emailField.getText();
                UtilisateurService utilisateurService = new UtilisateurService();
                if (!utilisateurService.isValidEmail(email)) {
                    showAlertControle("Email invalide !");
                } else if (!utilisateurService.isValidPhoneNumber(tel)) {
                    showAlertControle("Le numéro de téléphone doit contenir exactement 8 chiffres.");
                } else if (!utilisateurService.isValidPassword(motDePasse)) {
                    showAlertControle(utilisateurService.getPasswordError(motDePasse));
                } else if (motDePasse.equals(confirmerMotDePasse)) {

                    try {
                        utilisateurService.add(new utilisateur(email, nom, prenom, "ROLE_USER", tel, motDePasse, true));
                        clearAll();
                    } catch (SQLIntegrityConstraintViolationException e) {

                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Erreur d'inscription");
                        error.setHeaderText(null);
                        if (e.getMessage().contains("UNIQ_IDENTIFIER_EMAIL")) {
                            error.setContentText("Cette adresse email est déjà utilisée. Veuillez en utiliser une autre.");
                        } else {
                            error.setContentText("Une contrainte de base de données a été violée: " + e.getMessage());
                        }
                        error.showAndWait();

                    } catch (SQLException e) {

                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Erreur");
                        error.setHeaderText(null);
                        error.setContentText(e.getMessage());
                        error.showAndWait();
                        throw e;

                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur de validation");
                    alert.setHeaderText(null);
                    alert.setContentText("Les mots de passe ne correspondent pas !");
                    alert.showAndWait();
                }

            } else if (radioProfessionnel.isSelected()) {
                String nom = proNomField.getText();
                String matricule = matriculeField.getText();
                String tel = proTelField.getText();
                String email = proEmailField.getText();
                UtilisateurService utilisateurService = new UtilisateurService();

                if (!utilisateurService.isValidEmail(email)) {
                    showAlertControle("Email invalide !");
                } else if (!utilisateurService.isValidPhoneNumber(tel)) {
                    showAlertControle("Le numéro de téléphone doit contenir exactement 8 chiffres.");
                } else if (!utilisateurService.isValidPassword(motDePassePro)) {
                    showAlertControle(utilisateurService.getPasswordError(motDePassePro));
                } else if (!utilisateurService.isValidMatriculeFiscale(matricule)) {
                    showAlertControle("La matricule fiscale doit contenir exactement 13 chiffres.");
                } else if (motDePassePro.equals(confirmerMotDePassePro)) {

                    try {
                        utilisateurService.add(new utilisateur(email, nom, "ROLE_PROFESSIONNEL", tel, motDePassePro, true, matricule));
                        clearAll();
                    }catch (SQLIntegrityConstraintViolationException e) {

                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Erreur d'inscription");
                        error.setHeaderText(null);
                        if (e.getMessage().contains("UNIQ_IDENTIFIER_EMAIL")) {
                            error.setContentText("Cette adresse email est déjà utilisée. Veuillez en utiliser une autre.");
                        } else {
                            error.setContentText("Une contrainte de base de données a été violée: " + e.getMessage());
                        }
                        error.showAndWait();

                    } catch (SQLException e) {

                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Erreur");
                        error.setHeaderText(null);
                        error.setContentText(e.getMessage());
                        error.showAndWait();
                        throw e;

                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur de validation");
                    alert.setHeaderText(null);
                    alert.setContentText("Les mots de passe ne correspondent pas !");
                    alert.showAndWait();
                }

            }
        }
    }
    private void clearAll() {
        // Champs Particulier
        nomField.clear();
        prenomField.clear();
        telField.clear();
        emailField.clear();
        passwordField.clear();
        passwordVisibleField.clear();
        confirmPasswordField.clear();
        confirmPasswordVisibleField.clear();

        // Champs Professionnel
        proNomField.clear();
        matriculeField.clear();
        proTelField.clear();
        proEmailField.clear();
        proPasswordField.clear();
        proPasswordVisibleField.clear();
        proConfirmField.clear();
        proConfirmVisibleField.clear();
    }

    public boolean ControleVide() {
        if (radioParticulier.isSelected()) {
            if (nomField.getText().isEmpty()) {
                showAlert("Nom");
                return true;
            }
            if (prenomField.getText().isEmpty()) {
                showAlert("Prénom");
                return true;
            }
            if (telField.getText().isEmpty()) {
                showAlert("Numéro de téléphone");
                return true;
            }
            if (emailField.getText().isEmpty()) {
                showAlert("Email");
                return true;
            }
            if (passwordField.getText().isEmpty()) {
                showAlert("Mot de passe");
                return true;
            }
            if (confirmPasswordField.getText().isEmpty()) {
                showAlert("Confirmation du mot de passe");
                return true;
            }
        }

        if (radioProfessionnel.isSelected()) {
            if (proNomField.getText().isEmpty()) {
                showAlert("Nom (Professionnel)");
                return true;
            }
            if (matriculeField.getText().isEmpty()) {
                showAlert("Matricule fiscale");
                return true;
            }
            if (proTelField.getText().isEmpty()) {
                showAlert("Numéro de téléphone (Professionnel)");
                return true;
            }
            if (proEmailField.getText().isEmpty()) {
                showAlert("Email (Professionnel)");
                return true;
            }
            if (proPasswordField.getText().isEmpty()) {
                showAlert("Mot de passe (Professionnel)");
                return true;
            }
            if (proConfirmField.getText().isEmpty()) {
                showAlert("Confirmation du mot de passe (Professionnel)");
                return true;
            }
        }

        return false;
    }
    private void showAlert(String champ) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Champ obligatoire");
        alert.setHeaderText(null);
        alert.setContentText("Le champ \"" + champ + "\" est vide.");
        alert.showAndWait();
    }

    private void showAlertControle(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de validation");
        alert.setContentText(message);
        alert.showAndWait();
    }



    @FXML
    void loginD(MouseEvent event) throws IOException {
        Parent page = FXMLLoader.load(getClass().getResource("/Login.fxml"));
        Scene scene = new Scene(page);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }


}
