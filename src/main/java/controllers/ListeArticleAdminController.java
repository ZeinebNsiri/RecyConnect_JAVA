package controllers;

import entities.Article;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.ArticleService;
import services.CateArtService;

import java.io.File;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import javax.activation.DataSource;
import java.util.Properties;




public class ListeArticleAdminController {

    @FXML private TableView<ArticleView> articleTable;
    @FXML private TableColumn<ArticleView, Integer> idColumn;
    @FXML private TableColumn<ArticleView, String> imageColumn;
    @FXML private TableColumn<ArticleView, String> nomColumn;
    @FXML private TableColumn<ArticleView, String> categorieColumn;
    @FXML private TableColumn<ArticleView, String> quantiteColumn;
    @FXML private TableColumn<ArticleView, String> prixColumn;
    @FXML private TableColumn<ArticleView, String> proprietaireColumn;

    @FXML private TextField nomArticleField;
    @FXML private TextField proprietaireField;
    @FXML private ComboBox<String> categorieFilterCombo;
    @FXML private Button rechercherBtn;
    @FXML private Button btnPrevious;
    @FXML private Button btnNext;
    @FXML private HBox pagination;

    private ObservableList<ArticleView> allViews;
    private FilteredList<ArticleView> filteredArticles;
    private static final int ROWS_PER_PAGE = 5;
    private int currentPageIndex = 0;
    private int pageCount = 1;


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
                    setGraphic(null);
                } else {
                    try {
                        String imageUrl = "C:/Users/azizz/OneDrive/Bureau/Recyconnect/public/uploads/photo_dir/" + imagePath;
                        File file = new File(imageUrl);
                        imageView.setImage(new Image(file.toURI().toString()));
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
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


            pageCount = (int) Math.ceil((double) filteredArticles.size() / ROWS_PER_PAGE);
            pageCount = Math.max(pageCount, 1);
            currentPageIndex = 0;
            setCurrentPage(currentPageIndex);


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

        int pageCount = (int) Math.ceil((double) filteredArticles.size() / ROWS_PER_PAGE);
        pageCount = (int) Math.ceil((double) filteredArticles.size() / ROWS_PER_PAGE);
        pageCount = Math.max(pageCount, 1);
        currentPageIndex = 0;
        setCurrentPage(currentPageIndex);

    }


    private void addActionColumn() {
        // Supprimer les anciennes colonnes d'action si elles existent
        articleTable.getColumns().removeIf(col -> col.getText().isBlank());

        TableColumn<ArticleView, Void> actionsColumn = new TableColumn<>("");
        actionsColumn.setPrefWidth(100); // Empêche l’affichage de "..."

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button banButton = new Button("🗑 Ban");

            {
                banButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand;");
                banButton.setPrefWidth(80);

                banButton.setOnAction(e -> {
                    ArticleView view = getTableView().getItems().get(getIndex());

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmation de bannissement");
                    confirm.setHeaderText("Êtes-vous sûr de vouloir bannir cet article ?");
                    confirm.setContentText("Cette action est irréversible.");

                    ButtonType oui = new ButtonType("Oui", ButtonBar.ButtonData.OK_DONE);
                    ButtonType non = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
                    confirm.getButtonTypes().setAll(oui, non);

                    confirm.showAndWait().ifPresent(response -> {
                        if (response == oui) {
                            try {
                                ArticleService service = new ArticleService();
                                Article articleToDelete = service.getArticleById(view.getId());
                                String ownerEmail = service.getEmailUtilisateurById(articleToDelete.getUtilisateur_id());

                                service.delete(articleToDelete);

                                sendBanEmail(ownerEmail, articleToDelete.getNom_article());

                                loadArticles();
                                applyFilters();
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
                setGraphic(empty ? null : new HBox(banButton));
                setStyle("-fx-alignment: CENTER;");
            }
        });

        articleTable.getColumns().add(actionsColumn);
    }



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

    private void sendBanEmail(String recipientEmail, String articleName) {
        final String username = "recyconnectapp2425@gmail.com";
        final String password = "kqfn xmcd aquh gbpe";

        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Notification de bannissement de votre article");

            // 🔥 Email HTML + Image
            MimeMultipart multipart = new MimeMultipart("related");

            // 1. Partie HTML
            BodyPart htmlPart = new MimeBodyPart();
            String htmlContent = "<html><body style='font-family: Arial, sans-serif; color: #333;'>"
                    + "<div style='text-align: center;'>"
                    + "<img src='cid:logo' style='width: 120px; height: auto; margin-bottom: 20px;' />"
                    + "</div>"
                    + "<div style='text-align: center;'>"
                    + "<h2 style='color: #dc3545;'>Article Banni</h2>"
                    + "<p>Bonjour,</p>"
                    + "<p>Votre article intitulé <strong>\"" + articleName + "\"</strong> a été <span style='color: #dc3545;'>banni</span> de la plateforme <strong>RecyConnect</strong> pour non-conformité.</p>"
                    + "<p>Si vous pensez qu'il s'agit d'une erreur, veuillez nous contacter.</p>"
                    + "<br>"
                    + "<p style='font-size: 12px; color: #777;'>Merci de votre compréhension.<br>L'équipe RecyConnect.</p>"
                    + "</div>"
                    + "</body></html>";

            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // 2. Partie Image (logo)
            MimeBodyPart imagePart = new MimeBodyPart();
            InputStream logoStream = getClass().getResourceAsStream("/mainlogo.png");
            if (logoStream == null) {
                System.out.println("❌ Logo introuvable !");
                return;
            }
            DataSource fds = new ByteArrayDataSource(logoStream, "image/png");
            imagePart.setDataHandler(new DataHandler(fds));
            imagePart.setHeader("Content-ID", "<logo>");
            imagePart.setDisposition(MimeBodyPart.INLINE);
            multipart.addBodyPart(imagePart);

            message.setContent(multipart);

            Transport.send(message);

            System.out.println("✅ Email envoyé avec succès à : " + recipientEmail);

        } catch (AuthenticationFailedException e) {
            System.out.println("❌ Erreur d'authentification : " + e.getMessage());
            e.printStackTrace();
        } catch (SendFailedException e) {
            System.out.println("❌ Erreur d'envoi : " + e.getMessage());
            e.printStackTrace();
        } catch (MessagingException e) {
            System.out.println("❌ Erreur de messagerie : " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("❌ Erreur inconnue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setCurrentPage(int index) {
        currentPageIndex = index;
        createPage(index);
        updateCustomPagination();
    }

    private void updateCustomPagination() {
        pagination.getChildren().clear();

        for (int i = 0; i < pageCount; i++) {
            final int index = i;
            Button pageButton = new Button(String.valueOf(i + 1));
            pageButton.getStyleClass().add("btn-pagination");
            if (i == currentPageIndex) {
                pageButton.getStyleClass().add("btn-pagination-active");
            }
            pageButton.setOnAction(e -> setCurrentPage(index));
            pagination.getChildren().add(pageButton);
        }
    }


    private void createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredArticles.size());

        ObservableList<ArticleView> currentPageData = FXCollections.observableArrayList(
                filteredArticles.subList(fromIndex, toIndex)
        );

        SortedList<ArticleView> sortedData = new SortedList<>(currentPageData);
        sortedData.comparatorProperty().bind(articleTable.comparatorProperty());
        articleTable.setItems(sortedData);

        addActionColumn();
    }

    @FXML
    private void handlePrevious() {
        if (currentPageIndex > 0) {
            setCurrentPage(currentPageIndex - 1);
        }
    }

    @FXML
    private void handleNext() {
        if (currentPageIndex < pageCount - 1) {
            setCurrentPage(currentPageIndex + 1);
        }
    }




}
