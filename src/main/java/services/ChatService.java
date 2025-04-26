package services;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ChatService {
    // Choisissez un des modèles suggérés
    private static final String CHAT_MODEL_API_URL = "https://api-inference.huggingface.co/models/mistralai/Mixtral-8x7B-Instruct-v0.1";
    private static final String API_KEY = "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";

    public static String sendMessage(String message) {
        try {
            // Établissement de la connexion
            URL url = new URL(CHAT_MODEL_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000); // 30 secondes timeout
            conn.setReadTimeout(30000);

            // Format avec template de chat et instruction explicite
            String inputJson = "{\"inputs\": \"<s>[INST] Réponds directement sans répéter la question. " + message + " [/INST]\", \"use_chat_template\": true}";



            // Envoi de la requête
            try (OutputStream os = conn.getOutputStream()) {
                os.write(inputJson.getBytes("UTF-8"));
                os.flush();
            }

            // Vérification du code de réponse
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                Scanner errorScanner = new Scanner(conn.getErrorStream());
                StringBuilder errorResponse = new StringBuilder();
                while (errorScanner.hasNext()) {
                    errorResponse.append(errorScanner.nextLine());
                }
                errorScanner.close();
                return "❌ Erreur: Code " + responseCode + " - " + errorResponse.toString();
            }

            // Lecture de la réponse
            Scanner scanner = new Scanner(conn.getInputStream(), "UTF-8");
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            // Traitement de la réponse
            String fullResponse = response.toString();

            // Extraction du texte généré
            int start = fullResponse.indexOf("\"generated_text\":\"") + 18;
            int end = fullResponse.lastIndexOf("\"");

            if (start != -1 && end != -1 && end > start) {
                String extractedText = fullResponse.substring(start, end)
                        .replace("\\n", "\n")
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\");

                // Nettoyage complet des balises et textes répétés
                extractedText = extractedText
                        // Supprime les balises INST avec contenu
                        .replaceAll("<s>\\s*\\[INST\\].*?\\[/INST\\]", "")
                        // Supprime toutes les balises
                        .replaceAll("<s>|</s>|\\[INST\\]|\\[/INST\\]", "")
                        // Supprime les indicateurs User/Assistant
                        .replaceAll("(?i)User:\\s*" + Pattern.quote(message) + "\\s*Assistant:", "")
                        .replaceAll("(?i)User:|Assistant:", "")
                        .trim();

                // Si la réponse commence toujours par le message d'origine
                if (extractedText.startsWith(message)) {
                    extractedText = extractedText.substring(message.length()).trim();
                }

                // Supprime les réponses vides ou trop courtes
                if (extractedText.isEmpty() || extractedText.length() < 2) {
                    return "⚠️ Le modèle n'a pas généré de réponse valide.";
                }

                // Suppression des caractères de contrôle indésirables
                extractedText = extractedText.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

                return extractedText;
            } else {
                // Si on ne trouve pas le format attendu, retourne la réponse brute
                return "✅ " + fullResponse.replace("\\n", "\n").replace("\\\"", "\"");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Erreur: " + e.getMessage();
        }
    }

    public static String sendMessageAlternative(String message) {
        try {
            URL url = new URL(CHAT_MODEL_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Format avec wait_for_model pour éviter les timeouts
            String inputJson = "{\"inputs\": \"" + message + "\", \"wait_for_model\": true}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(inputJson.getBytes("UTF-8"));
                os.flush();
            }

            // Vérifie si la réponse est un stream ou un objet JSON standard
            String contentType = conn.getHeaderField("Content-Type");

            if (contentType != null && contentType.contains("text/event-stream")) {
                // Traitement des réponses en streaming
                Scanner scanner = new Scanner(conn.getInputStream(), "UTF-8");
                StringBuilder finalResponse = new StringBuilder();

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        // Traitement des données de streaming
                        if (!data.equals("[DONE]")) {
                            try {
                                // Extraction du texte généré du stream
                                int textStart = data.indexOf("\"text\":\"") + 8;
                                int textEnd = data.indexOf("\"", textStart);
                                if (textStart != -1 && textEnd != -1) {
                                    finalResponse.append(data.substring(textStart, textEnd));
                                }
                            } catch (Exception e) {
                                // Ignore les erreurs de parsing
                            }
                        }
                    }
                }
                scanner.close();

                return finalResponse.toString().trim();
            } else {
                // Traitement standard
                Scanner scanner = new Scanner(conn.getInputStream(), "UTF-8");
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();

                // Extraction et nettoyage
                String fullResponse = response.toString();
                int start = fullResponse.indexOf("\"generated_text\":\"") + 18;
                int end = fullResponse.lastIndexOf("\"");

                if (start != -1 && end != -1 && end > start) {
                    String extractedText = fullResponse.substring(start, end)
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\");

                    // Nettoyage complet des balises et textes répétés
                    extractedText = extractedText
                            .replaceAll("<s>\\s*\\[INST\\].*?\\[/INST\\]", "")
                            .replaceAll("<s>|</s>|\\[INST\\]|\\[/INST\\]", "")
                            .replaceAll("(?i)User:\\s*" + Pattern.quote(message) + "\\s*Assistant:", "")
                            .replaceAll("(?i)User:|Assistant:", "")
                            .trim();

                    if (extractedText.startsWith(message)) {
                        extractedText = extractedText.substring(message.length()).trim();
                    }

                    return extractedText;
                } else {
                    return "✅ " + fullResponse.replace("\\n", "\n").replace("\\\"", "\"");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Erreur: " + e.getMessage();
        }
    }
}