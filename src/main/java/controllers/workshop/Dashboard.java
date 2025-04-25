package controllers.workshop;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import services.CoursService;

import java.sql.SQLException;
import java.util.Map;

public class Dashboard {

    @FXML private BarChart<String, Number> barChartCategory;
    @FXML private PieChart pieChartRatingSum;
    @FXML private LineChart<String, Number> polarChartAverage;

    private final CoursService coursService = new CoursService();

    @FXML
    private void initialize() {
        try {
            fillBarChart();
            fillPieChart();
            fillPolarChart();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fillBarChart() throws SQLException {
        Map<String, Integer> stats = coursService.getWorkshopCountByCategory();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Workshops");

        stats.forEach((category, count) -> {
            series.getData().add(new XYChart.Data<>(category, count));
        });

        barChartCategory.getData().add(series);
    }

    private void fillPieChart() throws SQLException {
        Map<String, Integer> pieData = coursService.getRatingSumByCategory();

        for (Map.Entry<String, Integer> entry : pieData.entrySet()) {
            pieChartRatingSum.getData().add(
                    new PieChart.Data(entry.getKey(), entry.getValue())
            );
        }
    }

    private void fillPolarChart() throws SQLException {
        Map<String, Double> avgMap = coursService.getAverageRatingByWorkshop();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Moyenne");

        avgMap.forEach((workshop, avg) -> {
            series.getData().add(new XYChart.Data<>(workshop, avg));
        });

        polarChartAverage.getData().add(series);
    }
}
