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
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import services.CategorieCoursService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherCategorieCours {

    @FXML private TableView<CategorieCours> categorieTableView;
    @FXML private TableColumn<CategorieCours, Integer> colId;
    @FXML private TableColumn<CategorieCours, String> colNom;
    @FXML private TableColumn<CategorieCours, String> colDescription;
    @FXML private TableColumn<CategorieCours, Void> colDetails;
    @FXML private Button btnAjouter;
    @FXML private HBox paginationContainer;

    private final CategorieCoursService service = new CategorieCoursService();

    private List<CategorieCours> allCategories;
    private static final int ITEMS_PER_PAGE = 5;
    private int currentPage = 1;

    @FXML
    private void initialize() {
        colId.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    int indexOnPage = getIndex();
                    int absoluteIndex = (currentPage - 1) * ITEMS_PER_PAGE + indexOnPage + 1;
                    setText(String.valueOf(absoluteIndex));
                }
            }
        });
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomCategorie"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("descriptionCategorie"));

        colDescription.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.length() > 50 ? item.substring(0, 50) + "..." : item);
            }
        });

        colDetails.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");

            {
                btnModifier.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                btnSupprimer.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

                btnModifier.setOnAction(e -> {
                    CategorieCours selected = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseAdmin.fxml"));
                        Parent root = loader.load();
                        BaseAdminController controller = loader.getController();
                        controller.showModifierCategorieViewWithData(selected);
                        Stage stage = (Stage) getTableView().getScene().getWindow();
                        stage.setScene(new Scene(root, 1000, 600));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

                btnSupprimer.setOnAction(e -> {
                    CategorieCours selected = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer la catégorie \"" + selected.getNomCategorie() + "\" ?", ButtonType.OK, ButtonType.CANCEL);
                    alert.setTitle("Confirmation de suppression");
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                service.delete(selected);
                                loadCategories();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(10, btnModifier, btnSupprimer));
            }
        });

        loadCategories();

        btnAjouter.setOnAction(e -> handleAjouterCategorie());
    }

    private void loadCategories() {
        try {
            allCategories = service.displayList();
            updateTable();
            buildPagination();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateTable() {
        int from = (currentPage - 1) * ITEMS_PER_PAGE;
        int to = Math.min(from + ITEMS_PER_PAGE, allCategories.size());
        categorieTableView.getItems().setAll(allCategories.subList(from, to));
    }

    private void buildPagination() {
        paginationContainer.getChildren().clear();
        int totalPages = (int) Math.ceil((double) allCategories.size() / ITEMS_PER_PAGE);

        Button prev = new Button("« Précédent");
        prev.setDisable(currentPage == 1);
        prev.setStyle("-fx-background-color: #198754; -fx-text-fill: white;");
        prev.setOnAction(e -> {
            currentPage--;
            updateTable();
            buildPagination();
        });

        paginationContainer.getChildren().add(prev);

        for (int i = 1; i <= totalPages; i++) {
            Button page = new Button(String.valueOf(i));
            page.setStyle(i == currentPage
                    ? "-fx-background-color: #198754; -fx-text-fill: white;"
                    : "-fx-background-color: white; -fx-border-color: #198754; -fx-text-fill: #198754;");
            final int index = i;
            page.setOnAction(e -> {
                currentPage = index;
                updateTable();
                buildPagination();
            });
            paginationContainer.getChildren().add(page);
        }

        Button next = new Button("Suivant »");
        next.setDisable(currentPage == totalPages);
        next.setStyle("-fx-background-color: #198754; -fx-text-fill: white;");
        next.setOnAction(e -> {
            currentPage++;
            updateTable();
            buildPagination();
        });

        paginationContainer.getChildren().add(next);
    }

    private void handleAjouterCategorie() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseAdmin.fxml"));
            Parent root = loader.load();
            BaseAdminController shell = loader.getController();
            shell.showAjoutCategorieView();
            Stage stage = (Stage) btnAjouter.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
