package services;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ChatService {
    // Modèle API URL
    private static final String CHAT_MODEL_API_URL = "https://api-inference.huggingface.co/models/mistralai/Mixtral-8x7B-Instruct-v0.1";
    private static final String API_KEY = "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"; // Remplacez par votre clé API

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

            // Création du prompt avec instruction sur le recyclage
            String recyclingPrompt =
                    "Tu es un assistant spécialisé dans le recyclage pour l'application Recyconnect. " +
                            "Si la question est sur le recyclage, l'écologie, la gestion des déchets ou le développement durable, " +
                            "réponds normalement. " +
                            "Si la question n'est pas liée au recyclage, réoriente poliment la conversation vers un aspect du recyclage. " +
                            "Pour des salutations ou questions de base comme 'bonjour', 'comment ça va', réponds normalement tout en " +
                            "mentionnant brièvement ton intérêt pour le recyclage. " +
                            "Quand on te demande ce qu'est Recyconnect ou ce que l'application propose, réponds que: 'Recyconnect est une application " +
                            "destinée à établir un lien efficace entre différents acteurs tels que professionnels et particuliers. " +
                            "L'objectif principal est de promouvoir la réutilisation et le recyclage en facilitant l'échange de produits, " +
                            "la publication de demandes et la sensibilisation via des événements et formations.' " +
                            "Sois toujours amical et naturel dans tes réponses. " +
                            "Question: " + message;

            // Format avec template de chat et instruction sur le recyclage
            String inputJson = "{\"inputs\": \"<s>[INST] " + recyclingPrompt + " [/INST]\", \"use_chat_template\": true}";

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
                    return "⚠️ Je n'ai pas pu générer de réponse. Parlons de recyclage!";
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
}