package services;

import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PaymeeService {

    public static Map<String, String> createPayment(double amount, String note, String firstName, String lastName, String email, String phone, String webhookUrl, String orderId) throws Exception {
        URL url = new URL("https://sandbox.paymee.tn/api/v2/payments/create");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Token 0d1057005686148c3a26dd6d1aa6f89591b01784");
        connection.setDoOutput(true);

        JSONObject json = new JSONObject();
        String amountStr = String.format(Locale.US, "%.2f", amount);
        json.put("amount", amountStr);
        json.put("note", note);
        json.put("order_id", orderId);
        json.put("first_name", firstName);
        json.put("last_name", lastName);
        json.put("email", email);
        json.put("phone", phone);
        json.put("webhook_url", webhookUrl);
        json.put("return_url", "https://yourdomain.com/success");
        json.put("cancel_url", "https://yourdomain.com/cancel");

        String jsonInputString = json.toString();

        System.out.println("---- CONTENU DE LA REQUÊTE ENVOYÉE À PAYMEE ----");
        System.out.println(jsonInputString);
        System.out.println("------------------------------------------------");

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        InputStream responseStream = (responseCode == HttpURLConnection.HTTP_OK)
                ? connection.getInputStream()
                : connection.getErrorStream();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(responseStream, "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            JSONObject responseObject = new JSONObject(response.toString());

            System.out.println("---- RÉPONSE PAYMEE ----");
            System.out.println(responseObject.toString(4));
            System.out.println("------------------------");

            if (responseObject.has("status") && responseObject.getBoolean("status") && responseObject.has("data")) {
                JSONObject data = responseObject.getJSONObject("data");

                Map<String, String> paymentData = new HashMap<>();
                paymentData.put("payment_url", data.optString("payment_url", ""));
                paymentData.put("payment_id", data.optString("payment_id", ""));
                paymentData.put("payment_token", data.optString("payment_token", ""));

                return paymentData;
            } else {
                String errorMsg = responseObject.optString("message", "Réponse invalide ou champ 'data' absent.");
                throw new Exception("Erreur Paymee : " + errorMsg + "\nRéponse brute : " + responseObject.toString(2));
            }
        }
    }
}
