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

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));


        colNom.setCellValueFactory(new PropertyValueFactory<>("nomCategorie"));


        colDescription.setCellValueFactory(new PropertyValueFactory<>("descriptionCategorie"));


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


        colDetails.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");

            {
                // Style des boutons
                btnModifier.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                btnSupprimer.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");


                btnModifier.setOnAction(event -> {
                    CategorieCours selected = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/workshop/ModifierCategorieCours.fxml"));
                        Parent root = loader.load();
                        ModifierCategorieCours controller = loader.getController();
                        controller.setCategorieCours(selected);
                        Stage stage = (Stage) getTableView().getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });


                btnSupprimer.setOnAction(event -> {
                    CategorieCours selected = getTableView().getItems().get(getIndex());
                    Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmationAlert.setTitle("Confirmation de suppression");
                    confirmationAlert.setHeaderText("Supprimer la catégorie ?");
                    confirmationAlert.setContentText("Voulez-vous vraiment supprimer la catégorie \"" + selected.getNomCategorie() + "\" ?");
                    ButtonType okButton = ButtonType.OK;
                    ButtonType cancelButton = ButtonType.CANCEL;
                    confirmationAlert.getButtonTypes().setAll(okButton, cancelButton);
                    confirmationAlert.showAndWait().ifPresent(response -> {
                        if (response == okButton) {
                            try {
                                service.delete(selected);
                                loadCategories();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    });
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


        loadCategories();


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

 
    @FXML
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
