package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import services.UtilisateurService;

import java.io.IOException;

public class StatistiquesUtilisateurController {

    @FXML
    private PieChart rolePieChart;
    @FXML
    private PieChart statusPieChart;
    private UtilisateurService utilisateurService = new UtilisateurService();

    @FXML
    public void initialize() {
        // Remplir les graphiques avec les données
        loadRoleStatistics();
        loadStatusStatistics();
    }

    // Charger les statistiques des rôles (Particuliers et Professionnels)
    private void loadRoleStatistics() {
        try {
            // Obtenir les données de l'utilisateur (Utiliser une méthode de ton service pour cela)
            int particuliers = utilisateurService.getCountByRole("ROLE_USER");
            int professionnels = utilisateurService.getCountByRole("ROLE_PROFESSIONNEL");

            // Créer les sections de la PieChart
            PieChart.Data particulierData = new PieChart.Data("Particuliers", particuliers);
            PieChart.Data professionnelData = new PieChart.Data("Professionnels", professionnels);

            // Ajouter les données au graphique
            rolePieChart.getData().addAll(particulierData, professionnelData);
        } catch (Exception e) {
            showError("Erreur de récupération des statistiques des rôles", e.getMessage());
        }
    }

    // Charger les statistiques de statut (Actif et Désactivé)
    private void loadStatusStatistics() {
        try {
            // Obtenir les données des utilisateurs (Actifs et Désactivés)
            int actifs = utilisateurService.getCountByStatus(true);  // Actifs
            int desactives = utilisateurService.getCountByStatus(false);  // Désactivés

            // Créer les sections de la PieChart
            PieChart.Data actifData = new PieChart.Data("Actifs", actifs);
            PieChart.Data desactiveData = new PieChart.Data("Désactivés", desactives);

            // Ajouter les données au graphique
            statusPieChart.getData().addAll(actifData, desactiveData);
        } catch (Exception e) {
            showError("Erreur de récupération des statistiques de statut", e.getMessage());
        }
    }

    // Méthode pour afficher un message d'erreur
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode pour revenir au dashboard
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
