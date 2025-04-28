package controllers;

import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class PaymeeController {

    @FXML
    private WebView webview;

    public void init(String paymentToken) {
        String iframeHtml = "<html><body style='margin:0;padding:0;'>" +
                "<iframe src='https://sandbox.paymee.tn/gateway/" + paymentToken +
                "' width='100%' height='100%' frameborder='0' allowfullscreen>" +
                "</iframe></body></html>";

        WebEngine engine = webview.getEngine();
        engine.loadContent(iframeHtml, "text/html");
    }

}
