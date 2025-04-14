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

    @FXML
    private TextField nomCategorieField; // Champ pour le vrai nom de la catégorie
    @FXML
    private TextArea descriptionField;   // Champ pour la vraie description
    @FXML
    private Label errorNom;              // Label d'erreur pour le champ nom
    @FXML
    private Label errorDescription;      // Label d'erreur pour le champ description
    @FXML
    private Button btnConfirmer;         // Bouton pour confirmer la modification
    @FXML
    private Button btnAnnuler;           // Bouton pour annuler la modification

    private final CategorieCoursService service = new CategorieCoursService();
    private CategorieCours currentCategorie; // L'objet à modifier

    // Cette méthode reçoit la catégorie sélectionnée et remplit le formulaire
    public void setCategorieCours(CategorieCours categorie) {
        this.currentCategorie = categorie;
        nomCategorieField.setText(categorie.getNomCategorie());
        descriptionField.setText(categorie.getDescriptionCategorie());
    }

    // Méthode appelée lorsque l'utilisateur clique sur "Confirmer"
    @FXML
    private void confirmerModification(ActionEvent event) {
        // Réinitialiser les messages d'erreur
        errorNom.setText("");
        errorDescription.setText("");

        boolean valid = true;
        String newName = nomCategorieField.getText().trim();
        String newDescription = descriptionField.getText().trim();

        // Contrôle de saisie : vérifier que les champs ne sont pas vides
        if (newName.isEmpty()) {
            errorNom.setText("Ce champ est obligatoire.");
            valid = false;
        }
        if (newDescription.isEmpty()) {
            errorDescription.setText("Ce champ est obligatoire.");
            valid = false;
        }
        if (!valid) {
            return;
        }

        // Si le nom a été modifié, vérifier son unicité
        if (!newName.equalsIgnoreCase(currentCategorie.getNomCategorie())) {
            try {
                List<CategorieCours> listeExistante = service.displayList();
                for (CategorieCours cat : listeExistante) {
                    // On vérifie que le nouveau nom n'existe pas déjà
                    if (cat.getNomCategorie().equalsIgnoreCase(newName)) {
                        errorNom.setText("Ce nom de catégorie existe déjà.");
                        return;
                    }
                }
            } catch (SQLException e) {
                showAlert("Erreur Critique", "Erreur lors de la vérification : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
                return;
            }
        }

        try {
            // Mettre à jour l'objet avec les nouvelles valeurs
            currentCategorie.setNomCategorie(newName);
            currentCategorie.setDescriptionCategorie(newDescription);
            // Appel du service pour mettre à jour la catégorie en base de données
            service.update(currentCategorie);
            // Affichage d'une confirmation (popup) indiquant que la modification a réussi
            showAlert("Succès", "Catégorie modifiée avec succès", Alert.AlertType.CONFIRMATION);
            // Retour à l'interface d'affichage
            retourAfficherCategorie();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthode appelée lorsque l'utilisateur clique sur "Annuler"
    @FXML
    private void annulerModification(ActionEvent event) {
        retourAfficherCategorie();
    }

    // Recharge l'interface d'affichage des catégories
    private void retourAfficherCategorie() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/workshop/AfficherCategorieCours.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnAnnuler.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Affiche une alerte pop-up en cas d'erreur critique ou pour confirmer une action
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
