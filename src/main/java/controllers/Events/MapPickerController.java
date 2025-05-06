package controllers.Events;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class MapPickerController {

    @FXML
    private WebView mapView;
    @FXML
    private Label selectedLocationLabel;
    @FXML
    private Label addressLabel;
    @FXML
    private Button confirmButton;

    private double selectedLat = 0;
    private double selectedLon = 0;
    private String address = "";
    private boolean locationSelected = false;

    @FXML
    public void initialize() {
        WebEngine webEngine = mapView.getEngine();
        String mapHtml = createMapHtml();
        webEngine.loadContent(mapHtml);

        // Disable confirm button initially
        confirmButton.setDisable(true);

        // Connect JavaScript to Java after the page loads
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaConnector", new JavaConnector());
            }
        });
    }

    private String createMapHtml() {
        return "<html><head>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.3/dist/leaflet.css'/>"
                + "<script src='https://unpkg.com/leaflet@1.9.3/dist/leaflet.js'></script>"
                + "<style>body, html, #map { width: 100%; height: 100%; margin: 0; padding: 0; }</style>"
                + "</head><body>"
                + "<div id='map'></div>"
                + "<script>"
                + "var map = L.map('map').setView([36.8065, 10.1815], 10);" // Default center (Tunis)
                + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {maxZoom: 19}).addTo(map);"
                + "var marker;"

                // Click event handler
                + "map.on('click', function(e) {"
                + "  if (marker) { map.removeLayer(marker); }"
                + "  marker = L.marker(e.latlng).addTo(map);"
                + "  var lat = e.latlng.lat.toFixed(6);"
                + "  var lng = e.latlng.lng.toFixed(6);"

                // Always update coordinates immediately - don't wait for geocoding
                + "  window.javaConnector.updateCoordinates(lat, lng);"

                // Then try reverse geocoding with Nominatim
                + "  try {"
                + "    fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=18&addressdetails=1`)"
                + "      .then(response => response.json())"
                + "      .then(data => {"
                + "        var address = data.display_name || 'Unknown location';"
                + "        window.javaConnector.updateAddress(address);"
                + "      })"
                + "      .catch(error => {"
                + "        console.error('Error fetching address:', error);"
                + "        window.javaConnector.updateAddress('Address lookup failed');"
                + "      });"
                + "  } catch (e) {"
                + "    console.error('Exception in geocoding:', e);"
                + "    window.javaConnector.updateAddress('Address lookup failed');"
                + "  }"
                + "});"
                + "</script>"
                + "</body></html>";
    }

    @FXML
    private void handleConfirmLocation() {
        if (locationSelected) {
            MapPickerResult.selectedCoordinates = selectedLat + "," + selectedLon;
            MapPickerResult.selectedAddress = address;
            Stage stage = (Stage) mapView.getScene().getWindow();
            stage.close();
        } else {
            selectedLocationLabel.setText("Veuillez sélectionner un emplacement sur la carte");
            selectedLocationLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleCancel() {
        MapPickerResult.selectedCoordinates = "";
        MapPickerResult.selectedAddress = "";
        Stage stage = (Stage) mapView.getScene().getWindow();
        stage.close();
    }

    // This class provides the bridge between JavaScript and Java
    public class JavaConnector {
        public void updateCoordinates(String lat, String lon) {
            selectedLat = Double.parseDouble(lat);
            selectedLon = Double.parseDouble(lon);
            locationSelected = true;

            // Update the UI on the JavaFX thread
            javafx.application.Platform.runLater(() -> {
                selectedLocationLabel.setText("Coordonnées sélectionnées: " + lat + ", " + lon);
                selectedLocationLabel.setStyle("-fx-text-fill: green;");

                // Enable the confirm button
                confirmButton.setDisable(false);
            });
        }

        public void updateAddress(String addressText) {
            address = addressText;

            // Update the UI on the JavaFX thread
            javafx.application.Platform.runLater(() -> {
                addressLabel.setText("Adresse: " + addressText);
            });
        }
    }
}