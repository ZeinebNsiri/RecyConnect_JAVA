package controllers;

import entities.Post;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.PostService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EditPostController {

    @FXML private TextArea contentTextArea;
    @FXML private FlowPane existingImagesPane;
    @FXML private Button addImageButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Post post;
    private List<String> existingMediaPaths = new ArrayList<>();
    private List<File> newImageFiles = new ArrayList<>();
    private PostService postService = new PostService();

    public void setPost(Post post) {
        this.post = post;
        contentTextArea.setText(post.getContenu());

        List<String> mediaUrls = postService.getMediaForPost(post.getId());
        existingMediaPaths.clear();
        existingMediaPaths.addAll(mediaUrls);

        for (String url : mediaUrls) {
            ImageView imageView = new ImageView(new Image("file:" + url));
            imageView.setFitWidth(80);
            imageView.setFitHeight(80);
            imageView.setPreserveRatio(true);

            Button removeBtn = new Button("✖");
            removeBtn.setOnAction(e -> {
                existingImagesPane.getChildren().remove(imageView.getParent());
                existingMediaPaths.remove(url); // retirer l'image supprimée
            });

            VBox imageBox = new VBox(imageView, removeBtn);
            existingImagesPane.getChildren().add(imageBox);
        }
    }

    @FXML
    private void initialize() {
        addImageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(addImageButton.getScene().getWindow());
            if (file != null) {
                ImageView imageView = new ImageView(new Image(file.toURI().toString()));
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
                imageView.setPreserveRatio(true);

                VBox imageBox = new VBox(imageView);
                existingImagesPane.getChildren().add(imageBox);

                newImageFiles.add(file); // à copier plus tard
            }
        });

        saveButton.setOnAction(e -> {
            post.setContenu(contentTextArea.getText());

            List<String> finalMediaPaths = new ArrayList<>(existingMediaPaths);

            for (File file : newImageFiles) {
                // copie locale vers /media/post/... + obtenir le path absolu
                String destinationPath = "C:\\Users\\samar\\Desktop\\PI_RecyConnect_TechSquad\\public\\Posts\\uploads\\" + file.getName();
                try {
                    Files.copy(file.toPath(), Path.of(destinationPath), StandardCopyOption.REPLACE_EXISTING);
                    finalMediaPaths.add(destinationPath);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

            try {
                postService.update(post, finalMediaPaths);
                closeWindow();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        cancelButton.setOnAction(e -> closeWindow());
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
