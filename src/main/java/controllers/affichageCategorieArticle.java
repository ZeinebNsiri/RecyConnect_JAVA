package controllers;

import entities.CategorieArticle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import services.CateArtService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class affichageCategorieArticle {

    @FXML
    private TableView<CategorieArticle> categorieTable;

    @FXML
    private TableColumn<CategorieArticle, Integer> idColumn;

    @FXML
    private TableColumn<CategorieArticle, String> imageColumn;

    @FXML
    private TableColumn<CategorieArticle, String> nomColumn;

    @FXML
    private TableColumn<CategorieArticle, String> descriptionColumn;

    @FXML
    private TableColumn<CategorieArticle, Void> actionsColumn;

    @FXML
    private Button addCategorieBtn;

    @FXML
    public void initialize() {
        categorieTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image_categorie"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom_categorie"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description_categorie"));

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("✏ Modifier");
            private final Button deleteButton = new Button("🗑 Supprimer");
            private final HBox pane = new HBox(10, editButton, deleteButton);

            {
                pane.setStyle("-fx-alignment: center;");
                editButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

                deleteButton.setOnAction(event -> {
                    CategorieArticle cat = getTableView().getItems().get(getIndex());
                    try {
                        CateArtService service = new CateArtService();
                        service.delete(cat);
                        categorieTable.setItems(FXCollections.observableArrayList(service.displayList()));
                    } catch (SQLException e) {
                        e.printStackTrace();
                        showAlert("Erreur lors de la suppression !");
                    }
                });

                editButton.setOnAction(event -> {
                    CategorieArticle cat = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseAdmin.fxml"));
                        Parent root = loader.load();

                        BaseAdminController controller = loader.getController();
                        controller.showAjoutCategorieViewWithData(cat);

                        addCategorieBtn.getScene().setRoot(root);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });

        loadCategories();
    }

    private void loadCategories() {
        try {
            CateArtService service = new CateArtService();
            List<CategorieArticle> categories = service.displayList();
            categorieTable.setItems(FXCollections.observableArrayList(categories));
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement des catégories !");
        }
    }

    @FXML
    private void ajouterCategorieAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseAdmin.fxml"));
            Parent root = loader.load();

            BaseAdminController controller = loader.getController();
            controller.showAjoutCategorieView();

            addCategorieBtn.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture du formulaire d'ajout !");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }
}
