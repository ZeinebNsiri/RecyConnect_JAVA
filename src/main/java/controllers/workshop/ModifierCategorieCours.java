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
    private TextField nomCategorieField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private Label errorNom;
    @FXML
    private Label errorDescription;
    @FXML
    private Button btnConfirmer;
    @FXML
    private Button btnAnnuler;

    private final CategorieCoursService service = new CategorieCoursService();
    private CategorieCours currentCategorie;


    public void setCategorieCours(CategorieCours categorie) {
        this.currentCategorie = categorie;
        nomCategorieField.setText(categorie.getNomCategorie());
        descriptionField.setText(categorie.getDescriptionCategorie());
    }


    @FXML
    private void confirmerModification(ActionEvent event) {

        errorNom.setText("");
        errorDescription.setText("");

        boolean valid = true;
        String newName = nomCategorieField.getText().trim();
        String newDescription = descriptionField.getText().trim();


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


        if (!newName.equalsIgnoreCase(currentCategorie.getNomCategorie())) {
            try {
                List<CategorieCours> listeExistante = service.displayList();
                for (CategorieCours cat : listeExistante) {

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

            currentCategorie.setNomCategorie(newName);
            currentCategorie.setDescriptionCategorie(newDescription);

            service.update(currentCategorie);

            showAlert("Succès", "Catégorie modifiée avec succès", Alert.AlertType.CONFIRMATION);

            retourAfficherCategorie();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void annulerModification(ActionEvent event) {
        retourAfficherCategorie();
    }


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


    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
