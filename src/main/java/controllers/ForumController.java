package controllers;

import entities.Post;
import entities.utilisateur;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import services.PostService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ForumController {

    @FXML
    private VBox postList;

    private PostService postService = new PostService();


    @FXML
    public void initialize() {
        loadPosts();
    }

    public void loadPosts() {
        try {
            List<Post> posts = postService.displayList(); // Récupère la liste des posts

            for (Post post : posts) {
                // Créer un VBox pour chaque post
                VBox postCard = new VBox();
                postCard.setSpacing(10);
                postCard.getStyleClass().add("post-card");

                // Créer un HBox pour le contenu du post
                HBox postContent = new HBox();
                postContent.setSpacing(15);

                // Récupérer l'utilisateur associé au post
                utilisateur user = postService.getUserPById(post.getUser_p_id()); // Utiliser la méthode pour récupérer l'utilisateur
                VBox userInfo = new VBox();

                // Afficher le prénom et la photo de profil
                Label usernameLabel = new Label(user.getPrenom()); // Utiliser le prénom de l'utilisateur
                Label timestampLabel = new Label(post.getDate_publication().toString());
                userInfo.getChildren().addAll(usernameLabel, timestampLabel);

                // Ajouter la photo de profil
                ImageView profileImageView = new ImageView();
                profileImageView.setFitWidth(40);
                profileImageView.setFitHeight(40);
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/avatar-15.png"));
                profileImageView.setImage(defaultImage);


                VBox content = new VBox();
                Label postText = new Label(post.getContenu());
                content.getChildren().add(postText);

                // Ajouter les images associées au post
                VBox mediaContainer = new VBox();
                mediaContainer.setSpacing(5);

                List<String> mediaUrls = postService.getMediaForPost(post.getId());
                for (String mediaUrl : mediaUrls) {
                    ImageView imageView = new ImageView();
                    imageView.setFitWidth(80);
                    imageView.setFitHeight(80);
                    imageView.setPreserveRatio(true);
                    Image image = new Image("file:" + mediaUrl); // Charger l'image depuis le chemin
                    imageView.setImage(image);
                    mediaContainer.getChildren().add(imageView);
                }

                // Ajouter le compteur de likes et commentaires
                HBox stats = new HBox();
                stats.setSpacing(15);
                Label likesLabel = new Label("♥ 0");
                Label commentsLabel = new Label("💬 0");
                stats.getChildren().addAll(likesLabel, commentsLabel);

                // Ajouter les éléments au HBox principal
                postContent.getChildren().addAll(userInfo, content);
                postCard.getChildren().addAll(postContent, mediaContainer, new Separator(), stats);
                postList.getChildren().add(postCard);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreerPublication(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddPost.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
