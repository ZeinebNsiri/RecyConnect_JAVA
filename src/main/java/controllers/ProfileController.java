package controllers;


import entities.utilisateur;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import services.UtilisateurService;
import utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class ProfileController {

    @FXML private TabPane tabPane;

    // Affichage
    @FXML private ImageView profileImage;
    @FXML private Label fullNameLabel, roleLabel, nomLabel, prenomLabel, emailLabel, telLabel, adresseLabel;

    // Formulaire
    @FXML private TextField nomField, prenomField, emailField, telField, adresseField;
    @FXML private Label fileNameLabel;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;


    private File selectedFile;
    private utilisateur currentUser;
    private final UtilisateurService userService = new UtilisateurService();
    private static final String PROFILE_IMAGE_DIR = "C:/Users/azizz/OneDrive/Bureau/Recyconnect/public/uploads/profile_dir";

    @FXML
    public void initialize() {
        currentUser = Session.getInstance().getCurrentUser();
        showProfileInfo();


        // 👇 Cercle de clipping pour rendre l'image circulaire
        Circle clip = new Circle(80, 80, 80); // x, y, radius
        profileImage.setClip(clip);
    }

    private void showProfileInfo() {
        if (currentUser == null) return;

        fullNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom_user());
        if (currentUser.getRoles().contains("ADMIN")) {
            roleLabel.setText("Administrateur");
        } else if (currentUser.getRoles().contains("USER")) {
            roleLabel.setText("Particulier");
        } else {
            roleLabel.setText("Professionnel");
        }
        nomLabel.setText(currentUser.getNom_user());
        prenomLabel.setText(currentUser.getPrenom());
        emailLabel.setText(currentUser.getEmail());
        telLabel.setText(currentUser.getNum_tel());
        adresseLabel.setText(currentUser.getAdresse());

        nomField.setText(currentUser.getNom_user());
        prenomField.setText(currentUser.getPrenom());
        emailField.setText(currentUser.getEmail());
        telField.setText(currentUser.getNum_tel());
        adresseField.setText(currentUser.getAdresse());

        // Image
        String imageName = (currentUser.getPhoto_profil() != null && !currentUser.getPhoto_profil().isEmpty())
                ? currentUser.getPhoto_profil()
                : "profile.jpg";

        File imgFile = new File(PROFILE_IMAGE_DIR, imageName);
        if (!imgFile.exists()) {
            imgFile = new File(PROFILE_IMAGE_DIR, "profile.jpg");
        }

        profileImage.setImage(new Image(imgFile.toURI().toString()));
    }

    @FXML
    public void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            fileNameLabel.setText(selectedFile.getName());
        }
    }

    @FXML
    public void handleSave() {
        if (currentUser == null) return;

        currentUser.setNom_user(nomField.getText());
        currentUser.setPrenom(prenomField.getText());
        currentUser.setEmail(emailField.getText());
        currentUser.setNum_tel(telField.getText());
        currentUser.setAdresse(adresseField.getText());
        if (currentUser.getRoles().contains("PROFESSIONNEL")) {
            currentUser.setRoles("ROLE_PROFESSIONNEL");
        } else if (currentUser.getRoles().contains("USER")) {
            currentUser.setRoles("ROLE_USER");
        }else  {
            currentUser.setRoles("ROLE_ADMIN");
        }

        // Sauvegarde image si une a été choisie
        if (selectedFile != null) {
            try {
                File destDir = new File(PROFILE_IMAGE_DIR);
                if (!destDir.exists()) destDir.mkdirs();

                File destFile = new File(destDir, selectedFile.getName());
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                currentUser.setPhoto_profil(selectedFile.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!ControleVide()) {
            UtilisateurService utilisateurService = new UtilisateurService();
            if (!utilisateurService.isValidEmail(emailField.getText())) {
                showAlertControle("Email invalide !");
            } else if (!utilisateurService.isValidPhoneNumber(telField.getText())) {
                showAlertControle("Le numéro de téléphone doit contenir exactement 8 chiffres.");
            } else {
                try {
                    utilisateurService.update(currentUser);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                showProfileInfo(); // Refresh
                tabPane.getSelectionModel().select(0);
            }
        }
    }

    public boolean ControleVide() {

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
    private void handlePasswordChange() {
        String oldPass = oldPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        utilisateur currentUser = Session.getInstance().getCurrentUser();
        UtilisateurService service = new UtilisateurService();
        String currPass = currentUser.getPassword();
        currPass = currPass.replaceFirst("^\\$2y\\$", "\\$2a\\$");

        try {
            if (!BCrypt.checkpw(oldPass,currPass)) {
                showAlertControle("L'ancien mot de passe est incorrect.");
                return;
            }

            if (!newPass.equals(confirmPass)) {
                showAlertControle( "Les nouveaux mots de passe ne correspondent pas.");
                return;
            }

            if (!service.isValidPassword(newPass)) {
                showAlertControle(service.getPasswordError(newPass));
                return;
            }
            currentUser.setPassword(newPass);
            service.updatePassword(currentUser.getId(),newPass);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Mot de passe mis à jour avec succès.");
            alert.showAndWait();
            oldPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlertControle("Erreur lors de la mise à jour du mot de passe.");
        }
    }


}
