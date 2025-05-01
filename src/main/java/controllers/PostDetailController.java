package controllers;

import entities.utilisateur;
import enums.PostTag;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import entities.Commentaire;
import entities.Post;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.CommentaireService;
import services.PostService;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;


import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PostDetailController {

    private final PostService postService = new PostService();
    private final CommentaireService commentaireService = new CommentaireService();

    @FXML
    private Label auteurPostLabel;

    @FXML
    private ImageView postProfileImage;

    @FXML
    private HBox ajoutCommentaireBox;

    @FXML
    private TextField champCommentaire;

    @FXML
    private Label datePostLabel;

    @FXML
    private Label contenuPostText;

    @FXML
    private FlowPane hashtagsPane;

    @FXML
    private VBox commentairesVBox;

    @FXML
    private TextField nouveauCommentaireField;

    private Post post;

    // Setter appelé depuis ForumController
    public void setPost(Post post) {
        this.post = post;
        afficherDetailsPost();
        chargerCommentaires();
    }

    private void afficherDetailsPost() {

        utilisateur user = postService.getUserPById(post.getUser_p_id());
        auteurPostLabel.setText(user.getPrenom());
        try {
            Image image = new Image(getClass().getResourceAsStream("/images/avatar-15.png")); // chemin relatif dans ton projet
            postProfileImage.setImage(image);
        } catch (Exception e) {
            e.printStackTrace();
            // Si une erreur se produit, mettre une image par défaut aussi
            postProfileImage.setImage(new Image(getClass().getResourceAsStream("/images/avatar-15.png")));
        }



        datePostLabel.setText(post.getDate_publication().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        contenuPostText.setText(post.getContenu());
        hashtagsPane.getChildren().clear();

        List<PostTag> tags = post.getTags();
        if (tags != null) {
            for (PostTag tag : tags) {
                Label tagLabel = new Label("#" + tag.getLabel());
                tagLabel.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 8;");
                hashtagsPane.getChildren().add(tagLabel);
            }
        }
    }

    private void chargerCommentaires() {
        commentairesVBox.getChildren().clear();

        // Appelle ta méthode pour récupérer les commentaires du post depuis la base
        List<Commentaire> commentaires = commentaireService.getCommentairesByPost(post.getId());

        for (Commentaire commentaire : commentaires) {
            if (commentaire.getParentId() == null) {
                VBox blocCommentaire = creerBlocCommentaire(commentaire, 0);
                commentairesVBox.getChildren().add(blocCommentaire);
            }
        }
    }

    private VBox creerBlocCommentaire(Commentaire commentaire, int indentLevel) {
        VBox container = new VBox();
        container.setSpacing(5);
        container.setPadding(new Insets(5, 5, 5, indentLevel * 30));

        utilisateur userC = commentaireService.getUserComById(commentaire.getUserComId());

        ImageView imageView = new ImageView();
        try {
            Image image = new Image(getClass().getResourceAsStream("/images/avatar-15.png")); // Chemin ou URL
            imageView.setImage(image);
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/avatar-15.png"))); // image par défaut si erreur
        }
        imageView.setFitHeight(40);
        imageView.setFitWidth(40);
        imageView.setPreserveRatio(true);
        imageView.setClip(new Circle(20, 20, 20));

        Label auteurLabel = new Label(userC.getPrenom() + " " + userC.getNom_user());
        auteurLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        Label dateLabel = new Label(commentaire.getDateCom().format(DateTimeFormatter.ofPattern("MMMM d, yyyy, h:mm a")));
        dateLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 11px;");

        Label contenuLabel = new Label(commentaire.getContenuCom());
        contenuLabel.setWrapText(true);
        contenuLabel.setStyle("-fx-font-size: 13px;");

        VBox texteBox = new VBox(auteurLabel, contenuLabel, dateLabel);
        texteBox.setSpacing(3);
        texteBox.setPadding(new Insets(5));

        HBox bloc = new HBox(imageView, texteBox);
        bloc.setSpacing(10);
        bloc.setStyle("-fx-background-color: #ffffff; -fx-padding: 10; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");

        container.getChildren().add(bloc);

        // Chargement récursif des replies
        List<Commentaire> replies = commentaireService.getReplies(commentaire.getId());
        for (Commentaire reply : replies) {
            VBox blocReply = creerBlocCommentaire(reply, indentLevel + 1);
            container.getChildren().add(blocReply);
        }

        return container;
    }



    @FXML
    private void afficherChampCommentaire() {
        ajoutCommentaireBox.setVisible(true);
        ajoutCommentaireBox.setManaged(true);
        champCommentaire.requestFocus();
    }

    @FXML
    private void envoyerCommentaire() {
        String contenu = champCommentaire.getText().trim();

        if (contenu.isEmpty()) {
            // Affiche une alerte ou un message d'erreur à l'utilisateur
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champ vide");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez saisir un commentaire avant d'envoyer.");
            alert.showAndWait();
            return; // Stoppe l'exécution ici
        }

        // Si non vide, on envoie le commentaire
        Commentaire commentaire = new Commentaire();
        commentaire.setContenuCom(contenu);
        commentaire.setUserComId(2); // À remplacer par l’ID de l'utilisateur connecté
        commentaire.setDateCom(java.time.LocalDateTime.now());
        commentaire.setPostComId(post.getId());
        commentaire.setParentId(null);

        commentaireService.ajouter(commentaire);

        champCommentaire.clear();
        ajoutCommentaireBox.setVisible(false);
        ajoutCommentaireBox.setManaged(false);
        chargerCommentaires();
    }



}
