package controllers.Events;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class MapPickerController {

    @FXML
    private WebView mapView;
    @FXML
    private Label selectedLocationLabel;

    private double selectedLat = 0;
    private double selectedLon = 0;

    @FXML
    public void initialize() {
        WebEngine webEngine = mapView.getEngine();
        String mapHtml = "<html><head>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.3/dist/leaflet.css'/>"
                + "<script src='https://unpkg.com/leaflet@1.9.3/dist/leaflet.js'></script>"
                + "</head><body style='margin:0;'>"
                + "<div id='map' style='width:100%;height:100%;'></div>"
                + "<script>"
                + "var map = L.map('map').setView([36.8065, 10.1815], 7);" // Tunis par défaut
                + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {maxZoom: 19}).addTo(map);"
                + "var marker;"
                + "map.on('click', function(e) {"
                + "  if (marker) { map.removeLayer(marker); }"
                + "  marker = L.marker(e.latlng).addTo(map);"
                + "  var lat = e.latlng.lat.toFixed(6);"
                + "  var lon = e.latlng.lng.toFixed(6);"
                + "  window.javaConnector.sendCoordinates(lat, lon);"
                + "});"
                + "</script>"
                + "</body></html>";

        webEngine.loadContent(mapHtml);

        // Connect JavaScript events to Java
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState.toString().equals("SUCCEEDED")) {
                mapView.getEngine().executeScript("window.javaConnector = {"
                        + "sendCoordinates: function(lat, lon) {"
                        + "    javafx.scene.web.WebEngine javaEngine = javafx.scene.web.WebEngine.getEngine();"
                        + "}"
                        + "};");
            }
        });

        mapView.getEngine().setOnAlert(event -> {
            String data = event.getData();
            if (data != null && data.contains(",")) {
                String[] parts = data.split(",");
                selectedLat = Double.parseDouble(parts[0]);
                selectedLon = Double.parseDouble(parts[1]);
                selectedLocationLabel.setText("Lieu sélectionné : " + selectedLat + ", " + selectedLon);
            }
        });
    }

    @FXML
    private void handleConfirmLocation() {
        if (selectedLat != 0 && selectedLon != 0) {
            MapPickerResult.selectedCoordinates = selectedLat + "," + selectedLon;
        } else {
            MapPickerResult.selectedCoordinates = "";
        }
        Stage stage = (Stage) mapView.getScene().getWindow();
        stage.close();
    }
}
