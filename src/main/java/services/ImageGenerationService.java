package services;

// OkHttp 5.0.0-alpha imports
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.HttpUrl;
import okio.BufferedSink;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
public class ImageGenerationService {

    private static final String API_URL = "https://modelslab.com/api/v6/realtime/text2img";
    //    private static final String API_KEY = "KTTjl9EdaZEq1uv4jrdIJ6nl9ujFCfvEeALRDHgUwSLdXASyclel90zzPO5o";

    public static String generateImage(String prompt, String savePath) {
        try {
            // Create OkHttpClient instance
            OkHttpClient client = new OkHttpClient().newBuilder().build();

            // Construct the request body as a JSON object
            MediaType mediaType = MediaType.parse("application/json");
            String requestBodyContent = "{\n" +
                    //"  \"key\": \"" + API_KEY + "\",\n" +
                    "  \"prompt\": \"" + prompt + "\",\n" +
                    "  \"negative_prompt\": \"bad quality\",\n" +
                    "  \"width\": \"512\",\n" +
                    "  \"height\": \"512\",\n" +
                    "  \"safety_checker\": false,\n" +
                    "  \"seed\": 2345,\n" +
                    "  \"sample\": 1,\n" +
                    "  \"webhook\": \"\",\n" +
                    "  \"track_id\": 20\n}";

            RequestBody body = RequestBody.create(mediaType, requestBodyContent);

            // Build the request
            Request request = new Request.Builder()
                    .url(API_URL)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // Execute the request
            Response response = client.newCall(request).execute();

            // Check response status
            if (response.code() != 200) {
                throw new RuntimeException("Failed: HTTP error code: " + response.code());
            }

            // Parse the response body
            String responseBody = response.body().string();
            System.out.println("Modelslab API response: " + responseBody);

            // Check if 'output' key is present instead of 'images'
            if (!responseBody.contains("output")) {
                throw new RuntimeException("Modelslab API response missing 'output' key");
            }

            // Extract the image URL from the response (based on the output key)
            String imageUrl = responseBody.split("\"output\":\\[\"")[1].split("\"\\]")[0].replace("\\/", "/");
            Files.createDirectories(Paths.get("uploads"));

            // Download and save the image
            try (InputStream in = new URL(imageUrl).openStream();
                 OutputStream out = new FileOutputStream(savePath)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            return savePath;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Image generation failed: " + e.getMessage());
        }
    }
}
