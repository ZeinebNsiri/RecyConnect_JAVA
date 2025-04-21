package controllers;

import entities.Article;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import services.ArticleService;
import services.CateArtService;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ListeArticleAdminController {

    @FXML private TableView<ArticleView> articleTable;
    @FXML private TableColumn<ArticleView, Integer> idColumn;
    @FXML private TableColumn<ArticleView, String> imageColumn;
    @FXML private TableColumn<ArticleView, String> nomColumn;
    @FXML private TableColumn<ArticleView, String> categorieColumn;
    @FXML private TableColumn<ArticleView, String> quantiteColumn;
    @FXML private TableColumn<ArticleView, String> prixColumn;
    @FXML private TableColumn<ArticleView, String> proprietaireColumn;
    @FXML private TableColumn<ArticleView, Void> actionsColumn;

    @FXML private TextField nomArticleField;
    @FXML private TextField proprietaireField;
    @FXML private ComboBox<String> categorieFilterCombo;
    @FXML private Button rechercherBtn;

    private ObservableList<ArticleView> allViews;
    private FilteredList<ArticleView> filteredArticles;
    private SortedList<ArticleView> sortedArticles;

    @FXML
    public void initialize() {
        articleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nomArticle"));
        quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        categorieColumn.setCellValueFactory(new PropertyValueFactory<>("nomCategorie"));
        proprietaireColumn.setCellValueFactory(new PropertyValueFactory<>("nomProprietaire"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("imagePath"));

        // ✅ Image column with corrected updateItem
        imageColumn.setCellFactory(param -> new TableCell<ArticleView, String>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(80);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(false);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setStyle("-fx-alignment: CENTER;");
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);

                if (empty || imagePath == null || imagePath.trim().isEmpty()) {
                    System.out.println("🔴 IMAGE VIDE");
                    setGraphic(null);
                } else {
                    try {
                        String imageUrl = "C:/Users/Admin/Desktop/PI_RecyConnect_TechSquad/public/uploads/photo_dir/" + imagePath;
                        System.out.println("🟢 Chargement image: " + imageUrl);
                        File file = new File(imageUrl);
                        imageView.setImage(new Image(file.toURI().toString()));
                        setGraphic(imageView);
                    } catch (Exception e) {
                        System.out.println("⚠️ Erreur chargement image: " + e.getMessage());
                        setGraphic(null);
                    }
                }
            }
        });

        // Actions column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button banButton = new Button("🗑 Ban");
            private final HBox box = new HBox(banButton);

            {
                box.setStyle("-fx-alignment: center;");
                banButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                banButton.setOnAction(e -> {
                    ArticleView view = getTableView().getItems().get(getIndex());
                    try {
                        ArticleService service = new ArticleService();
                        Article articleToDelete = service.getArticleById(view.getId());
                        service.delete(articleToDelete);
                        loadArticles(); // Refresh
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        rechercherBtn.setOnAction(e -> applyFilters());

        loadArticles();
        loadCategories();
    }

    private void loadArticles() {
        try {
            ArticleService articleService = new ArticleService();
            List<Article> articles = articleService.displayList();
            List<ArticleView> views = new ArrayList<>();

            for (Article a : articles) {
                String nomCategorie = articleService.getCategorieById(a.getCategorie_id()).getNom_categorie();
                String nomUtilisateur = articleService.getNomUtilisateurById(a.getUtilisateur_id());

                views.add(new ArticleView(
                        a.getId(),
                        a.getNom_article(),
                        a.getQuantite_article() + " KG",
                        a.getPrix() + " TN/KG",
                        a.getImage_article(),
                        nomCategorie,
                        nomUtilisateur
                ));
            }

            allViews = FXCollections.observableArrayList(views);
            filteredArticles = new FilteredList<>(allViews, p -> true);
            sortedArticles = new SortedList<>(filteredArticles);
            sortedArticles.comparatorProperty().bind(articleTable.comparatorProperty());
            articleTable.setItems(sortedArticles);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCategories() {
        try {
            categorieFilterCombo.getItems().clear();
            categorieFilterCombo.getItems().add("Toutes les catégories");
            new CateArtService().displayList().forEach(cat ->
                    categorieFilterCombo.getItems().add(cat.getNom_categorie())
            );
            categorieFilterCombo.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void applyFilters() {
        String selectedCategorie = categorieFilterCombo.getValue();
        String searchNom = nomArticleField.getText().toLowerCase();
        String searchProp = proprietaireField.getText().toLowerCase();

        filteredArticles.setPredicate(a -> {
            boolean matchCategorie = selectedCategorie.equals("Toutes les catégories")
                    || a.getNomCategorie().equalsIgnoreCase(selectedCategorie);
            boolean matchNom = searchNom.isEmpty()
                    || a.getNomArticle().toLowerCase().contains(searchNom);
            boolean matchProp = searchProp.isEmpty()
                    || a.getNomProprietaire().toLowerCase().contains(searchProp);
            return matchCategorie && matchNom && matchProp;
        });
    }

    // DTO interne pour affichage enrichi
    public static class ArticleView {
        private final int id;
        private final String nomArticle;
        private final String quantite;
        private final String prix;
        private final String imagePath;
        private final String nomCategorie;
        private final String nomProprietaire;

        public ArticleView(int id, String nomArticle, String quantite, String prix, String imagePath, String nomCategorie, String nomProprietaire) {
            this.id = id;
            this.nomArticle = nomArticle;
            this.quantite = quantite;
            this.prix = prix;
            this.imagePath = imagePath;
            this.nomCategorie = nomCategorie;
            this.nomProprietaire = nomProprietaire;
        }

        public int getId() { return id; }
        public String getNomArticle() { return nomArticle; }
        public String getQuantite() { return quantite; }
        public String getPrix() { return prix; }
        public String getImagePath() { return imagePath; }
        public String getNomCategorie() { return nomCategorie; }
        public String getNomProprietaire() { return nomProprietaire; }
    }
}
