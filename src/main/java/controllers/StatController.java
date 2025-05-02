package controllers;

import entities.Commande;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import services.CommandeService;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class StatController implements Initializable {

    @FXML
    private Label totalVentesLabel;

    @FXML
    private Label ventesPayeesLabel;

    private final CommandeService commandeService = new CommandeService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Appeler les méthodes pour afficher les statistiques
        afficherStatistiques();
    }

    // Fonction pour afficher le total des ventes d'aujourd'hui
    private double afficherTotalVentesAujourdHui() {
        double totalVentesAujourdHui = 0;

        // Récupérer toutes les commandes
        for (Commande cmd : commandeService.getAllCommandes()) {
            if (cmd.getDateCommande().toLocalDate().isEqual(LocalDate.now())) {
                totalVentesAujourdHui += cmd.getPrixTotal();
            }
        }

        return totalVentesAujourdHui;
    }

    // Fonction pour calculer le nombre de commandes payées par VISA
    private int calculerCommandesPayeesParVisa() {
        int nbCommandesPayeesParVisa = 0;

        // Récupérer toutes les commandes
        for (Commande cmd : commandeService.getAllCommandes()) {
            if (cmd.getStatut().equalsIgnoreCase("Payé par VISA")) {
                nbCommandesPayeesParVisa++;
            }
        }

        return nbCommandesPayeesParVisa;
    }

    // Fonction qui met à jour les labels avec les statistiques
    private void afficherStatistiques() {
        double totalVentesAujourdHui = afficherTotalVentesAujourdHui();
        int nbCommandesPayeesParVisa = calculerCommandesPayeesParVisa();

        // Mettre à jour les labels
        totalVentesLabel.setText(String.format("%.3f TND", totalVentesAujourdHui));
        ventesPayeesLabel.setText(String.valueOf(nbCommandesPayeesParVisa));
    }
}
