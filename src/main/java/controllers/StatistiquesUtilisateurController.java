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
            // Obtenir les données des utilisateurs
            int particuliers = utilisateurService.getCountByRole("ROLE_USER");
            int professionnels = utilisateurService.getCountByRole("ROLE_PROFESSIONNEL");

            // Remplir les graphiques
            PieChart.Data particulierData = new PieChart.Data("Particuliers", particuliers);
            PieChart.Data professionnelData = new PieChart.Data("Professionnels", professionnels);
            rolePieChart.getData().addAll(particulierData, professionnelData);

            // Afficher les chiffres totaux
            totalRoleCount.setText(String.valueOf(particuliers + professionnels));
            particulierCount.setText(String.valueOf(particuliers));
            professionnelCount.setText(String.valueOf(professionnels));
        } catch (Exception e) {
            showError("Erreur de récupération des statistiques des rôles", e.getMessage());
        }
    }

    // Charger les statistiques de statut
    private void loadStatusStatistics() {
        try {
            // Obtenir les données des utilisateurs
            int actifs = utilisateurService.getCountByStatus(true);
            int desactives = utilisateurService.getCountByStatus(false);

            // Remplir les graphiques
            PieChart.Data actifData = new PieChart.Data("Actifs", actifs);
            PieChart.Data desactiveData = new PieChart.Data("Désactivés", desactives);
            statusPieChart.getData().addAll(actifData, desactiveData);

            // Afficher les chiffres totaux
            totalStatusCount.setText(String.valueOf(actifs + desactives));
            actifCount.setText(String.valueOf(actifs));
            desactiveCount.setText(String.valueOf(desactives));
        } catch (Exception e) {
            showError("Erreur de récupération des statistiques de statut", e.getMessage());
        }
    }

    // Méthode pour afficher un message d'erreur
    private void showError(String title, String message) {
        // Afficher une alerte avec l'erreur
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    public void back() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseAdmin.fxml"));
            Parent root = loader.load();
            BaseAdminController baseAdminController = loader.getController();
            baseAdminController.showUsersView();
            rolePieChart.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
