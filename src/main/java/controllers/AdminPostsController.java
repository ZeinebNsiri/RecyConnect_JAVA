package controllers;

import entities.Post;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import services.PostService;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminPostsController {

    @FXML
    private TableView<Post> postsTable;

    private PostService postService = new PostService();
    private ObservableList<Post> postList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadPosts();
    }

    private void setupTableColumns() {
        TableColumn<Post, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Post, Integer> authorCol = new TableColumn<>("Auteur");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("user_p_id"));

        TableColumn<Post, String> contentCol = new TableColumn<>("Contenu");
        contentCol.setCellValueFactory(new PropertyValueFactory<>("contenu"));

        TableColumn<Post, String> dateCol = new TableColumn<>("Créé le");
        dateCol.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getDate_publication().format(formatter)
            );
        });

        TableColumn<Post, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(cellData -> {
            boolean status = cellData.getValue().isStatus_post();
            return new javafx.beans.property.SimpleStringProperty(status ? "Approuvé" : "Rejeté");
        });

        TableColumn<Post, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("Approuver");
            private final Button rejectBtn = new Button("Rejeter");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox actionButtons = new HBox(5);

            {
                approveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                rejectBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");

                approveBtn.setOnAction(e -> {
                    Post post = getTableView().getItems().get(getIndex());
                    try {
                        postService.approuverPost(post);
                        post.setStatus_post(true);
                        postsTable.refresh();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });

                rejectBtn.setOnAction(e -> {
                    Post post = getTableView().getItems().get(getIndex());
                    try {
                        postService.rejeterPost(post);
                        post.setStatus_post(false);
                        postsTable.refresh();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });

                deleteBtn.setOnAction(e -> {
                    Post post = getTableView().getItems().get(getIndex());
                    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmation.setTitle("Confirmation de suppression");
                    confirmation.setHeaderText("Voulez-vous vraiment supprimer ce post ?");
                    confirmation.setContentText(post.getContenu());

                    confirmation.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                postService.delete(post);
                                postList.remove(post);
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
                if (empty) {
                    setGraphic(null);
                } else {
                    Post post = getTableView().getItems().get(getIndex());
                    actionButtons.getChildren().clear();

                    if (post.isStatus_post()) {
                        actionButtons.getChildren().addAll(rejectBtn, deleteBtn);
                    } else {
                        actionButtons.getChildren().addAll(approveBtn, deleteBtn);
                    }

                    setGraphic(actionButtons);
                }
            }
        });

        postsTable.getColumns().setAll(idCol, authorCol, contentCol, dateCol, statusCol, actionsCol);
    }

    private void loadPosts() {
        try {
            List<Post> posts = postService.displayList();
            postList.setAll(posts);
            postsTable.setItems(postList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
