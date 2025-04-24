package controllers;

import entities.Post;
import entities.utilisateur;
import enums.PostTag;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.PostService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class ForumController {

    @FXML private ToggleButton recentToggle;
    @FXML private ToggleButton likedToggle;
    @FXML private ToggleButton myPostsToggle;
    @FXML private ToggleButton savedToggle;
    @FXML private VBox postList;
    @FXML private VBox tagListBox;

    private final PostService postService = new PostService();
    private final List<ToggleButton> toggleButtons = new ArrayList<>();
    private final Set<String> selectedTags = new HashSet<>();

    private static final int POSTS_PER_PAGE = 5;
    private int currentPage = 1;
    private List<Post> allPosts = new ArrayList<>();
    private Map<Integer, List<String>> postMediaMap = new HashMap<>();

    @FXML
    public void initialize() {
        toggleButtons.addAll(Arrays.asList(recentToggle, likedToggle, myPostsToggle, savedToggle));

        recentToggle.setOnAction(event -> {
            selectToggle(recentToggle);
            currentPage = 1;
            loadPosts();
        });

        myPostsToggle.setOnAction(event -> {
            selectToggle(myPostsToggle);
            currentPage = 1;
            handleMyPosts();
        });

        likedToggle.setOnAction(event -> {
            selectToggle(likedToggle);
        });

        savedToggle.setOnAction(event -> {
            selectToggle(savedToggle);
        });

        selectToggle(recentToggle);
        loadPosts();
        displayAllTags();
    }

    private void selectToggle(ToggleButton selectedButton) {
        for (ToggleButton btn : toggleButtons) {
            btn.setSelected(btn == selectedButton);
        }
    }

    private void displayAllTags() {
        tagListBox.getChildren().clear();

        // Carte principale pour les tags
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("tag-card"); // Tu peux définir ça dans le CSS
        card.setMaxWidth(250); // Largeur maximale de la carte

        Label heading = new Label("Tags");
        heading.getStyleClass().add("sidebar-heading");

        FlowPane tagContainer = new FlowPane();
        tagContainer.setHgap(8);
        tagContainer.setVgap(8);
        tagContainer.setPadding(new Insets(5));

        for (PostTag tag : PostTag.values()) {
            Label tagLabel = new Label(tag.getLabel());
            tagLabel.getStyleClass().add("tag-badgeL");
            if (selectedTags.contains(tag.getLabel())) {
                tagLabel.getStyleClass().add("active-tag");
            }

            tagLabel.setOnMouseClicked(event -> {
                if (selectedTags.contains(tag.getLabel())) {
                    selectedTags.remove(tag.getLabel());
                    tagLabel.getStyleClass().remove("active-tag");
                } else {
                    selectedTags.add(tag.getLabel());
                    tagLabel.getStyleClass().add("active-tag");
                }
                loadPosts(); // Pour mettre à jour les posts affichés
            });

            tagContainer.getChildren().add(tagLabel);
        }

        card.getChildren().addAll(heading, tagContainer);
        VBox.setMargin(card, new Insets(70, 0, 0, 0));
        tagListBox.getChildren().add(card);
    }


    public void loadPosts() {
        try {
            allPosts = postService.displayList();
            // Filter posts by selected tags if any
            if (!selectedTags.isEmpty()) {
                allPosts = allPosts.stream()
                        .filter(post -> post.getTags().stream()
                                .anyMatch(tag -> selectedTags.contains(tag.getLabel())))
                        .toList();
            }
            allPosts.sort((p1, p2) -> p2.getDate_publication().compareTo(p1.getDate_publication()));
            postMediaMap.clear();

            for (Post post : allPosts) {
                List<String> media = postService.getMediaForPost(post.getId());
                postMediaMap.put(post.getId(), media);
            }

            displayCurrentPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayCurrentPage() {
        postList.getChildren().clear();

        int start = (currentPage - 1) * POSTS_PER_PAGE;
        int end = Math.min(start + POSTS_PER_PAGE, allPosts.size());

        List<Post> pagePosts = allPosts.subList(start, end);
        for (Post post : pagePosts) {
            VBox postCard = createPostCard(post, postMediaMap.get(post.getId()), false);
            postList.getChildren().add(postCard);
        }

        addPaginationControls();
    }

    private void addPaginationControls() {
        HBox paginationBox = new HBox(10);
        paginationBox.setStyle("-fx-alignment: center;");
        paginationBox.setPadding(new Insets(10));

        Button prevButton = new Button("⟨ Précédent");
        Button nextButton = new Button("Suivant ⟩");

        prevButton.setDisable(currentPage == 1);
        nextButton.setDisable(currentPage * POSTS_PER_PAGE >= allPosts.size());

        prevButton.setOnAction(e -> {
            currentPage--;
            displayCurrentPage();
        });

        nextButton.setOnAction(e -> {
            currentPage++;
            displayCurrentPage();
        });

        paginationBox.getChildren().addAll(prevButton, new Label("Page " + currentPage), nextButton);
        postList.getChildren().add(paginationBox);
    }

    private void handleMyPosts() {
        int fakeUserId = 2;
        postList.getChildren().clear();

        Map<Post, List<String>> postsMap = postService.getPostsWithMediaByUser(fakeUserId);
        List<Map.Entry<Post, List<String>>> postEntries = new ArrayList<>(postsMap.entrySet());
        postEntries.sort((e1, e2) -> e2.getKey().getDate_publication().compareTo(e1.getKey().getDate_publication()));

        for (Map.Entry<Post, List<String>> entry : postEntries) {
            VBox postCard = createPostCard(entry.getKey(), entry.getValue(), true);
            postList.getChildren().add(postCard);
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

    private VBox createPostCard(Post post, List<String> mediaUrls, boolean isMyPost) {
        List<PostTag> tags = post.getTags();

        if (tags == null) {
            tags = new ArrayList<>(); // Initialize with an empty list if null
        }
        VBox postCard = new VBox();
        postCard.setSpacing(10);
        postCard.getStyleClass().add("post-card");

        HBox postContent = new HBox();
        postContent.setSpacing(15);

        utilisateur user = postService.getUserPById(post.getUser_p_id());

        VBox userInfo = new VBox();
        Label usernameLabel = new Label(user.getPrenom());
        Label timestampLabel = new Label(post.getDate_publication().toString());
        usernameLabel.getStyleClass().add("username");
        timestampLabel.getStyleClass().add("timestamp");
        userInfo.getChildren().addAll(usernameLabel, timestampLabel);

        ImageView profileImageView = new ImageView();
        profileImageView.setFitWidth(40);
        profileImageView.setFitHeight(40);
        profileImageView.setImage(new Image(getClass().getResourceAsStream("/images/avatar-15.png")));

        VBox content = new VBox();
        Label postText = new Label(post.getContenu());


        HBox tagBox = new HBox(5);
        tagBox.setPadding(new Insets(5, 0, 0, 0));
        for (PostTag tag : post.getTags()) {
            Label tagLabel = new Label(tag.getLabel());
            tagLabel.getStyleClass().add("tag-badge");
            tagBox.getChildren().add(tagLabel);
        }
        content.getChildren().addAll(postText, tagBox);

        VBox mediaContainer = new VBox();
        mediaContainer.setSpacing(5);
        for (String mediaUrl : mediaUrls) {
            ImageView imageView = new ImageView(new Image("file:" + mediaUrl));
            imageView.setFitWidth(80);
            imageView.setFitHeight(80);
            imageView.setPreserveRatio(true);
            mediaContainer.getChildren().add(imageView);
        }

        HBox stats = new HBox();
        stats.setSpacing(15);
        stats.setStyle("-fx-alignment: center-left;");
        stats.setPrefWidth(Double.MAX_VALUE);

        Label likesLabel = new Label("♥ " + post.getNbr_jaime());
        Label commentsLabel = new Label("💬 0");
        likesLabel.getStyleClass().add("stat-label");
        commentsLabel.getStyleClass().add("stat-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        stats.getChildren().addAll(likesLabel, commentsLabel, spacer);

        if (isMyPost) {
            ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/icons/edit.png")));
            editIcon.setFitWidth(20);
            editIcon.setFitHeight(20);
            Button editBtn = new Button("", editIcon);
            editBtn.setStyle("-fx-background-color: transparent;");
            editBtn.setTooltip(new Tooltip("Modifier"));

            editBtn.setOnAction(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditPostDialog.fxml"));
                    Parent root = loader.load();

                    EditPostController controller = loader.getController();
                    controller.setPost(post);

                    Stage stage = new Stage();
                    stage.setTitle("Modifier le post");
                    stage.setScene(new Scene(root));
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.showAndWait();

                    loadPosts();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/icons/delete.png")));
            deleteIcon.setFitWidth(20);
            deleteIcon.setFitHeight(20);
            Button deleteBtn = new Button("", deleteIcon);
            deleteBtn.setStyle("-fx-background-color: transparent;");
            deleteBtn.setTooltip(new Tooltip("Supprimer"));

            deleteBtn.setOnAction(e -> {
                try {
                    postService.delete(post);
                    postList.getChildren().remove(postCard);
                    loadPosts();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

            stats.getChildren().addAll(editBtn, deleteBtn);
        }

        postContent.getChildren().addAll(profileImageView, userInfo, content);
        postCard.getChildren().addAll(postContent, mediaContainer, new Separator(), stats);

        return postCard;
    }
}