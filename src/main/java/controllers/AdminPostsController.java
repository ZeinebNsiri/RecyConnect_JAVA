package controllers;

import entities.Post;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import services.PostService;

import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AdminPostsController {

    @FXML
    private TableView<Post> postsTable;

    @FXML
    private PieChart statusPieChart;

    @FXML
    private LineChart<String, Number> postsLineChart;

    @FXML
    private CategoryAxis monthsAxis;

    @FXML
    private NumberAxis countAxis;

    private PostService postService = new PostService();
    private ObservableList<Post> postList = FXCollections.observableArrayList();

    private XYChart.Series<String, Number> postCountSeries = new XYChart.Series<>();

    @FXML
    public void initialize() {
        setupTableColumns();
        postsLineChart.getData().add(postCountSeries);
        postCountSeries.setName("Nombre de posts par mois");
        loadPosts();
        updatePostStatusChart();
        loadPostStatistics();
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
            return new SimpleStringProperty(cellData.getValue().getDate_publication().format(formatter));
        });

        TableColumn<Post, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(cellData -> {
            boolean status = cellData.getValue().isStatus_post();
            return new SimpleStringProperty(status ? "Approuvé" : "Rejeté");
        });

        TableColumn<Post, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("Approuver");
            private final Button rejectBtn = new Button("Rejeter");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox actionButtons = new HBox(5);

            {
                approveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                rejectBtn.setStyle("-fx-background-color: #e48f59; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #a32c2b; -fx-text-fill: white;");

                approveBtn.setOnAction(e -> {
                    Post post = getTableView().getItems().get(getIndex());
                    try {
                        postService.approuverPost(post);
                        post.setStatus_post(true);
                        postsTable.refresh();
                        updatePostStatusChart();
                        loadPostStatistics();
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
                        updatePostStatusChart();
                        loadPostStatistics();
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
                                updatePostStatusChart();
                                loadPostStatistics();
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

    private void updatePostStatusChart() {
        int approvedCount = 0;
        int rejectedCount = 0;

        for (Post post : postList) {
            if (post.isStatus_post()) {
                approvedCount++;
            } else {
                rejectedCount++;
            }
        }

        PieChart.Data approvedData = new PieChart.Data("Approuvé", approvedCount);
        PieChart.Data rejectedData = new PieChart.Data("Rejeté", rejectedCount);

        statusPieChart.setData(FXCollections.observableArrayList(approvedData, rejectedData));
    }

    private void loadPostStatistics() {
        Map<YearMonth, Integer> postCountByMonth = postService.getPostCountByMonth();

        postCountSeries.getData().clear();  // Clean old data

        postCountByMonth.forEach((month, count) -> {
            XYChart.Data<String, Number> data = new XYChart.Data<>(month.toString(), count);
            postCountSeries.getData().add(data);

            // Animation (fade in)
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-background-color: #0077be;");
                    newNode.setOpacity(0);
                    FadeTransition ft = new FadeTransition(Duration.millis(800), newNode);
                    ft.setFromValue(0);
                    ft.setToValue(1);
                    ft.play();
                }
            });
        });

        monthsAxis.setLabel("Mois");
        countAxis.setLabel("Nombre de posts");
    }
}
