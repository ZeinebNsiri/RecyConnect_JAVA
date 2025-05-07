package controllers.Events;

import entities.Article;
import entities.Commande;
import entities.Event;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import services.ArticleService;
import services.CommandeService;
import services.CoursService;
import services.EventService;
import services.UtilisateurService;

import java.awt.Color;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class DashboardEventsController implements Initializable {

    // Original Event Dashboard components
    @FXML private PieChart pieChartCapacity;
    @FXML private BarChart<String, Number> barChartPopular;
    @FXML private GridPane statsGrid;
    @FXML private Label totalEventsLabel;
    @FXML private Label totalCapacityLabel;
    @FXML private Label totalReservationsLabel;
    @FXML private Label avgUtilizationLabel;

    // Workshop Dashboard components
    @FXML private BarChart<String, Number> barChartCategory;
    @FXML private PieChart pieChartRatingSum;
    @FXML private LineChart<String, Number> polarChartAverage;

    // Articles Dashboard components
    @FXML private PieChart pieChartArticle;
    @FXML private BarChart<String, Number> barChartCategorie;
    @FXML private BarChart<String, Number> barChartArticle;

    // User Stats components
    @FXML private PieChart rolePieChart;
    @FXML private PieChart statusPieChart;
    @FXML private Label totalRoleCount;
    @FXML private Label particulierCount;
    @FXML private Label professionnelCount;
    @FXML private Label totalStatusCount;
    @FXML private Label actifCount;
    @FXML private Label desactiveCount;

    // Commandes Stats components
    @FXML private Label totalVentesLabel;
    @FXML private Label ventesPayeesLabel;
    @FXML private PieChart pieChartVentes;
    @FXML private BarChart<String, Number> barChartVisa;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    // Tab management
    @FXML private TabPane mainTabPane;

    // Services
    private final EventService eventService;
    private final CoursService coursService;
    private final ArticleService articleService;
    private final UtilisateurService utilisateurService;
    private final CommandeService commandeService;

    public DashboardEventsController() {
        eventService = new EventService();
        coursService = new CoursService();
        articleService = new ArticleService();
        utilisateurService = new UtilisateurService();
        commandeService = new CommandeService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Initialize all tabs with their data
            loadEventsData();
            loadWorkshopData();
            loadArticlesData();
            loadUserStatsData();
            loadCommandesData();
        } catch (SQLException e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*
     * Events Dashboard Methods
     */
    private void loadEventsData() throws SQLException {
        loadCapacityUtilizationChart();
        loadPopularEventsChart();
        loadEventStatistics();
    }

    private void loadCapacityUtilizationChart() throws SQLException {
        List<Event> eventsUtilization = eventService.getEventsCapacityUtilization();

        // Group events by utilization percentage ranges
        Map<String, Integer> utilizationRanges = new HashMap<>();
        utilizationRanges.put("0-25%", 0);
        utilizationRanges.put("26-50%", 0);
        utilizationRanges.put("51-75%", 0);
        utilizationRanges.put("76-100%", 0);

        for (Event event : eventsUtilization) {
            int capacity = event.getCapacity();
            int utilized = capacity - event.getRemaining();

            if (capacity > 0) {
                double utilizationPercentage = (double) utilized / capacity * 100;

                if (utilizationPercentage <= 25) {
                    utilizationRanges.put("0-25%", utilizationRanges.get("0-25%") + 1);
                } else if (utilizationPercentage <= 50) {
                    utilizationRanges.put("26-50%", utilizationRanges.get("26-50%") + 1);
                } else if (utilizationPercentage <= 75) {
                    utilizationRanges.put("51-75%", utilizationRanges.get("51-75%") + 1);
                } else {
                    utilizationRanges.put("76-100%", utilizationRanges.get("76-100%") + 1);
                }
            }
        }

        // Create pie chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : utilizationRanges.entrySet()) {
            if (entry.getValue() > 0) {
                pieChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + " events)", entry.getValue()));
            }
        }

        pieChartCapacity.setData(pieChartData);
        pieChartCapacity.setTitle("Event Capacity Utilization");
        pieChartCapacity.setLegendVisible(true);
        pieChartCapacity.setLabelsVisible(true);

        // Add custom colors that are more distinct
        int colorIndex = 0;
        String[] pieColors = {
                "#e74c3c", // Red
                "#f39c12", // Orange
                "#2ecc71", // Green
                "#3498db"  // Blue
        };

        for (PieChart.Data data : pieChartData) {
            String color = pieColors[colorIndex % pieColors.length];
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
            colorIndex++;
        }
    }

    private void loadPopularEventsChart() throws SQLException {
        List<Event> popularEvents = eventService.getPopularEvents();
        // Limit to top 5 events for better visualization
        popularEvents = popularEvents.stream().limit(5).collect(Collectors.toList());

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Reservations");

        for (Event event : popularEvents) {
            int reservations = event.getCapacity() - event.getRemaining();
            String shortName = event.getName();
            // Trim long event names for better display
            if (shortName.length() > 15) {
                shortName = shortName.substring(0, 12) + "...";
            }
            series.getData().add(new XYChart.Data<>(shortName, reservations));
        }

        barChartPopular.getData().clear();
        barChartPopular.getData().add(series);
        barChartPopular.setTitle("Most Popular Events");
        barChartPopular.setAnimated(false);

        // Improve visibility of bar chart
        barChartPopular.setCategoryGap(30);
        barChartPopular.setBarGap(0);

        // Set minimum value for y-axis to make small values more visible
        NumberAxis yAxis = (NumberAxis) barChartPopular.getYAxis();
        yAxis.setAutoRanging(true);
        yAxis.setTickUnit(1);
        yAxis.setMinorTickVisible(false);
    }

    private void loadEventStatistics() throws SQLException {
        List<Event> allEvents = eventService.getEventsCapacityUtilization();

        int totalEvents = allEvents.size();
        int totalCapacity = allEvents.stream().mapToInt(Event::getCapacity).sum();
        int totalReservations = allEvents.stream()
                .mapToInt(event -> event.getCapacity() - event.getRemaining())
                .sum();

        double avgUtilization = totalCapacity > 0 ?
                (double) totalReservations / totalCapacity * 100 : 0;

        totalEventsLabel.setText(String.valueOf(totalEvents));
        totalCapacityLabel.setText(String.valueOf(totalCapacity));
        totalReservationsLabel.setText(String.valueOf(totalReservations));
        avgUtilizationLabel.setText(String.format("%.1f%%", avgUtilization));
    }

    /*
     * Workshop Dashboard Methods
     */
    private void loadWorkshopData() {
        try {
            fillBarChartCategory();
            fillPieChartRatingSum();
            fillPolarChartAverage();
        } catch (SQLException e) {
            System.err.println("Error loading workshop data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void fillBarChartCategory() throws SQLException {
        Map<String, Integer> stats = coursService.getWorkshopCountByCategory();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Workshops");

        stats.forEach((category, count) -> {
            series.getData().add(new XYChart.Data<>(category, count));
        });

        barChartCategory.getData().clear();
        barChartCategory.getData().add(series);
    }

    private void fillPieChartRatingSum() throws SQLException {
        Map<String, Integer> pieData = coursService.getRatingSumByCategory();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<String, Integer> entry : pieData.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        pieChartRatingSum.setData(pieChartData);
    }

    private void fillPolarChartAverage() throws SQLException {
        Map<String, Double> avgMap = coursService.getAverageRatingByWorkshop();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Moyenne");

        avgMap.forEach((workshop, avg) -> {
            series.getData().add(new XYChart.Data<>(workshop, avg));
        });

        polarChartAverage.getData().clear();
        polarChartAverage.getData().add(series);
    }

    /*
     * Articles Dashboard Methods
     */
    private void loadArticlesData() {
        try {
            loadPieChartArticle();
            loadBarChartCategorie();
            loadBarChartArticle();
        } catch (SQLException e) {
            System.err.println("Error loading articles data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPieChartArticle() throws SQLException {
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

        pieChartArticle.setData(pieChartData);
        pieChartArticle.setLegendVisible(true);
        pieChartArticle.setLabelsVisible(true);
    }

    private void loadBarChartCategorie() throws SQLException {
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
    }

    private void loadBarChartArticle() throws SQLException {
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
    }

    private String generateRgbColor(int index, int total) {
        float hue = (index * 360.0f / total) % 360;
        Color color = Color.getHSBColor(hue / 360, 0.7f, 0.9f);
        return String.format("rgb(%d, %d, %d)", color.getRed(), color.getGreen(), color.getBlue());
    }

    /*
     * User Statistics Methods
     */
    private void loadUserStatsData() {
        loadRoleStatistics();
        loadStatusStatistics();
    }

    private void loadRoleStatistics() {
        try {
            int particuliers = utilisateurService.getCountByRole("ROLE_USER");
            int professionnels = utilisateurService.getCountByRole("ROLE_PROFESSIONNEL");

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Particuliers", particuliers),
                    new PieChart.Data("Professionnels", professionnels)
            );
            rolePieChart.setData(pieChartData);

            totalRoleCount.setText(String.valueOf(particuliers + professionnels));
            particulierCount.setText(String.valueOf(particuliers));
            professionnelCount.setText(String.valueOf(professionnels));
        } catch (Exception e) {
            System.err.println("Error loading role statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadStatusStatistics() {
        try {
            int actifs = utilisateurService.getCountByStatus(true);
            int desactives = utilisateurService.getCountByStatus(false);

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Actifs", actifs),
                    new PieChart.Data("Désactivés", desactives)
            );
            statusPieChart.setData(pieChartData);

            totalStatusCount.setText(String.valueOf(actifs + desactives));
            actifCount.setText(String.valueOf(actifs));
            desactiveCount.setText(String.valueOf(desactives));
        } catch (Exception e) {
            System.err.println("Error loading status statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*
     * Commandes Statistics Methods
     */
    private void loadCommandesData() {
        afficherStatistiquesVentes();
        afficherBarChartPaiement();
        afficherStatistiques();
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
        Map<LocalDate, Double> paiementsParJour = new TreeMap<>();

        LocalDate aujourdHui = LocalDate.now();
        LocalDate ilYA30Jours = aujourdHui.minusDays(29);

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

    @FXML
    private void refreshDashboard() {
        try {
            // Refresh all dashboard data
            loadEventsData();
            loadWorkshopData();
            loadArticlesData();
            loadUserStatsData();
            loadCommandesData();
        } catch (SQLException e) {
            System.err.println("Error refreshing dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void switchTab(int tabIndex) {
        if (mainTabPane != null && tabIndex >= 0 && tabIndex < mainTabPane.getTabs().size()) {
            mainTabPane.getSelectionModel().select(tabIndex);
        }
    }
}