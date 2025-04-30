package services;

import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class PaymeeService {

    public static Map<String, String> createPayment(double amount, String note, String firstName, String lastName, String email, String phone, String webhookUrl, String orderId) throws Exception {
        URL url = new URL("https://sandbox.paymee.tn/api/v2/payments/create");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Token 0d1057005686148c3a26dd6d1aa6f89591b01784");
        connection.setDoOutput(true);

        // ⚠️ Le champ "vendor" doit être une chaîne, même s'il contient un nombre
        JSONObject json = new JSONObject();
        json.put("vendor", 3666);
        json.put("amount", amount); // double est accepté
        json.put("currency", "TND");
        json.put("note", note);
        json.put("order_id", orderId);
        json.put("webhook_url", "https://yourdomain.com/webhook");

        // ✅ Structure correcte du client
        JSONObject customer = new JSONObject();
        customer.put("first_name", firstName);
        customer.put("last_name", lastName);
        customer.put("email", email);
        customer.put("phone", phone);
        json.put("customer", customer);

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

            if (responseCode == HttpURLConnection.HTTP_OK && responseObject.getBoolean("status")) {
                JSONObject data = responseObject.getJSONObject("data");

                Map<String, String> paymentData = new HashMap<>();
                paymentData.put("payment_url", data.getString("payment_url"));
                paymentData.put("payment_id", data.getString("payment_id"));
                paymentData.put("payment_token", data.getString("payment_token"));

                return paymentData;
            } else {
                throw new Exception("Erreur lors de la création du paiement Paymee: " + responseObject.optString("message", "Réponse invalide"));
            }
        }
    }
}
