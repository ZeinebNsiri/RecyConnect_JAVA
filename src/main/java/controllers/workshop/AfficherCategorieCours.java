package controllers.workshop;

import entities.CategorieCours;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import services.CategorieCoursService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherCategorieCours {

    @FXML
    private TableView<CategorieCours> categorieTableView;

    @FXML
    private TableColumn<CategorieCours, Integer> colId;

    @FXML
    private TableColumn<CategorieCours, String> colNom;
    @FXML
    private TableColumn<CategorieCours, String> colDescription;

    @FXML
    private TableColumn<CategorieCours, Void> colDetails;

    @FXML
    private Button btnAjouter;

    private final CategorieCoursService service = new CategorieCoursService();

    @FXML
    private void initialize() {
        // ID -> "id"
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        // « Nom de la catégorie » -> nomCategorie
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomCategorie"));

        // « Description » -> descriptionCategorie
        colDescription.setCellValueFactory(new PropertyValueFactory<>("descriptionCategorie"));

        // Réduire le texte si la description est trop longue (> 50 caractères)
        colDescription.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if (item.length() > 50) {
                        setText(item.substring(0, 50) + "...");
                    } else {
                        setText(item);
                    }
                }
            }
        });

        // Créer les boutons "Modifier" et "Supprimer"
        colDetails.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");

            {
                btnModifier.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                btnSupprimer.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

                // Bouton Modifier
                btnModifier.setOnAction(event -> {
                    CategorieCours selected = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/workshop/ModifierCategorieCours.fxml"));
                        Parent root = loader.load();

                        // Récupérer le contrôleur
                        ModifierCategorieCours controller = loader.getController();
                        controller.setCategorieCours(selected);

                        Stage stage = (Stage) getTableView().getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                // Bouton Supprimer (logique à implémenter)
                btnSupprimer.setOnAction(event -> {
                    CategorieCours selected = getTableView().getItems().get(getIndex());
                    System.out.println("Supprimer : " + selected.getNomCategorie());
                    // TODO : implémenter la logique de suppression (service.delete(...) etc.)
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox container = new HBox(10, btnModifier, btnSupprimer);
                    setGraphic(container);
                }
            }
        });

        // Charger la liste des catégories
        loadCategories();

        // Bouton "+ Ajouter catégorie"
        btnAjouter.setOnAction(event -> handleAjouterCategorie());
    }

    private void loadCategories() {
        try {
            List<CategorieCours> categories = service.displayList();
            categorieTableView.getItems().setAll(categories);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleAjouterCategorie() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/workshop/AjoutCategorieCours.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnAjouter.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
