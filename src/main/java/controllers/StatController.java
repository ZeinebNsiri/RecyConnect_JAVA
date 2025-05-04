package controllers;

import entities.Commande;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import services.CommandeService;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;


import java.util.Map;
import java.util.TreeMap;


public class StatController implements Initializable {

    @FXML
    private Label totalVentesLabel;

    @FXML
    private Label ventesPayeesLabel;

    @FXML
    private PieChart pieChartVentes;

    @FXML
    private BarChart<String, Number> barChartVisa;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    private final CommandeService commandeService = new CommandeService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        afficherStatistiques();
        afficherStatistiquesVentes();
        afficherBarChartPaiement(); // ← Ajouté ici
    }

    private void afficherStatistiquesVentes() {
        double totalAujourdHui = 0;
        double totalPayees = 0;

        for (Commande cmd : commandeService.getAllCommandes()) {
            if (cmd.getDateCommande().toLocalDate().isEqual(LocalDate.now())) {
                totalAujourdHui += cmd.getPrixTotal();
                if ("Payé".equalsIgnoreCase(cmd.getStatut()) || "Payé par VISA".equalsIgnoreCase(cmd.getStatut())) {
                    totalPayees += cmd.getPrixTotal();
                }
            }
        }

        double totalNonPayees = totalAujourdHui - totalPayees;

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Payées", totalPayees),
                new PieChart.Data("Non Payées", totalNonPayees)
        );

        pieChartVentes.setData(pieChartData);
        pieChartVentes.setTitle("Répartition des ventes d'aujourd'hui");
    }

    private void afficherBarChartPaiement() {
        Map<LocalDate, Double> paiementsParJour = new TreeMap<>(); // ← bien typé

        LocalDate aujourdHui = LocalDate.now();
        LocalDate ilYA30Jours = aujourdHui.minusDays(29); // Inclure aujourd'hui

        for (Commande cmd : commandeService.getAllCommandes()) {
            LocalDate dateCmd = cmd.getDateCommande().toLocalDate();
            String statut = cmd.getStatut().toLowerCase();

            if (!dateCmd.isBefore(ilYA30Jours) && !dateCmd.isAfter(aujourdHui) &&
                    (statut.equals("payé") || statut.equals("payé par visa"))) {

                double montantExistant = paiementsParJour.getOrDefault(dateCmd, 0.0);
                paiementsParJour.put(dateCmd, montantExistant + cmd.getPrixTotal());
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventes Payées");

        for (int i = 0; i < 30; i++) {
            LocalDate date = ilYA30Jours.plusDays(i);
            double total = paiementsParJour.getOrDefault(date, 0.0);
            series.getData().add(new XYChart.Data<>(date.toString(), total));
        }

        barChartVisa.getData().clear();
        barChartVisa.getData().add(series);
        barChartVisa.setTitle("Ventes Payées sur les 30 Derniers Jours");
    }


    private void afficherStatistiques() {
        double totalAujourdHui = 0;

        for (Commande cmd : commandeService.getAllCommandes()) {
            if (cmd.getDateCommande().toLocalDate().isEqual(LocalDate.now())) {
                totalAujourdHui += cmd.getPrixTotal();
            }
        }

        int nbCommandesPayeesParVisa = calculerCommandesPayeesParVisa();

        totalVentesLabel.setText(String.format("%.3f TND", totalAujourdHui));
        ventesPayeesLabel.setText(String.valueOf(nbCommandesPayeesParVisa));
    }

    private int calculerCommandesPayeesParVisa() {
        int nbCommandesPayeesParVisa = 0;

        for (Commande cmd : commandeService.getAllCommandes()) {
            if (cmd.getStatut().equalsIgnoreCase("Payé par VISA")) {
                nbCommandesPayeesParVisa++;
            }
        }

        return nbCommandesPayeesParVisa;
    }
}
