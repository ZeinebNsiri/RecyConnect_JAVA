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

    // --- état initial ---
    private String origName;
    private String origDesc;

    /** Appelé par AfficherCategorieCours pour pré‑remplir */
    public void setCategorieCours(CategorieCours categorie) {
        this.currentCategorie = categorie;
        this.origName = categorie.getNomCategorie();
        this.origDesc = categorie.getDescriptionCategorie();

        nomCategorieField.setText(origName);
        descriptionField.setText(origDesc);
    }

    @FXML
    private void confirmerModification(ActionEvent event) {

        errorNom.setText("");
        errorDescription.setText("");

        String newName = nomCategorieField.getText().trim();
        String newDesc = descriptionField.getText().trim();

        // --- détection “aucune modification” ---
        if (newName.equals(origName) && newDesc.equals(origDesc)) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Vous n'avez modifié aucun champ.")
                    .showAndWait();
            return;
        }

        // --- validation des champs ---
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

        // --- unicité du nom si modifié ---
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
                showAlert("Erreur Critique",
                        "Impossible de vérifier l'existence : " + ex.getMessage(),
                        Alert.AlertType.ERROR);
                return;
            }
        }

        // --- application des changements ---
        currentCategorie.setNomCategorie(newName);
        currentCategorie.setDescriptionCategorie(newDesc);

        try {
            service.update(currentCategorie);
            showAlert("Succès",
                    "Catégorie modifiée avec succès.",
                    Alert.AlertType.CONFIRMATION);
            retourAfficherCategorie();
        } catch (SQLException ex) {
            showAlert("Erreur BD",
                    "Impossible de modifier la catégorie : " + ex.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void annulerModification(ActionEvent event) {
        retourAfficherCategorie();
    }

    private void retourAfficherCategorie() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/workshop/AfficherCategorieCours.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) btnAnnuler.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
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
