package controllers;


import entities.utilisateur;
import javafx.scene.shape.Circle;
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
        UtilisateurService utilisateurService = new UtilisateurService();
        try {
            utilisateurService.update(currentUser);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        showProfileInfo(); // Refresh
        tabPane.getSelectionModel().select(0);
    }
}
