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


    @FXML
    void ajouterCategorieArticleAction(ActionEvent event) throws SQLException, IOException {
        CateArtService service = new CateArtService();

        if (isEditMode) {
            // modifier une cat
            CategorieArticle catToUpdate = new CategorieArticle();
            catToUpdate.setId(Integer.parseInt(idCategorieField.getText()));
            catToUpdate.setNom_categorie(nomCategorieField.getText());
            catToUpdate.setDescription_categorie(descriptionCategorieField.getText());
            catToUpdate.setImage_categorie(nomImagecatField.getText());

            service.update(catToUpdate);
        } else {
            // ajouter une nouvelle cat
            CategorieArticle newCat = new CategorieArticle(
                    nomCategorieField.getText(),
                    descriptionCategorieField.getText(),
                    nomImagecatField.getText()
            );
            service.add(newCat);
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(isEditMode ? "Catégorie modifiée avec succès !" : "Catégorie ajoutée avec succès !");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/affichageCategorieArticle.fxml"));
            Parent root = loader.load();
            confirmAddCatBtn.getScene().setRoot(root);
        }


    }


    @FXML
    private void annulerAction(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/affichageCategorieArticle.fxml"));
            Parent root = loader.load();
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
}











