package controllers;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;

public class PaymeeController {

    @FXML
    private WebView webView;

    public void init(String paymentUrl) {
        webView.getEngine().load(paymentUrl);
    }

}
