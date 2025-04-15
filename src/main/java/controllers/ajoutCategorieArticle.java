package controllers;

import entities.CategorieArticle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import services.CateArtService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ajoutCategorieArticle {

    @FXML
    private Button cancelCatBtn;

    @FXML
    private Button confirmAddCatBtn;

    @FXML
    private TextArea descriptionCategorieField;

    @FXML
    private TextField nomCategorieField;

    @FXML
    private TextField nomImagecatField;

    @FXML
    private TextField idCategorieField;

    private boolean isEditMode = false;

    private CateArtService service = new CateArtService();

    @FXML
    void ajouterCategorieArticleAction(ActionEvent event) throws SQLException, IOException {
        //  Vérification des champs
        if (!validerChamps()) return;

        //  Créer ou modifier la catégorie
        if (isEditMode) {
            CategorieArticle catToUpdate = new CategorieArticle();
            catToUpdate.setId(Integer.parseInt(idCategorieField.getText()));
            catToUpdate.setNom_categorie(nomCategorieField.getText());
            catToUpdate.setDescription_categorie(descriptionCategorieField.getText());
            catToUpdate.setImage_categorie(nomImagecatField.getText());
            service.update(catToUpdate);
        } else {
            CategorieArticle newCat = new CategorieArticle(
                    nomCategorieField.getText(),
                    descriptionCategorieField.getText(),
                    nomImagecatField.getText()
            );
            service.add(newCat);
        }

        //  Confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(isEditMode ? "Catégorie modifiée avec succès !" : "Catégorie ajoutée avec succès !");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseAdmin.fxml"));
            Parent root = loader.load();

            BaseAdminController controller = loader.getController();
            controller.showCategorieView();

            confirmAddCatBtn.getScene().setRoot(root);
        }
    }

    @FXML
    private void annulerAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseAdmin.fxml"));
            Parent root = loader.load();

            BaseAdminController controller = loader.getController();
            controller.showCategorieView();

            cancelCatBtn.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadCategorieData(CategorieArticle cat) {
        idCategorieField.setText(String.valueOf(cat.getId()));
        nomCategorieField.setText(cat.getNom_categorie());
        descriptionCategorieField.setText(cat.getDescription_categorie());
        nomImagecatField.setText(cat.getImage_categorie());

        confirmAddCatBtn.setText("Modifier");
        isEditMode = true;
    }

    private boolean validerChamps() throws SQLException {
        String nom = nomCategorieField.getText().trim();
        String desc = descriptionCategorieField.getText().trim();
        String image = nomImagecatField.getText().trim();

        if (nom.isEmpty() || desc.isEmpty() || image.isEmpty()) {
            showAlert("Tous les champs doivent être remplis !");
            return false;
        }

        List<CategorieArticle> existantes = service.displayList();

        for (CategorieArticle cat : existantes) {
            boolean sameName = cat.getNom_categorie().equalsIgnoreCase(nom);
            boolean isSameId = isEditMode && cat.getId() == Integer.parseInt(idCategorieField.getText());

            if (sameName && !isSameId) {
                showAlert("Une catégorie avec ce nom existe déjà !");
                return false;
            }
        }

        return true;
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Erreur de saisie");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
