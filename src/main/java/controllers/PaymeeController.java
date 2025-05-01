package controllers;

import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class PaymeeController {

    @FXML
    private WebView webView;

    public void init(String paymentUrl) {
        // Load the URL into the WebView
        WebEngine webEngine = webView.getEngine();  // Corrected to use the instance of webView
        webEngine.load(paymentUrl);
    }
}
