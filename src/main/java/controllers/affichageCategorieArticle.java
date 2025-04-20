package controllers;

import entities.CategorieArticle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


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
    private TableColumn<CategorieArticle, Integer> nombreArticlesColumn;

    @FXML
    private Button addCategorieBtn;

    @FXML
    private ComboBox<String> categorieFilterCombo;

    private ObservableList<CategorieArticle> allCategories;

    private FilteredList<CategorieArticle> filteredCategories;
    private SortedList<CategorieArticle> sortedCategories;

    @FXML
    public void initialize() {
        categorieTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image_categorie"));

        imageColumn.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(80);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(false);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setStyle("-fx-alignment: CENTER;");
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);

                if (empty || imagePath == null || imagePath.trim().isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        String imageUrl = "file:/C:/Users/Admin/Desktop/PI_RecyConnect_TechSquad/public/uploads/photo_dir/" + imagePath;
                        imageView.setImage(new javafx.scene.image.Image(imageUrl));
                        setGraphic(imageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                        setGraphic(null);
                    }
                }
            }
        });

        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom_categorie"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description_categorie"));
        nombreArticlesColumn.setCellValueFactory(param -> {
            try {
                int count = new CateArtService().countArticlesByCategorie(param.getValue().getId());
                return new javafx.beans.property.SimpleIntegerProperty(count).asObject();
            } catch (SQLException e) {
                e.printStackTrace();
                return new javafx.beans.property.SimpleIntegerProperty(0).asObject();
            }
        });

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

                        // Recharger la liste depuis la base
                        List<CategorieArticle> updatedList = service.displayList();
                        allCategories.setAll(updatedList); // ✅ met à jour sans casser la liaison

                        // Mettre à jour le ComboBox si nécessaire
                        updateComboBox(updatedList);

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
            allCategories = FXCollections.observableArrayList(categories);

            // Lier les listes
            filteredCategories = new FilteredList<>(allCategories, p -> true);
            sortedCategories = new SortedList<>(filteredCategories);
            sortedCategories.comparatorProperty().bind(categorieTable.comparatorProperty());
            categorieTable.setItems(sortedCategories);

            // Remplir ComboBox
            categorieFilterCombo.getItems().clear();
            categorieFilterCombo.getItems().add("Toutes les catégories");
            for (CategorieArticle cat : categories) {
                categorieFilterCombo.getItems().add(cat.getNom_categorie());
            }
            categorieFilterCombo.getSelectionModel().selectFirst();

            // Listener
            categorieFilterCombo.setOnAction(e -> applyFilter());

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

    private void applyFilter() {
        String selectedNom = categorieFilterCombo.getValue();

        if (selectedNom == null || selectedNom.equals("Toutes les catégories")) {
            filteredCategories.setPredicate(cat -> true);
        } else {
            filteredCategories.setPredicate(cat ->
                    cat.getNom_categorie().equalsIgnoreCase(selectedNom)
            );
        }
    }

    private void updateComboBox(List<CategorieArticle> categories) {
        String previousSelection = categorieFilterCombo.getValue();

        categorieFilterCombo.getItems().clear();
        categorieFilterCombo.getItems().add("Toutes les catégories");
        for (CategorieArticle cat : categories) {
            categorieFilterCombo.getItems().add(cat.getNom_categorie());
        }

        // Réappliquer la sélection
        if (previousSelection != null && categorieFilterCombo.getItems().contains(previousSelection)) {
            categorieFilterCombo.setValue(previousSelection);
        } else {
            categorieFilterCombo.getSelectionModel().selectFirst();
        }

        applyFilter(); // ✅ réapplique le filtre après suppression
    }


}
