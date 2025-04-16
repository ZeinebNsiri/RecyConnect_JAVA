package controllers;

import entities.Article;
import entities.Commande;
import entities.LigneCommande;
import entities.utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import services.CommandeService;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ListeCommandesController {

    @FXML
    private ComboBox<String> filterComboBox;

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
            System.out.println("Commandes récupérées: " + commandes.size());

            List<LigneCommande> lignes = new ArrayList<>();

            for (Commande commande : commandes) {
                System.out.println("Commande ID: " + commande.getId() + ", Total: " + commande.getPrixTotal());

                StringBuilder articleNames = new StringBuilder();
                String nomClient = "";

                for (LigneCommande lc : commande.getLigneCommandes()) {
                    articleNames.append(lc.getArticle().getNom_article()).append(", ");
                    nomClient = lc.getUtilisateur().getNom_user();

                    System.out.println(" - Article: " + lc.getArticle().getNom_article() + ", Client: " + nomClient);
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
            commandesTable.setItems(allCommandes);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterCommandes() {
        String selected = filterComboBox.getValue();
        if (selected != null) {
            if (selected.equals("En attente")) {
                commandesTable.setItems(allCommandes.filtered(ligne -> "En attente".equals(ligne.getEtat())));
            } else if (selected.equals("Payées")) {
                commandesTable.setItems(allCommandes.filtered(ligne -> "Payée".equals(ligne.getEtat())));
            } else {
                commandesTable.setItems(allCommandes);
            }
        }
    }
}
