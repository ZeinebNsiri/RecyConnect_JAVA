package controllers.workshop;

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

public class AfficherCours {

    @FXML
    private TableView<Cours> coursTableView;

    @FXML
    private TableColumn<Cours, Integer> colId;

    @FXML
    private TableColumn<Cours, String> colTitre;

    @FXML
    private TableColumn<Cours, String> colDescription;

    @FXML
    private TableColumn<Cours, String> colVideo;

    @FXML
    private TableColumn<Cours, String> colImage;


    @FXML
    private TableColumn<Cours, CategorieCours> colCategorie;

    @FXML
    private TableColumn<Cours, Void> colDetails;

    @FXML
    private Button btnAjouter;

    private final CoursService coursService = new CoursService();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titreCours"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("descriptionCours"));
        colVideo.setCellValueFactory(new PropertyValueFactory<>("video"));
        colImage.setCellValueFactory(new PropertyValueFactory<>("imageCours"));


        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorieCours"));
        colCategorie.setCellFactory(column -> new TableCell<Cours, CategorieCours>() {
            @Override
            protected void updateItem(CategorieCours item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNomCategorie());
                }
            }
        });


        colDescription.setCellFactory(column -> new TableCell<Cours, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.length() > 50 ? item.substring(0, 50) + "..." : item);
                }
            }
        });


        colVideo.setCellFactory(column -> new TableCell<Cours, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Cours cours = getTableView().getItems().get(getIndex());
                    setGraphic(cours.getVideoView());
                }
            }
        });


        colImage.setCellFactory(column -> new TableCell<Cours, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Cours cours = getTableView().getItems().get(getIndex());
                    setGraphic(cours.getImageView());
                }
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
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/workshop/ModifierCours.fxml"));
                        Parent root = loader.load();

                        // Récupérer le contrôleur de ModifierCours
                        ModifierCours controller = loader.getController();
                        controller.setCours(selected);

                        Stage stage = (Stage) getTableView().getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });


                btnSupprimer.setOnAction(event -> {
                    Cours selected = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation de suppression");
                    alert.setHeaderText("Supprimer le cours ?");
                    alert.setContentText("Voulez-vous vraiment supprimer le cours \"" + selected.getTitreCours() + "\" ?");
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                coursService.delete(selected);
                                loadCours();
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

        loadCours();
        btnAjouter.setOnAction(event -> handleAjouterCours());
    }

    private void loadCours() {
        try {
            List<Cours> coursList = coursService.displayList();
            coursTableView.getItems().setAll(coursList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleAjouterCours() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/workshop/AjouterCours.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnAjouter.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
