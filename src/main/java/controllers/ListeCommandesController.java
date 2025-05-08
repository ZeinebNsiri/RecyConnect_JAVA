package controllers;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import entities.Article;
import entities.Commande;
import entities.LigneCommande;
import entities.utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import services.CommandeService;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.element.Cell;


import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;


public class ListeCommandesController {

    @FXML private ComboBox<String> filterComboBox;
    @FXML private HBox paginationContainer;
    @FXML private TableView<LigneCommande> commandesTable;
    @FXML private TableColumn<LigneCommande, Integer> idColumn;
    @FXML private TableColumn<LigneCommande, Double> prixColumn;
    @FXML private TableColumn<LigneCommande, String> dateColumn;
    @FXML private TableColumn<LigneCommande, String> statutCommandeColumn;
    @FXML private TableColumn<LigneCommande, String> clientColumn;
    @FXML private TableColumn<LigneCommande, String> articlesColumn;
    @FXML private TextField searchField;
    @FXML private TableColumn<LigneCommande, Void> actionColumn;

    private int currentPage = 1;
    private static final int ITEMS_PER_PAGE = 10;
    private List<LigneCommande> filteredList = new ArrayList<>();
    private final ObservableList<LigneCommande> allCommandes = FXCollections.observableArrayList();
    private final CommandeService commandeService = new CommandeService();

    @FXML
    public void initialize() {
        commandesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateCommandeFormatted"));
        statutCommandeColumn.setCellValueFactory(new PropertyValueFactory<>("etat"));
        clientColumn.setCellValueFactory(new PropertyValueFactory<>("nomClient"));
        articlesColumn.setCellValueFactory(new PropertyValueFactory<>("articles"));
        addButtonToTable();
        loadCommandes();

        filterComboBox.setOnAction(event -> filterCommandes());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> SearchArticles());
    }

    private void loadCommandes() {
        try {
            List<Commande> commandes = commandeService.getCommandesAvecDetailsParUtilisateur();
            List<LigneCommande> lignes = new ArrayList<>();

            for (Commande commande : commandes) {
                StringBuilder articleNames = new StringBuilder();
                String nomClient = "";

                for (LigneCommande lc : commande.getLigneCommandes()) {
                    articleNames.append(lc.getArticle().getNom_article()).append(", ");
                    nomClient = lc.getUtilisateur().getNom_user();
                }

                if (!commande.getLigneCommandes().isEmpty()) {
                    LigneCommande ligne = new LigneCommande();
                    ligne.setId(commande.getId());
                    ligne.setPrix(commande.getPrixTotal());
                    ligne.setEtat(commande.getStatut());
                    ligne.setDateCommande(commande.getDateCommande());

                    utilisateur client = new utilisateur();
                    client.setNom_user(nomClient);
                    ligne.setUtilisateur(client);

                    Article article = new Article();
                    article.setNom_article(articleNames.toString().replaceAll(", $", ""));
                    ligne.setArticle(article);

                    lignes.add(ligne);
                }
            }

            allCommandes.setAll(lignes);
            filteredList = new ArrayList<>(allCommandes);
            currentPage = 1;
            updateCommandesTable();
            buildPagination();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterCommandes() {
        String selected = filterComboBox.getValue();

        if (selected != null) {
            if (selected.equals("Payées")) {
                filteredList = allCommandes.filtered(ligne -> "Payé par VISA".equalsIgnoreCase(ligne.getEtat()));
            } else if (selected.equals("Non payées")) {
                filteredList = allCommandes.filtered(ligne -> "Payé par livraison".equalsIgnoreCase(ligne.getEtat()));
            } else {
                filteredList = new ArrayList<>(allCommandes);
            }

            currentPage = 1;
            updateCommandesTable();
            buildPagination();
        }
    }

    private void updateCommandesTable() {
        int fromIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredList.size());
        List<LigneCommande> pageItems = filteredList.subList(fromIndex, toIndex);
        commandesTable.setItems(FXCollections.observableArrayList(pageItems));
    }

    private void buildPagination() {
        paginationContainer.getChildren().clear();
        int totalPages = (int) Math.ceil((double) filteredList.size() / ITEMS_PER_PAGE);

        Button prev = new Button("« Précédent");
        prev.setDisable(currentPage == 1);
        prev.setStyle("-fx-background-color: #198754; -fx-text-fill: white;");
        prev.setOnAction(e -> {
            currentPage--;
            updateCommandesTable();
            buildPagination();
        });
        paginationContainer.getChildren().add(prev);

        for (int i = 1; i <= totalPages; i++) {
            Button pageBtn = new Button(String.valueOf(i));
            pageBtn.setStyle(i == currentPage
                    ? "-fx-background-color: #198754; -fx-text-fill: white;"
                    : "-fx-background-color: white; -fx-border-color: #198754; -fx-text-fill: #198754;");
            final int pageIndex = i;
            pageBtn.setOnAction(e -> {
                currentPage = pageIndex;
                updateCommandesTable();
                buildPagination();
            });
            paginationContainer.getChildren().add(pageBtn);
        }

        Button next = new Button("Suivant »");
        next.setDisable(currentPage == totalPages);
        next.setStyle("-fx-background-color: #198754; -fx-text-fill: white;");
        next.setOnAction(e -> {
            currentPage++;
            updateCommandesTable();
            buildPagination();
        });
        paginationContainer.getChildren().add(next);
    }

    private void SearchArticles() {
        String selected = filterComboBox.getValue();
        String searchText = searchField.getText().toLowerCase();

        filteredList = allCommandes.filtered(ligne -> {
            boolean matchStatut = true;
            boolean matchSearch = ligne.getArticle().getNom_article().toLowerCase().contains(searchText);

            if ("Payées".equals(selected)) {
                matchStatut = "Payé par VISA".equalsIgnoreCase(ligne.getEtat());
            } else if ("Non payées".equals(selected)) {
                matchStatut = "Paiement à la livraison".equalsIgnoreCase(ligne.getEtat());
            }

            return matchStatut && matchSearch;
        });

        currentPage = 1;
        updateCommandesTable();
        buildPagination();
    }

    private void addButtonToTable() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Générer Facture");

            {
                btn.setOnAction(event -> {
                    LigneCommande ligne = getTableView().getItems().get(getIndex());
                    generateInvoiceFor(ligne);
                });
                btn.setStyle("-fx-background-color: #198754; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    private void generateInvoiceFor(LigneCommande ligne) {
        if (ligne == null) return;

        String fileName = "Facture_Commande_" + ligne.getId() + ".pdf";
        File pdfFile = new File(System.getProperty("user.home"), fileName);

        try {
            PdfWriter writer = new PdfWriter(pdfFile.getAbsolutePath());
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // --- Logo en haut à gauche ---
            String logoPath = getClass().getResource("/mainlogo.png").getPath();
            if (new File(logoPath).exists()) {
                ImageData data = ImageDataFactory.create(logoPath);
                Image logo = new Image(data).scaleToFit(80, 80);
                logo.setFixedPosition(pdf.getDefaultPageSize().getWidth() - 130, pdf.getDefaultPageSize().getHeight() - 100);
                document.add(logo);
            }

            // --- Couleur verte foncée personnalisée ---
            com.itextpdf.kernel.colors.Color vertFoncer = new DeviceRgb(25, 135, 84); // #198754

            // --- Titre Facture centré ---
            Paragraph header = new Paragraph("Facture")
                    .setFontSize(22)
                    .setBold()
                    .setFontColor(vertFoncer)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20);
            document.add(header);

            // --- Infos commande ---
            document.add(new Paragraph("Numéro de Facture : " + ligne.getId()));
            document.add(new Paragraph("Date : " + ligne.getDateCommandeFormatted()));

            document.add(new Paragraph("\nInformations du Client").setBold());
            document.add(new Paragraph("Nom et Prénom : " + ligne.getUtilisateur().getNom_user()));
            document.add(new Paragraph("Adresse : ")); // à compléter

            // --- Tableau des articles ---
            document.add(new Paragraph("\nDétails de la Commande").setBold());

            Table table = new Table(4).useAllAvailableWidth();
            table.addHeaderCell(new Cell().add(new Paragraph("Article").setBold()).setBackgroundColor(vertFoncer).setFontColor(ColorConstants.WHITE));
            table.addHeaderCell(new Cell().add(new Paragraph("Quantité").setBold()).setBackgroundColor(vertFoncer).setFontColor(ColorConstants.WHITE));
            table.addHeaderCell(new Cell().add(new Paragraph("Prix Unitaire (DT)").setBold()).setBackgroundColor(vertFoncer).setFontColor(ColorConstants.WHITE));
            table.addHeaderCell(new Cell().add(new Paragraph("Total (DT)").setBold()).setBackgroundColor(vertFoncer).setFontColor(ColorConstants.WHITE));

            String[] articles = ligne.getArticle().getNom_article().split(",\\s*");
            double total = ligne.getPrix();
            double prixUnitaire = total / articles.length;

            for (String nom : articles) {
                table.addCell(nom);
                table.addCell("1"); // ou quantité réelle si disponible
                table.addCell(String.format("%.2f", prixUnitaire));
                table.addCell(String.format("%.2f", prixUnitaire));
            }

            document.add(table);

            // --- Total final ---
            Paragraph totalParagraph = new Paragraph("Total à payer : " + String.format("%.2f", total) + " DT")
                    .setBold()
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(10);
            document.add(totalParagraph);

            // --- Footer (optionnel) ---
            document.add(new Paragraph("\nRecyConect")
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();

            // --- Alerte succès ---
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Facture générée");
            alert.setHeaderText("Facture enregistrée avec succès !");
            alert.setContentText("Fichier : " + pdfFile.getAbsolutePath());
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible de générer la facture");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }




}
