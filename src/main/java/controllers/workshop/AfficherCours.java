package controllers.workshop;

import controllers.BaseAdminController;
import entities.CategorieCours;
import entities.Cours;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import services.CoursService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherCours {

    @FXML private TableView<Cours> coursTableView;
    @FXML private TableColumn<Cours, Integer> colId;
    @FXML private TableColumn<Cours, String> colTitre;
    @FXML private TableColumn<Cours, String> colDescription;
    @FXML private TableColumn<Cours, String> colVideo;
    @FXML private TableColumn<Cours, String> colImage;
    @FXML private TableColumn<Cours, CategorieCours> colCategorie;
    @FXML private TableColumn<Cours, Void> colDetails;
    @FXML private Button btnAjouter;
    @FXML private ComboBox<String> categorieFilterComboBox;

    private final CoursService coursService = new CoursService();
    private List<Cours> allCourses;

    @FXML
    private void initialize() {
        setupColumns();
        try {
            allCourses = coursService.displayList();
            populateCategories();
            coursTableView.getItems().setAll(allCourses);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        btnAjouter.setOnAction(event -> handleAjouterCours());

        categorieFilterComboBox.setOnAction(e -> {
            String selectedCat = categorieFilterComboBox.getValue();
            if (selectedCat == null || selectedCat.equals("Tous")) {
                coursTableView.getItems().setAll(allCourses);
            } else {
                coursTableView.getItems().setAll(
                        allCourses.stream()
                                .filter(c -> c.getCategorieCours().getNomCategorie().equals(selectedCat))
                                .collect(Collectors.toList()));
            }
        });
    }

    private void populateCategories() {
        categorieFilterComboBox.getItems().clear();
        categorieFilterComboBox.getItems().add("Tous");
        allCourses.stream()
                .map(c -> c.getCategorieCours().getNomCategorie())
                .distinct()
                .forEach(categorieFilterComboBox.getItems()::add);
        categorieFilterComboBox.setValue("Tous");
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titreCours"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("descriptionCours"));
        colVideo.setCellValueFactory(new PropertyValueFactory<>("video"));
        colImage.setCellValueFactory(new PropertyValueFactory<>("imageCours"));

        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorieCours"));
        colCategorie.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(CategorieCours item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNomCategorie());
            }
        });

        colDescription.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item.length() > 50 ? item.substring(0, 50) + "..." : item));
            }
        });

        colVideo.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : getTableView().getItems().get(getIndex()).getVideoView());
            }
        });

        colImage.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : getTableView().getItems().get(getIndex()).getImageView());
            }
        });

        colDetails.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");

            {
                btnModifier.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                btnSupprimer.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

                btnModifier.setOnAction(event -> {
                    Cours selected = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseAdmin.fxml"));
                        Parent root = loader.load();
                        BaseAdminController shell = loader.getController();
                        shell.showModifierCoursViewWithData(selected);
                        Stage stage = (Stage) getTableView().getScene().getWindow();
                        stage.setScene(new Scene(root, 1000, 600));
                        stage.setTitle("Modifier Cours");
                        stage.setResizable(false);
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                btnSupprimer.setOnAction(event -> {
                    Cours selected = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                            "Voulez-vous vraiment supprimer le cours \"" + selected.getTitreCours() + "\" ?",
                            ButtonType.OK, ButtonType.CANCEL);
                    alert.setTitle("Confirmation de suppression");
                    alert.setHeaderText("Supprimer le cours ?");
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                coursService.delete(selected);
                                allCourses.remove(selected);
                                coursTableView.getItems().remove(selected);
                                populateCategories();
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
                if (empty) setGraphic(null);
                else setGraphic(new HBox(10, btnModifier, btnSupprimer));
            }
        });
    }

    private void handleAjouterCours() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseAdmin.fxml"));
            Parent root = loader.load();
            BaseAdminController shell = loader.getController();
            shell.showAjouterCoursView();
            Stage stage = (Stage) btnAjouter.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 600));
            stage.setTitle("Ajouter Cours");
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
