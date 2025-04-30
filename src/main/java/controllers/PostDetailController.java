package controllers;

import entities.utilisateur;
import enums.PostTag;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import entities.Commentaire;
import entities.Post;
import services.CommentaireService;
import services.PostService;


import java.time.format.DateTimeFormatter;
import java.util.List;

public class PostDetailController {

    private final PostService postService = new PostService();
    private final CommentaireService commentaireService = new CommentaireService();

    @FXML
    private Label auteurPostLabel;

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

        Label auteurLabel = new Label(userC.getPrenom() + userC.getNom_user());
        Label dateLabel = new Label(commentaire.getDateCom().format(DateTimeFormatter.ofPattern("MMMM d, yyyy, h:mm a")));
        Label contenuLabel = new Label(commentaire.getContenuCom());

        VBox bloc = new VBox(auteurLabel, contenuLabel, dateLabel);
        bloc.setStyle("-fx-background-color: #f7f9fa; -fx-padding: 10; -fx-background-radius: 5;");
        container.getChildren().add(bloc);

        // Chargement récursif des replies
        List<Commentaire> replies = commentaireService.getReplies(commentaire.getId());
        for (Commentaire reply : replies) {
            VBox blocReply = creerBlocCommentaire(reply, indentLevel + 1);
            container.getChildren().add(blocReply);
        }

        return container;
    }

//    @FXML
//    private void ajouterCommentaire() {
//        String contenu = nouveauCommentaireField.getText().trim();
//        if (!contenu.isEmpty()) {
//            Commentaire commentaire = new Commentaire();
//            commentaire.setAuteur("UtilisateurTest"); // À adapter selon le contexte connecté
//            commentaire.setContenu(contenu);
//            commentaire.setPost(post);
//            commentaire.setDate(java.time.LocalDateTime.now());
//
//            CommentaireService.ajouterCommentaire(commentaire);
//            nouveauCommentaireField.clear();
//            chargerCommentaires();
//        }
//    }
}
