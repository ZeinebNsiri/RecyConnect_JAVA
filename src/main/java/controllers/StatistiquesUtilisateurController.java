package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import services.UtilisateurService;

import java.io.IOException;

public class StatistiquesUtilisateurController {

    @FXML
    private PieChart rolePieChart;
    @FXML
    private PieChart statusPieChart;

    @FXML
    private Text totalRoleCount;
    @FXML
    private Text particulierCount;
    @FXML
    private Text professionnelCount;

    @FXML
    private Text totalStatusCount;
    @FXML
    private Text actifCount;
    @FXML
    private Text desactiveCount;
    private UtilisateurService utilisateurService = new UtilisateurService();

    @FXML
    public void initialize() {
        loadRoleStatistics();
        loadStatusStatistics();
    }

    // Charger les statistiques des rôles
    private void loadRoleStatistics() {
        try {

            int particuliers = utilisateurService.getCountByRole("ROLE_USER");
            int professionnels = utilisateurService.getCountByRole("ROLE_PROFESSIONNEL");


            PieChart.Data particulierData = new PieChart.Data("Particuliers", particuliers);
            PieChart.Data professionnelData = new PieChart.Data("Professionnels", professionnels);
            rolePieChart.getData().addAll(particulierData, professionnelData);


            totalRoleCount.setText(String.valueOf(particuliers + professionnels));
            particulierCount.setText(String.valueOf(particuliers));
            professionnelCount.setText(String.valueOf(professionnels));
        } catch (Exception e) {
            showError("Erreur de récupération des statistiques des rôles", e.getMessage());
        }
    }


    private void loadStatusStatistics() {
        try {

            int actifs = utilisateurService.getCountByStatus(true);
            int desactives = utilisateurService.getCountByStatus(false);


            PieChart.Data actifData = new PieChart.Data("Actifs", actifs);
            PieChart.Data desactiveData = new PieChart.Data("Désactivés", desactives);
            statusPieChart.getData().addAll(actifData, desactiveData);


            totalStatusCount.setText(String.valueOf(actifs + desactives));
            actifCount.setText(String.valueOf(actifs));
            desactiveCount.setText(String.valueOf(desactives));
        } catch (Exception e) {
            showError("Erreur de récupération des statistiques de statut", e.getMessage());
        }
    }


    private void showError(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



}
