package controllers;

import entities.Post;
import entities.utilisateur;
import enums.PostTag;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import services.PostService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AddPostController {

    @FXML
    private TextArea postContent;

    @FXML
    private Button publishButton;

    @FXML
    private Button backButton;

    @FXML
    private Button chooseFileButton;

    @FXML
    private Label fileChosenLabel;

    @FXML
    private FlowPane tagsPane;

    private List<String> allTags = List.of(
            "Durabilité", "Réutilisation", "RecyclagePlastique", "RecyclagePapier",
            "RecyclageMétal", "RecyclageVerre", "DIYRecyclage", "Question",
            "Réclamation", "Initiative"
    );
    private utilisateur user = utils.Session.getInstance().getCurrentUser();

    private List<String> selectedTags = new ArrayList<>();

    private List<String> selectedImagePaths = new ArrayList<>();
    private final PostService postService = new PostService();

    @FXML
    public void initialize() {
        chooseFileButton.setOnAction(e -> handleChooseFile());
        publishButton.setOnAction(e -> handlePublish());
        backButton.setOnAction(e -> handleBack());
        for (String tag : allTags) {
            ToggleButton tagButton = new ToggleButton("#" + tag);
            tagButton.getStyleClass().add("tag-button");
            tagButton.setOnAction(e -> {
                if (tagButton.isSelected()) {
                    selectedTags.add(tag);
                    tagButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");
                } else {
                    selectedTags.remove(tag);
                    tagButton.setStyle(""); // reset style
                }
            });
            tagsPane.getChildren().add(tagButton);
        }
    }

    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseUser.fxml"));
            Parent root = loader.load();
            BaseUserController baseUserController = loader.getController();
            baseUserController.showPostsView();

            publishButton.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de revenir au forum.");
            alert.showAndWait();
        }
    }

    private void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());

        if (files != null) {
            selectedImagePaths.clear();
            for (File file : files) {
                selectedImagePaths.add(file.getAbsolutePath());
            }
            fileChosenLabel.setText(selectedImagePaths.size() + " fichier(s) sélectionné(s)");
        } else {
            fileChosenLabel.setText("Aucun fichier sélectionné");
        }
    }

    private void handlePublish() {
        String content = postContent.getText().trim();

        if (content.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de saisie");
            alert.setHeaderText(null);
            alert.setContentText("Le contenu du post est obligatoire.");
            alert.showAndWait();
            return;
        }
        postService.analyzePostContent(content);

        try {
            Post newPost = new Post();
            newPost.setUser_p_id(user.getId()); //ba3d tekhou el user connecté
            newPost.setContenu(content);
            newPost.setDate_publication(LocalDateTime.now());
            newPost.setNbr_jaime(0);
            newPost.setStatus_post(false); // Post non approuvé par défaut
            List<PostTag> postTags = selectedTags.stream()
                    .map(label -> {
                        try {
                            return PostTag.fromLabel("#" + label); // car le label contient le #
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace(); // ou log si besoin
                            return null;
                        }
                    })
                    .filter(tag -> tag != null)
                    .toList();
            newPost.setTags(postTags); // à adapter selon ton entité Post


            postService.addWithMedia(newPost, selectedImagePaths);
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Succès");
            success.setHeaderText(null);
            success.setContentText("Post ajouté avec succès !");
            success.showAndWait();

            // Optionnel : reset champs
            postContent.clear();
            selectedImagePaths.clear();
            fileChosenLabel.setText("Aucun fichier sélectionné");

        } catch (Exception e) {
            e.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Erreur");
            error.setHeaderText(null);
            error.setContentText("Une erreur s'est produite lors de l'ajout du post.");
            error.showAndWait();
        }
    }
}
