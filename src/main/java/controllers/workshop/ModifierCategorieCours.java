package controllers.workshop;

import controllers.BaseAdminController;
import entities.CategorieCours;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import services.CategorieCoursService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ModifierCategorieCours {

    @FXML private TextField nomCategorieField;
    @FXML private TextArea descriptionField;
    @FXML private Label errorNom;
    @FXML private Label errorDescription;
    @FXML private Button btnConfirmer;
    @FXML private Button btnAnnuler;

    private final CategorieCoursService service = new CategorieCoursService();
    private CategorieCours currentCategorie;
    private BaseAdminController baseAdminController;

    private String origName;
    private String origDesc;

    public void setCategorieCours(CategorieCours categorie) {
        this.currentCategorie = categorie;
        this.origName = categorie.getNomCategorie();
        this.origDesc = categorie.getDescriptionCategorie();

        nomCategorieField.setText(origName);
        descriptionField.setText(origDesc);
    }

    public void setBaseAdminController(BaseAdminController controller) {
        this.baseAdminController = controller;
        System.out.println("BaseAdminController set: " + (baseAdminController != null));
    }

    @FXML
    private void confirmerModification(ActionEvent event) {
        errorNom.setText("");
        errorDescription.setText("");

        String newName = nomCategorieField.getText().trim();
        String newDesc = descriptionField.getText().trim();

        if (newName.equals(origName) && newDesc.equals(origDesc)) {
            new Alert(Alert.AlertType.INFORMATION, "Vous n'avez modifié aucun champ.").showAndWait();
            return;
        }

        boolean valid = true;
        if (newName.isEmpty()) {
            errorNom.setText("Ce champ est obligatoire.");
            valid = false;
        }
        if (newDesc.isEmpty()) {
            errorDescription.setText("Ce champ est obligatoire.");
            valid = false;
        }
        if (!valid) return;

        if (!newName.equalsIgnoreCase(origName)) {
            try {
                List<CategorieCours> liste = service.displayList();
                for (CategorieCours cat : liste) {
                    if (cat.getNomCategorie().equalsIgnoreCase(newName)) {
                        errorNom.setText("Ce nom de catégorie existe déjà.");
                        return;
                    }
                }
            } catch (SQLException ex) {
                showAlert("Erreur Critique", "Impossible de vérifier l'existence : " + ex.getMessage(), Alert.AlertType.ERROR);
                return;
            }
        }

        currentCategorie.setNomCategorie(newName);
        currentCategorie.setDescriptionCategorie(newDesc);

        try {
            service.update(currentCategorie);
            showAlert("Succès", "Catégorie modifiée avec succès.", Alert.AlertType.CONFIRMATION);
            retourAfficherCategorie();
        } catch (SQLException ex) {
            showAlert("Erreur BD", "Impossible de modifier la catégorie : " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void annulerModification(ActionEvent event) {
        System.out.println("Annuler button clicked!");
        retourAfficherCategorie();
    }

    private void retourAfficherCategorie() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        boolean wasMaximized = stage.isMaximized();

        if (baseAdminController != null) {
            System.out.println("Navigating back to AfficherCategorieCours using stored BaseAdminController...");
            baseAdminController.showCategorieWorkshopView();
            stage.setMaximized(wasMaximized);
            return; // Return to avoid executing the fallback
        }

        // Fallback: Load a new BaseAdmin.fxml
        System.out.println("BaseAdminController not set, falling back to load a new BaseAdmin.fxml...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseAdmin.fxml"));
            Parent root = loader.load();
            BaseAdminController controller = loader.getController();
            controller.showCategorieWorkshopView();
            stage.setScene(new Scene(root));
            stage.setMaximized(wasMaximized);
            stage.show();
        } catch (IOException ex) {
            System.err.println("Error loading BaseAdmin.fxml: " + ex.getMessage());
            ex.printStackTrace();
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