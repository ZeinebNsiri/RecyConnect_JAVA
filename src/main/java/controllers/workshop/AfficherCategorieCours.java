package controllers.workshop;

import controllers.BaseAdminController;
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
                        // 1) Recharger le shell (header + sidebar)
                        FXMLLoader shellLoader = new FXMLLoader(
                                getClass().getResource("/BaseAdmin.fxml")
                        );
                        Parent shellRoot = shellLoader.load();
                        BaseAdminController base = shellLoader.getController();

                        // 2) Appeler la méthode de pré‑remplissage
                        base.showModifierCategorieViewWithData(selected);

                        // 3) Remplacer la racine de la scène courante
                        Stage stage = (Stage) getTableView().getScene().getWindow();
                        stage.setScene(new Scene(shellRoot, 1000, 600));
                        stage.setTitle("Modifier Catégorie");
                        stage.setResizable(false);
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
            // 1) Load the shell (BaseAdmin.fxml)
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/BaseAdmin.fxml")
            );
            Parent root = loader.load();

            // 2) Tell its controller to show the "Ajouter" form in the contentPane
            BaseAdminController shell = loader.getController();
            shell.showAjoutCategorieView();   // this does loadView("/workshop/AjoutCategorieCours.fxml")

            // 3) Swap the entire scene on the existing stage
            Stage stage = (Stage) btnAjouter.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 600));
            stage.setTitle("Gestion des Catégories de Cours");
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
