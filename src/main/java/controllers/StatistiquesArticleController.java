package controllers;

import entities.Article;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;

import services.ArticleService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatistiquesArticleController {

    @FXML
    private PieChart pieChart;

    @FXML
    private Button retourButton;

    @FXML
    public void initialize() {
        loadPieChartData();
    }

    private void loadPieChartData() {
        try {
            ArticleService articleService = new ArticleService();
            List<Article> articles = articleService.displayList();

            // Compter les articles par catégorie
            Map<String, Integer> categorieCount = new HashMap<>();
            for (Article a : articles) {
                String nomCategorie = articleService.getCategorieById(a.getCategorie_id()).getNom_categorie();
                categorieCount.put(nomCategorie, categorieCount.getOrDefault(nomCategorie, 0) + 1);
            }

            int totalArticles = articles.size(); // Pour calculer les pourcentages

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            for (Map.Entry<String, Integer> entry : categorieCount.entrySet()) {
                String nomCategorie = entry.getKey();
                int count = entry.getValue();
                double percentage = ((double) count / totalArticles) * 100;

                // 🔥 On affiche : nom (XX%) (nombre articles)
                String label = String.format("%s : %.1f%% (%d)", nomCategorie, percentage, count);
                pieChartData.add(new PieChart.Data(label, count));
            }

            pieChart.setData(pieChartData);
            pieChart.setLegendVisible(true);
            pieChart.setLabelsVisible(true);
            pieChart.setTitle("Répartition des Articles par Catégorie");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void backToArticles() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseAdmin.fxml"));
            Parent root = loader.load();
            BaseAdminController baseAdminController = loader.getController();
            baseAdminController.showCategorieView();
            pieChart.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
