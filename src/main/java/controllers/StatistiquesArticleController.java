package controllers;

import entities.Article;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Button;
import javafx.scene.Node;

import services.ArticleService;

import java.awt.Color;
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
    private BarChart<String, Number> barChartCategorie;

    @FXML
    private BarChart<String, Number> barChartArticle;

    @FXML
    public void initialize() {
        loadPieChartData();
        loadBarChartCategorie();
        loadBarChartArticle();
    }

    private void loadPieChartData() {
        try {
            ArticleService articleService = new ArticleService();
            List<Article> articles = articleService.displayList();

            Map<String, Integer> categorieCount = new HashMap<>();
            for (Article a : articles) {
                String nomCategorie = articleService.getCategorieById(a.getCategorie_id()).getNom_categorie();
                categorieCount.put(nomCategorie, categorieCount.getOrDefault(nomCategorie, 0) + 1);
            }

            int totalArticles = articles.size();
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            for (Map.Entry<String, Integer> entry : categorieCount.entrySet()) {
                String nomCategorie = entry.getKey();
                int count = entry.getValue();
                double percentage = ((double) count / totalArticles) * 100;
                String label = String.format("%s : %.1f%% (%d)", nomCategorie, percentage, count);
                pieChartData.add(new PieChart.Data(label, count));
            }

            pieChart.setData(pieChartData);
            pieChart.setLegendVisible(true);
            pieChart.setLabelsVisible(true);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadBarChartCategorie() {
        try {
            ArticleService articleService = new ArticleService();
            List<Article> articles = articleService.displayList();

            Map<String, Integer> categorieQuantite = new HashMap<>();
            for (Article a : articles) {
                String nomCategorie = articleService.getCategorieById(a.getCategorie_id()).getNom_categorie();
                categorieQuantite.put(nomCategorie,
                        categorieQuantite.getOrDefault(nomCategorie, 0) + a.getQuantite_article());
            }

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (Map.Entry<String, Integer> entry : categorieQuantite.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            barChartCategorie.getData().clear();
            barChartCategorie.getData().add(series);
            barChartCategorie.setLegendVisible(false);


            Platform.runLater(() -> {
                int total = series.getData().size();
                for (int i = 0; i < total; i++) {
                    XYChart.Data<String, Number> data = series.getData().get(i);
                    Node node = data.getNode();
                    if (node != null) {
                        node.setStyle("-fx-bar-fill: " + generateRgbColor(i, total) + ";");
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadBarChartArticle() {
        try {
            ArticleService articleService = new ArticleService();
            List<Article> articles = articleService.displayList();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (Article a : articles) {
                series.getData().add(new XYChart.Data<>(a.getNom_article(), a.getQuantite_article()));
            }

            barChartArticle.getData().clear();
            barChartArticle.getData().add(series);
            barChartArticle.setLegendVisible(false);

            Platform.runLater(() -> {
                int total = series.getData().size();
                for (int i = 0; i < total; i++) {
                    XYChart.Data<String, Number> data = series.getData().get(i);
                    Node node = data.getNode();
                    if (node != null) {
                        node.setStyle("-fx-bar-fill: " + generateRgbColor(i, total) + ";");
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String generateRgbColor(int index, int total) {
        float hue = (index * 360.0f / total) % 360;
        Color color = Color.getHSBColor(hue / 360, 0.7f, 0.9f);
        return String.format("rgb(%d, %d, %d)", color.getRed(), color.getGreen(), color.getBlue());
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
