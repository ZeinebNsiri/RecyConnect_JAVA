package controllers;

import services.ImageGenerationService;

public class ImageGenerationTest {
    public static void main(String[] args) {
        String prompt = "A professional event image for: Conference on AI.";
        String savePath = "test_image.png";

        try {
            String generatedImage = ImageGenerationService.generateImage(prompt, savePath);
            System.out.println("Image saved to: " + generatedImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
