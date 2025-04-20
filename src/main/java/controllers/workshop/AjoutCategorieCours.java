package controllers.workshop;

import entities.CategorieCours;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.CategorieCoursService;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.util.List;

public class AjoutCategorieCours {

    @FXML
    private TextField nomCategorieID;
    @FXML
    private TextArea descriptionCategorieID;
    @FXML
    private Label errorNom;
    @FXML
    private Label errorDescription;
    @FXML
    private Button btnSubmitID;
    @FXML
    private Button btnCancelID;

    @FXML
    void ajoutercategorie(ActionEvent event) {
        errorNom.setText("");
        errorDescription.setText("");

        boolean valid = true;
        if (nomCategorieID.getText().trim().isEmpty()) {
            errorNom.setText("Ce champ est obligatoire.");
            valid = false;
        }
        if (descriptionCategorieID.getText().trim().isEmpty()) {
            errorDescription.setText("Ce champ est obligatoire.");
            valid = false;
        }
        if (!valid) return;

        CategorieCoursService service = new CategorieCoursService();
        try {
            List<CategorieCours> listeExistante = service.displayList();
            for (CategorieCours cat : listeExistante) {
                if (cat.getNomCategorie().equalsIgnoreCase(nomCategorieID.getText().trim())) {
                    errorNom.setText("Ce nom de catégorie existe déjà.");
                    return;
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur Critique", "Erreur lors de la vérification : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
            return;
        }

        try {
            CategorieCours nouvelleCategorie = new CategorieCours();
            nouvelleCategorie.setNomCategorie(nomCategorieID.getText().trim());
            nouvelleCategorie.setDescriptionCategorie(descriptionCategorieID.getText().trim());

            service.add(nouvelleCategorie);

            showAlert("Succès", "Catégorie ajoutée avec succès", Alert.AlertType.CONFIRMATION);

            // Naviguer vers la liste des catégories
            navigateToBaseAdminView();

        } catch (SQLIntegrityConstraintViolationException ex) {
            errorNom.setText("Ce nom de catégorie existe déjà");
            ex.printStackTrace();
        } catch (Exception e) {
            showAlert("Erreur Critique", "Erreur : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void cancel(ActionEvent event) {
        navigateToBaseAdminView();
    }

    private void navigateToBaseAdminView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseAdmin.fxml"));
            Parent root = loader.load();

            // Utiliser une méthode spécifique dans BaseAdminController pour charger la vue Afficher
            controllers.BaseAdminController controller = loader.getController();
            controller.showCategorieWorkshopView();

            Stage stage = (Stage) btnCancelID.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}