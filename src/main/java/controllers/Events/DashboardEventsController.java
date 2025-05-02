package controllers.Events;

import entities.Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import services.EventService;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DashboardEventsController implements Initializable {

    @FXML private PieChart pieChartCapacity;
    @FXML private BarChart<String, Number> barChartPopular;
    @FXML private GridPane statsGrid;
    @FXML private Label totalEventsLabel;
    @FXML private Label totalCapacityLabel;
    @FXML private Label totalReservationsLabel;
    @FXML private Label avgUtilizationLabel;

    private EventService eventService;

    public DashboardEventsController() {
        eventService = new EventService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            loadDashboardData();
        } catch (SQLException e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDashboardData() throws SQLException {
        loadCapacityUtilizationChart();
        loadPopularEventsChart();
        loadStatistics();
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

    private void loadStatistics() throws SQLException {
        List<Event> allEvents = eventService.getEventsCapacityUtilization();
        List<Event> popularEvents = eventService.getPopularEvents();

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

    @FXML
    private void refreshDashboard() {
        try {
            loadDashboardData();
        } catch (SQLException e) {
            System.err.println("Error refreshing dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
}