package controllers.workshop;

import entities.CategorieCours;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

        colNom.setCellValueFactory(new PropertyValueFactory<>("descriptionCategorie"));

        colDescription.setCellValueFactory(new PropertyValueFactory<>("nomCategorie"));


        colDetails.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");

            {
                btnModifier.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                btnSupprimer.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

                btnModifier.setOnAction(event -> {
                    CategorieCours selected = getTableView().getItems().get(getIndex());
                    System.out.println("Modifier : " + selected.getNomCategorie());

                });

                btnSupprimer.setOnAction(event -> {
                    CategorieCours selected = getTableView().getItems().get(getIndex());
                    System.out.println("Supprimer : " + selected.getNomCategorie());

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
