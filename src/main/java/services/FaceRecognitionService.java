package services;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import java.io.File;

public class FaceRecognitionService {

    private final String apiKey = "fk-UMlb26ifZ0HwIsFcAVrwsGrjZKGaJ";
    private final String apiSecret = "jczs3sFSEpH0Q1kr3JbrIIavz-uF-DN_";

    public String detectFace(File imageFile) {
        HttpResponse<JsonNode> response = Unirest.post("https://api-us.faceplusplus.com/facepp/v3/detect")
                .field("api_key", apiKey)
                .field("api_secret", apiSecret)
                .field("image_file", imageFile)
                .field("return_landmark", "0")
                .field("return_attributes", "none")
                .asJson();

        if (response.getStatus() == 200) {
            System.out.println(response.getBody().toString());
            return response.getBody().getObject()
                    .getJSONArray("faces")
                    .getJSONObject(0)
                    .getString("face_token");
        }
        System.out.println(response.getStatus());
        return null;
    }
    public boolean compareFaces(String faceToken1, File newImage) {
        HttpResponse<JsonNode> response = Unirest.post("https://api-us.faceplusplus.com/facepp/v3/compare")
                .field("api_key", apiKey)
                .field("api_secret", apiSecret)
                .field("face_token1", faceToken1)
                .field("image_file2", newImage)
                .asJson();
        System.out.println(response.getStatus());
        if (response.getStatus() == 200) {
            System.out.println(200);
            double confidence = response.getBody().getObject().getDouble("confidence");
            System.out.println(confidence);
            return confidence >= 80.0;
        }

        return false;
    }
}
