package controllers;

import entities.Article;
import entities.Commande;
import entities.LigneCommande;
import entities.utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import services.CommandeService;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ListeCommandesController {

    @FXML
    private ComboBox<String> filterComboBox;

    @FXML private HBox paginationContainer;

    @FXML
    private TableView<LigneCommande> commandesTable;

    @FXML
    private TableColumn<LigneCommande, Integer> idColumn;

    @FXML
    private TableColumn<LigneCommande, Double> prixColumn;

    @FXML
    private TableColumn<LigneCommande, String> dateColumn;

    @FXML
    private TableColumn<LigneCommande, String> statutCommandeColumn;

    @FXML
    private TableColumn<LigneCommande, String> clientColumn;

    @FXML
    private TableColumn<LigneCommande, String> articlesColumn;

    private int currentPage = 1;
    private static final int ITEMS_PER_PAGE = 5;
    private List<LigneCommande> filteredList = new ArrayList<>();


    private final ObservableList<LigneCommande> allCommandes = FXCollections.observableArrayList();
    private final CommandeService commandeService = new CommandeService();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateCommandeFormatted"));
        statutCommandeColumn.setCellValueFactory(new PropertyValueFactory<>("etat"));
        clientColumn.setCellValueFactory(new PropertyValueFactory<>("nomClient"));
        articlesColumn.setCellValueFactory(new PropertyValueFactory<>("articles"));

        loadCommandes();
        filterComboBox.setOnAction(event -> filterCommandes());
    }

    private void loadCommandes() {
        try {
            List<Commande> commandes = commandeService.getCommandesAvecDetailsParUtilisateur(1);
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
                filteredList = allCommandes.filtered(ligne -> "Payée".equals(ligne.getEtat()));
            } else if (selected.equals("Non payées")) {
                filteredList = allCommandes.filtered(ligne -> "Non payée".equals(ligne.getEtat()));
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

}
