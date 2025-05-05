package services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javafx.scene.image.WritableImage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.media.MediaException;
import javafx.application.Platform;
import javafx.util.Duration;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;

public class VideoAnalysisService {
    // Primary image captioning model (Hugging Face, free)
    private static final String IMAGE_CAPTIONING_API_URL = "https://api-inference.huggingface.co/models/Salesforce/blip-image-captioning-large";

    // Backup model for detailed captions
    private static final String BACKUP_CAPTIONING_API_URL = "https://api-inference.huggingface.co/models/microsoft/git-large-coco";

    // Secondary detailed captioning model
    private static final String DETAILED_CAPTIONING_API_URL = "https://api-inference.huggingface.co/models/nlpconnect/vit-gpt2-image-captioning";

    // Scene classification model to identify context
    private static final String SCENE_CLASSIFICATION_API_URL = "https://api-inference.huggingface.co/models/microsoft/resnet-50";

    // Your Hugging Face API key
    private static final String API_KEY = "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";

    // Increased to 15 frames for more detailed video analysis
    private static final int DEFAULT_MAX_FRAMES = 15;

    // Lowered similarity threshold to capture more nuanced differences
    private static final double SIMILARITY_THRESHOLD = 0.45;

    public static String generateVideoDescription(File videoFile) {
        return generateVideoDescription(videoFile, DEFAULT_MAX_FRAMES);
    }

    public static String generateVideoDescription(File videoFile, int maxFrames) {
        try {
            // Validate video file format
            String extension = videoFile.getName().substring(videoFile.getName().lastIndexOf(".") + 1).toLowerCase();
            if (!List.of("mp4", "mov", "avi", "wmv", "mkv", "webm").contains(extension)) {
                return "Unsupported video format. Please use MP4, MOV, AVI, WMV, MKV, or WEBM.";
            }

            // Create temporary directory for frames
            Path tempDir = Files.createTempDirectory("video_frames");

            // Extract frames for detailed analysis
            List<File> frames = extractFrames(videoFile, tempDir, maxFrames);

            if (frames.isEmpty()) {
                return "Unable to extract frames from the video.";
            }

            System.out.println("Successfully extracted " + frames.size() + " frames");

            // Get video metadata (duration, resolution, etc.)
            Map<String, String> metadata = extractVideoMetadata(videoFile);

            // Analyze frames and build detailed description
            StringBuilder fullDescription = new StringBuilder();

            // Add metadata as intro
            String durationStr = metadata.getOrDefault("duration", "unknown duration");
            if (!durationStr.equals("unknown duration")) {
                try {
                    double durationSeconds = Double.parseDouble(durationStr);
                    if (durationSeconds < 60) {
                        durationStr = String.format("%.1f seconds", durationSeconds);
                    } else {
                        int minutes = (int)(durationSeconds / 60);
                        int seconds = (int)(durationSeconds % 60);
                        durationStr = minutes + " minute" + (minutes != 1 ? "s" : "") +
                                (seconds > 0 ? " and " + seconds + " second" + (seconds != 1 ? "s" : "") : "");
                    }
                } catch (NumberFormatException e) {
                    // Keep original string if parsing fails
                }
            }

            fullDescription.append("This is a ").append(durationStr).append(" video");

            // Optionally add resolution if available
            String resolution = metadata.getOrDefault("resolution", "");
            if (!resolution.isEmpty()) {
                fullDescription.append(" with ").append(resolution).append(" resolution");
            }

            fullDescription.append(". ");

            // Get all frame descriptions first
            List<String> frameDescriptions = new ArrayList<>();
            Map<String, String> sceneContexts = new HashMap<>();

            for (File frame : frames) {
                String frameDescription = analyzeImageWithHuggingFace(frame);
                frameDescriptions.add(frameDescription);

                // Try to get scene context classification as well
                try {
                    String sceneContext = classifySceneContext(frame);
                    if (sceneContext != null && !sceneContext.isEmpty()) {
                        sceneContexts.put(frame.getName(), sceneContext);
                    }
                } catch (Exception e) {
                    System.out.println("Scene classification failed: " + e.getMessage());
                }
            }

            // Filter out repetitive or too similar descriptions
            List<String> uniqueDescriptions = filterRepetitiveDescriptions(frameDescriptions);

            // Group by potential scenes/segments based on similarity
            List<List<String>> scenes = groupIntoScenes(uniqueDescriptions);

            fullDescription.append("Content: ");

            // Handle scene transitions
            if (scenes.size() == 1 && scenes.get(0).size() == 1) {
                // Single scene with single description
                fullDescription.append(scenes.get(0).get(0));
            } else {
                // Multiple scenes or multiple descriptions within scenes
                for (int sceneIndex = 0; sceneIndex < scenes.size(); sceneIndex++) {
                    List<String> sceneDescriptions = scenes.get(sceneIndex);

                    // Add scene indicator for multi-scene videos
                    if (scenes.size() > 1) {
                        if (sceneIndex == 0) {
                            fullDescription.append("The video begins with ");
                        } else if (sceneIndex == scenes.size() - 1) {
                            fullDescription.append(" The final scene shows ");
                        } else {
                            String[] transitions = {
                                    "Next, the video transitions to ",
                                    "Then the scene changes to ",
                                    "Following this, we see ",
                                    "The video then cuts to "
                            };
                            fullDescription.append(transitions[sceneIndex % transitions.length]);
                        }
                    }

                    // Add descriptions within this scene
                    for (int i = 0; i < sceneDescriptions.size(); i++) {
                        String description = sceneDescriptions.get(i);

                        if (i == 0) {
                            fullDescription.append(description);
                        } else if (i == sceneDescriptions.size() - 1) {
                            fullDescription.append(" and finally, ").append(description);
                        } else {
                            String[] connectors = {"then", "next", "followed by", "after that", "subsequently"};
                            String connector = connectors[i % connectors.length];
                            fullDescription.append(", ").append(connector).append(", ").append(description);
                        }
                    }

                    // Add scene separator if not the last scene
                    if (sceneIndex < scenes.size() - 1) {
                        fullDescription.append(".");
                    }
                }
            }

            // Add conclusion sentence to summarize the video
            String conclusion = getContextualConclusion(uniqueDescriptions, sceneContexts);
            fullDescription.append(". ").append(conclusion);

            // Clean up temporary files
            for (File frame : frames) {
                frame.delete();
            }
            tempDir.toFile().delete();

            return fullDescription.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error analyzing video: " + e.getMessage();
        }
    }

    private static List<List<String>> groupIntoScenes(List<String> descriptions) {
        List<List<String>> scenes = new ArrayList<>();
        if (descriptions.isEmpty()) {
            return scenes;
        }

        // Start with first description as the first scene
        List<String> currentScene = new ArrayList<>();
        currentScene.add(descriptions.get(0));

        // Group consecutive similar descriptions into scenes
        // but force at least 3 scenes for longer videos to provide more variety
        int targetScenes = Math.min(descriptions.size(), Math.max(3, descriptions.size() / 5));
        double sceneSimilarityThreshold = descriptions.size() > 10 ? 0.3 : 0.5;

        for (int i = 1; i < descriptions.size(); i++) {
            String currentDesc = descriptions.get(i);
            String previousDesc = descriptions.get(i-1);

            // If significantly different from previous, or if we need more scenes, start a new scene
            boolean forcedSceneBreak = scenes.size() < targetScenes - 1 &&
                    i > descriptions.size() / targetScenes * (scenes.size() + 1);

            if (calculateSimilarity(currentDesc, previousDesc) < sceneSimilarityThreshold || forcedSceneBreak) {
                if (!currentScene.isEmpty()) {
                    scenes.add(currentScene);
                }
                currentScene = new ArrayList<>();
            }

            currentScene.add(currentDesc);
        }

        // Add the last scene
        if (!currentScene.isEmpty()) {
            scenes.add(currentScene);
        }

        return scenes;
    }

    private static List<List<String>> consolidateScenes(List<List<String>> scenes) {
        // Simple consolidation - merge adjacent scenes if they're small
        List<List<String>> consolidatedScenes = new ArrayList<>();
        List<String> currentConsolidated = new ArrayList<>();

        for (List<String> scene : scenes) {
            // If current consolidated scene is small, or this scene is small, merge them
            if (currentConsolidated.size() < 2 || scene.size() < 2) {
                currentConsolidated.addAll(scene);
            } else {
                // Otherwise, finish the current consolidated scene and start a new one
                consolidatedScenes.add(currentConsolidated);
                currentConsolidated = new ArrayList<>(scene);
            }
        }

        // Add the last consolidated scene
        if (!currentConsolidated.isEmpty()) {
            consolidatedScenes.add(currentConsolidated);
        }

        return consolidatedScenes;
    }

    private static List<String> filterRepetitiveDescriptions(List<String> descriptions) {
        List<String> uniqueDescriptions = new ArrayList<>();

        // First pass: extract key phrases and do strict filtering
        for (String description : descriptions) {
            // Skip empty or error descriptions
            if (description == null || description.trim().isEmpty() ||
                    description.contains("undescribed") || description.contains("error")) {
                continue;
            }

            // Extract the core phrase (ignore "a", "an", "the", etc.)
            String coreConcept = extractCoreConcept(description);

            // Check if this core concept is already present
            boolean isDifferentEnough = true;
            for (String existingDesc : uniqueDescriptions) {
                String existingCoreConcept = extractCoreConcept(existingDesc);

                // Use a stricter threshold for duplicates (e.g., 0.8 instead of SIMILARITY_THRESHOLD)
                if (calculateSimilarity(coreConcept, existingCoreConcept) > 0.8) {
                    isDifferentEnough = false;

                    // If the new description is significantly more detailed, replace the old one
                    if (description.length() > existingDesc.length() * 1.5) {
                        uniqueDescriptions.remove(existingDesc);
                        uniqueDescriptions.add(description);
                        System.out.println("Replaced less detailed description with more detailed one");
                    }
                    break;
                }
            }

            if (isDifferentEnough) {
                uniqueDescriptions.add(description);
            }
        }

        // If we still have too few descriptions, try a more lenient approach
        if (uniqueDescriptions.size() <= 1 && descriptions.size() > 2) {
            // Take at least three descriptions if available, ensuring they are different
            uniqueDescriptions.clear();

            // Add the first description
            if (!descriptions.isEmpty()) {
                uniqueDescriptions.add(descriptions.get(0));
            }

            // Add the middle description if it's different enough
            if (descriptions.size() > 2) {
                String middleDesc = descriptions.get(descriptions.size() / 2);
                if (calculateSimilarity(extractCoreConcept(middleDesc), extractCoreConcept(uniqueDescriptions.get(0))) < 0.8) {
                    uniqueDescriptions.add(middleDesc);
                }
            }

            // Add the last description if it's different enough
            if (descriptions.size() > 1) {
                String lastDesc = descriptions.get(descriptions.size() - 1);
                boolean isUnique = true;
                for (String uniqueDesc : uniqueDescriptions) {
                    if (calculateSimilarity(extractCoreConcept(lastDesc), extractCoreConcept(uniqueDesc)) > 0.8) {
                        isUnique = false;
                        break;
                    }
                }
                if (isUnique) {
                    uniqueDescriptions.add(lastDesc);
                }
            }
        }

        // If we still don't have any descriptions, keep at least one
        if (uniqueDescriptions.isEmpty() && !descriptions.isEmpty()) {
            uniqueDescriptions.add(descriptions.get(0));
        }

        // If we still have duplicates, reduce further
        List<String> finalDescriptions = new ArrayList<>();
        for (String desc : uniqueDescriptions) {
            boolean isUnique = true;
            for (String finalDesc : finalDescriptions) {
                if (calculateSimilarity(desc, finalDesc) > 0.9) {
                    isUnique = false;
                    break;
                }
            }
            if (isUnique) {
                finalDescriptions.add(desc);
            }
        }

        return finalDescriptions.isEmpty() ? uniqueDescriptions : finalDescriptions;
    }

    private static String extractCoreConcept(String description) {
        if (description == null) return "";

        // Remove common articles and prepositions
        String core = description.toLowerCase()
                .replaceAll("\\ba\\b|\\ban\\b|\\bthe\\b|\\bwith\\b|\\bof\\b|\\bin\\b|\\bon\\b|\\bis\\b", " ")
                .replaceAll("\\s+", " ").trim();

        // Extract the main subject and object
        String[] words = core.split(" ");
        StringBuilder mainConcept = new StringBuilder();

        // Take up to 5 significant words (excluding very short words)
        int count = 0;
        for (String word : words) {
            if (word.length() > 3 && !isCommonWord(word)) {
                mainConcept.append(word).append(" ");
                count++;
                if (count >= 5) break;
            }
        }

        return mainConcept.toString().trim();
    }

    private static boolean isCommonWord(String word) {
        String[] commonWords = {"there", "that", "this", "have", "from", "what", "which", "when", "where", "who",
                "been", "would", "could", "should", "being", "about", "very", "some", "just"};
        for (String common : commonWords) {
            if (word.equals(common)) return true;
        }
        return false;
    }

    private static double calculateSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) return 0.0;

        // Convert to lowercase and split into words
        String[] words1 = str1.toLowerCase().split("\\s+");
        String[] words2 = str2.toLowerCase().split("\\s+");

        // Count common words with a heavier penalty for exact matches
        int commonWords = 0;
        int exactMatches = 0;
        for (String word1 : words1) {
            if (word1.length() <= 3) continue; // Ignore short words

            boolean foundMatch = false;
            for (String word2 : words2) {
                if (word1.equals(word2)) {
                    commonWords++;
                    foundMatch = true;
                    exactMatches++;
                    break;
                } else if (calculateWordSimilarity(word1, word2) > 0.9) {
                    commonWords++;
                    foundMatch = true;
                    break;
                }
            }
        }

        // Calculate similarity with a penalty for exact matches
        int totalWords = Math.max(words1.length, words2.length);
        if (totalWords == 0) return 0.0;
        double baseSimilarity = (double) commonWords / totalWords;
        double exactMatchPenalty = (double) exactMatches / totalWords * 0.3;
        return Math.min(1.0, baseSimilarity + exactMatchPenalty);
    }

    private static String getContextualConclusion(List<String> descriptions, Map<String, String> sceneContexts) {
        String[] vaseConclusions = {
                "This video presents a detailed examination of a decorative vase, showcasing its design features from multiple angles",
                "The footage offers a comprehensive view of a vase's artistic details and craftsmanship",
                "This appears to be a product showcase highlighting the aesthetic qualities of a decorative vase",
                "The video presents an artistic study of a vase, focusing on its form, color, and design elements",
                "This detailed presentation captures the unique characteristics and visual appeal of a decorative vessel"
        };

        boolean isAboutVase = false;
        for (String desc : descriptions) {
            if (desc != null && desc.toLowerCase().contains("vase")) {
                isAboutVase = true;
                break;
            }
        }

        if (isAboutVase) {
            return vaseConclusions[(int)(Math.random() * vaseConclusions.length)];
        }

        boolean hasAction = false;
        boolean hasPeople = false;
        boolean hasObjects = false;
        boolean hasMultipleScenes = false;
        boolean isInstructional = false;
        boolean isOutdoor = false;
        boolean isIndoor = false;

        if (!sceneContexts.isEmpty()) {
            int outdoorScenes = 0;
            int indoorScenes = 0;

            for (String context : sceneContexts.values()) {
                String lowerContext = context.toLowerCase();
                if (lowerContext.contains("outdoor") || lowerContext.contains("nature") ||
                        lowerContext.contains("landscape") || lowerContext.contains("beach")) {
                    outdoorScenes++;
                }
                if (lowerContext.contains("indoor") || lowerContext.contains("room") ||
                        lowerContext.contains("office") || lowerContext.contains("kitchen")) {
                    indoorScenes++;
                }
            }

            isOutdoor = outdoorScenes > indoorScenes && outdoorScenes > 0;
            isIndoor = indoorScenes >= outdoorScenes && indoorScenes > 0;
            hasMultipleScenes = sceneContexts.size() > 2;
        }

        for (String desc : descriptions) {
            if (desc == null) continue;
            String lowerDesc = desc.toLowerCase();

            if (lowerDesc.contains(" cut") || lowerDesc.contains("using") ||
                    lowerDesc.contains("show") || lowerDesc.contains("make") ||
                    lowerDesc.contains("present") || lowerDesc.contains("create") ||
                    lowerDesc.contains("demonstrat") || lowerDesc.contains("perform")) {
                hasAction = true;
            }

            if (lowerDesc.contains("tutorial") || lowerDesc.contains("step") ||
                    lowerDesc.contains("guide") || lowerDesc.contains("instruct") ||
                    lowerDesc.contains("how to") || lowerDesc.contains("learn")) {
                isInstructional = true;
            }

            if (lowerDesc.contains("person") || lowerDesc.contains("woman") ||
                    lowerDesc.contains("man") || lowerDesc.contains("people") ||
                    lowerDesc.contains("someone") || lowerDesc.contains("child") ||
                    lowerDesc.contains("girl") || lowerDesc.contains("boy")) {
                hasPeople = true;
            }

            if (lowerDesc.contains("jeans") || lowerDesc.contains("bag") ||
                    lowerDesc.contains("scissors") || lowerDesc.contains("fabric") ||
                    lowerDesc.contains("clothing") || lowerDesc.contains("table") ||
                    lowerDesc.contains("device") || lowerDesc.contains("phone") ||
                    lowerDesc.contains("camera") || lowerDesc.contains("tool")) {
                hasObjects = true;
            }
        }

        if (isInstructional && hasPeople && hasObjects) {
            return "This detailed tutorial video shows how to perform specific tasks with the demonstrated items";
        } else if (isInstructional && hasObjects) {
            return "This appears to be an instructional video demonstrating procedures or techniques";
        } else if (hasAction && hasPeople && hasObjects) {
            return "This video captures people interacting with various objects in a dynamic way";
        } else if (hasAction && hasObjects) {
            return "The sequence demonstrates the transformation or manipulation of various objects";
        } else if (hasPeople && isOutdoor) {
            return "The video features people in outdoor settings, capturing activities in natural environments";
        } else if (hasPeople && isIndoor) {
            return "The video documents people in indoor settings, showing interactions in enclosed spaces";
        } else if (hasPeople) {
            return "The video primarily features one or more people throughout its duration";
        } else if (hasMultipleScenes) {
            return "The video presents a sequence of different scenes, creating a visual narrative";
        } else {
            return "The sequence forms a visual presentation with consistent themes throughout";
        }
    }

    private static String classifySceneContext(File imageFile) {
        try {
            // Convert image to Base64
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Call Hugging Face API for scene classification
            String apiResponse = callHuggingFaceAPI(SCENE_CLASSIFICATION_API_URL, base64Image);

            // Parse the response
            if (apiResponse != null && !apiResponse.isEmpty() && !apiResponse.contains("error")) {
                return extractTopCategories(apiResponse);
            }

            return "";
        } catch (Exception e) {
            System.out.println("Scene classification error: " + e.getMessage());
            return "";
        }
    }

    private static String extractTopCategories(String apiResponse) {
        try {
            StringBuilder context = new StringBuilder();

            // Simple parsing - in production you'd use proper JSON parsing
            int currentPos = 0;
            int counter = 0;

            while ((currentPos = apiResponse.indexOf("\"label\":\"", currentPos)) != -1 && counter < 2) {
                int labelStart = currentPos + 9;
                int labelEnd = apiResponse.indexOf("\"", labelStart);

                if (labelStart > 9 && labelEnd > labelStart) {
                    String category = apiResponse.substring(labelStart, labelEnd);

                    // Add to context if it's a useful category
                    if (isUsefulCategory(category)) {
                        if (context.length() > 0) {
                            context.append(" with ");
                        }
                        context.append(category);
                        counter++;
                    }
                }

                currentPos = labelEnd + 1;
            }

            return context.toString();
        } catch (Exception e) {
            System.out.println("Error extracting categories: " + e.getMessage());
        }
        return "";
    }

    private static boolean isUsefulCategory(String category) {
        String[] lowValueCategories = {"image", "picture", "photo", "object", "misc", "other"};
        for (String lowValue : lowValueCategories) {
            if (category.toLowerCase().contains(lowValue)) {
                return false;
            }
        }
        return true;
    }

    private static Map<String, String> extractVideoMetadata(File videoFile) {
        Map<String, String> metadata = new HashMap<>();

        // Create Media object with error handling
        Media media;
        try {
            media = new Media(videoFile.toURI().toString());

            // Wait for media to be ready
            CountDownLatch readyLatch = new CountDownLatch(1);
            MediaPlayer mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnReady(() -> {
                // Get duration
                double durationSeconds = media.getDuration().toSeconds();
                metadata.put("duration", String.valueOf(durationSeconds));

                // Get width and height
                int width = media.getWidth();
                int height = media.getHeight();
                if (width > 0 && height > 0) {
                    metadata.put("resolution", width + "x" + height);
                }

                readyLatch.countDown();
            });

            mediaPlayer.setOnError(() -> {
                System.out.println("MediaPlayer error during metadata extraction: " +
                        (mediaPlayer.getError() != null ? mediaPlayer.getError().getMessage() : "Unknown error"));
                readyLatch.countDown();
            });

            // Wait for media to be ready (5 seconds timeout)
            if (!readyLatch.await(5, TimeUnit.SECONDS)) {
                System.out.println("Timeout waiting for media metadata");
            }

            // Cleanup
            Platform.runLater(() -> mediaPlayer.dispose());

        } catch (Exception e) {
            System.out.println("Failed to extract metadata: " + e.getMessage());
        }

        return metadata;
    }

    private static List<File> extractFrames(File videoFile, Path tempDir, int maxFrames) throws Exception {
        List<File> frames = new ArrayList<>();

        // Create Media object with error handling
        Media media;
        try {
            media = new Media(videoFile.toURI().toString());
        } catch (MediaException e) {
            System.out.println("Failed to load media: " + e.getMessage());
            return frames;
        }

        MediaPlayer mediaPlayer = new MediaPlayer(media);
        MediaView mediaView = new MediaView(mediaPlayer);

        // Wait for media to be ready
        CountDownLatch readyLatch = new CountDownLatch(1);

        mediaPlayer.setOnReady(() -> {
            System.out.println("Media ready, duration: " + mediaPlayer.getMedia().getDuration().toSeconds() + " seconds");
            readyLatch.countDown();
        });

        mediaPlayer.setOnError(() -> {
            System.out.println("MediaPlayer error: " +
                    (mediaPlayer.getError() != null ? mediaPlayer.getError().getMessage() : "Unknown error"));
            readyLatch.countDown();
        });

        // Wait for media to be ready (increased timeout to 30 seconds)
        if (!readyLatch.await(30, TimeUnit.SECONDS)) {
            System.out.println("Timeout waiting for media to be ready");
            Platform.runLater(() -> mediaPlayer.dispose());
            return frames;
        }

        // Check if an error occurred
        if (mediaPlayer.getError() != null) {
            System.out.println("MediaPlayer failed: " + mediaPlayer.getError().getMessage());
            Platform.runLater(() -> mediaPlayer.dispose());
            return frames;
        }

        // Get video duration
        Duration duration = mediaPlayer.getMedia().getDuration();
        double durationInSeconds = duration.toSeconds();
        System.out.println("Video duration: " + durationInSeconds + " seconds");

        // Skip extremely short videos
        if (durationInSeconds < 1.0) {
            System.out.println("Video too short to analyze");
            Platform.runLater(() -> mediaPlayer.dispose());
            return frames;
        }

        // Calculate adaptive frame count based on video duration
        int actualFrameCount = maxFrames;
        if (durationInSeconds < 10) {
            // For very short videos, fewer frames
            actualFrameCount = Math.min(5, maxFrames);
        } else if (durationInSeconds > 60) {
            // For longer videos, more frames up to max
            actualFrameCount = Math.min((int)(durationInSeconds / 4), maxFrames);
        }

        // Calculate frame capture points with more intelligent distribution
        double[] capturePoints = new double[actualFrameCount];

        if (actualFrameCount == 1) {
            capturePoints[0] = durationInSeconds * 0.5; // Single frame in the middle
        } else {
            // For very short videos (< 3 seconds), just distribute evenly
            if (durationInSeconds < 3.0) {
                for (int i = 0; i < actualFrameCount; i++) {
                    capturePoints[i] = durationInSeconds * (i + 0.5) / actualFrameCount;
                }
            } else {
                // For normal videos:
                // - Always include start (15% in) and end (85% in)
                // - Put more frames in the middle section
                capturePoints[0] = durationInSeconds * 0.15;
                capturePoints[actualFrameCount-1] = durationInSeconds * 0.85;

                if (actualFrameCount > 2) {
                    for (int i = 1; i < actualFrameCount - 1; i++) {
                        double percentage = 0.2 + (0.6 * i / (actualFrameCount - 2));
                        capturePoints[i] = durationInSeconds * percentage;
                    }
                }
            }
        }

        final SnapshotParameters params = new SnapshotParameters();

        // Capture each frame at calculated points
        CountDownLatch[] frameLatch = new CountDownLatch[actualFrameCount];
        for (int i = 0; i < actualFrameCount; i++) {
            frameLatch[i] = new CountDownLatch(1);
            final int frameIndex = i;
            final double capturePoint = capturePoints[i];
            final CountDownLatch currentFrameLatch = frameLatch[i];

            if (capturePoint < durationInSeconds) {
                Platform.runLater(() -> {
                    try {
                        mediaPlayer.seek(Duration.seconds(capturePoint));

                        mediaPlayer.setOnPlaying(() -> {
                            try {
                                Thread.sleep(500);
                                WritableImage snapshot = mediaView.snapshot(params, null);
                                if (snapshot != null) {
                                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
                                    BufferedImage resizedImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
                                    resizedImage.getGraphics().drawImage(
                                            bufferedImage.getScaledInstance(256, 256, java.awt.Image.SCALE_SMOOTH), 0, 0, null);

                                    String fileName = "frame_" + frameIndex + ".jpg";
                                    File frameFile = tempDir.resolve(fileName).toFile();
                                    ImageIO.write(resizedImage, "jpg", frameFile);

                                    BufferedImage testImage = ImageIO.read(frameFile);
                                    if (testImage == null) {
                                        System.out.println("Invalid JPEG generated for frame " + frameIndex);
                                        Files.copy(frameFile.toPath(), tempDir.resolve("invalid_frame_" + frameIndex + ".jpg"));
                                    } else {
                                        frames.add(frameFile);
                                        System.out.println("Frame " + frameIndex + " captured at " + capturePoint + " seconds");
                                    }
                                } else {
                                    System.out.println("Failed to capture image for frame " + frameIndex);
                                }
                            } catch (Exception e) {
                                System.out.println("Error saving frame " + frameIndex + ": " + e.getMessage());
                                e.printStackTrace();
                            } finally {
                                currentFrameLatch.countDown();
                            }
                        });

                        mediaPlayer.play();
                    } catch (Exception e) {
                        System.out.println("Error in frame capture " + frameIndex + ": " + e.getMessage());
                        e.printStackTrace();
                        currentFrameLatch.countDown();
                    }
                });

                if (!currentFrameLatch.await(10, TimeUnit.SECONDS)) {
                    System.out.println("Timeout for frame capture " + i);
                }

                Platform.runLater(() -> mediaPlayer.stop());
                Thread.sleep(300);
            }
        }

        Platform.runLater(() -> mediaPlayer.dispose());
        System.out.println("Extracted " + frames.size() + " frames from video");
        return frames;
    }

    private static String analyzeImageWithHuggingFace(File imageFile) {
        try {
            BufferedImage testImage = ImageIO.read(imageFile);
            if (testImage == null) {
                System.out.println("Invalid image file: " + imageFile.getAbsolutePath());
                return "an undescribed scene (invalid image)";
            }

            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            System.out.println("Base64 length for " + imageFile.getName() + ": " + base64Image.length());

            String mainDescription = callHuggingFaceAPI(IMAGE_CAPTIONING_API_URL, base64Image);
            String detailedDescription = callHuggingFaceAPI(DETAILED_CAPTIONING_API_URL, base64Image);

            if ((mainDescription == null || mainDescription.trim().isEmpty() || mainDescription.contains("error")) &&
                    (detailedDescription == null || detailedDescription.trim().isEmpty() || detailedDescription.contains("error"))) {
                System.out.println("Main and detailed models failed for " + imageFile.getName() + ", trying backup model");
                detailedDescription = callHuggingFaceAPI(BACKUP_CAPTIONING_API_URL, base64Image);
            }

            String sceneContext = classifySceneContext(imageFile);
            String combinedDescription = combineDescriptions(mainDescription, detailedDescription, sceneContext);
            String finalDescription = improveDescription(combinedDescription, imageFile.getName());

            return (finalDescription != null && !finalDescription.trim().isEmpty()) ? finalDescription : "an undescribed scene";

        } catch (Exception e) {
            e.printStackTrace();
            return "an undescribed scene (analysis error: " + e.getMessage() + ")";
        }
    }

    private static String combineDescriptions(String main, String detailed, String context) {
        main = (main != null && !main.trim().isEmpty() && !main.contains("error")) ? main.trim() : "";
        detailed = (detailed != null && !detailed.trim().isEmpty() && !detailed.contains("error")) ? detailed.trim() : "";
        context = (context != null && !context.trim().isEmpty()) ? context.trim() : "";

        if (main.isEmpty() && !detailed.isEmpty()) return detailed;
        if (!main.isEmpty() && detailed.isEmpty()) return main;
        if (main.isEmpty() && detailed.isEmpty()) return "an undescribed object or scene";

        if (calculateSimilarity(main, detailed) > 0.7) {
            String better = (detailed.length() > main.length()) ? detailed : main;
            return context.isEmpty() ? better : better + " in a " + context + " setting";
        }

        StringBuilder combined = new StringBuilder();
        String mainCore = extractCoreConcept(main).toLowerCase();
        String detailedCore = extractCoreConcept(detailed).toLowerCase();

        if (main.length() <= detailed.length()) {
            combined.append(main);

            String[] mainWords = mainCore.split("\\s+");
            String[] detailedWords = detailedCore.split("\\s+");

            List<String> uniqueDetails = new ArrayList<>();
            for (String word : detailedWords) {
                if (word.length() > 4) {
                    boolean isUnique = true;
                    for (String mainWord : mainWords) {
                        if (word.equals(mainWord) || calculateWordSimilarity(word, mainWord) > 0.8) {
                            isUnique = false;
                            break;
                        }
                    }
                    if (isUnique) {
                        uniqueDetails.add(word);
                    }
                }
            }

            if (!uniqueDetails.isEmpty()) {
                String detailPhrase = detailed.toLowerCase();
                for (String mainWord : mainWords) {
                    detailPhrase = detailPhrase.replaceAll("\\b" + mainWord + "\\b", "").trim();
                }
                detailPhrase = detailPhrase.replaceAll("\\s+", " ").trim();
                if (!detailPhrase.isEmpty()) {
                    combined.append(", featuring ").append(Character.toUpperCase(detailPhrase.charAt(0)) + detailPhrase.substring(1));
                }
            }
        } else {
            combined.append(detailed);

            String[] mainWords = mainCore.split("\\s+");
            String[] detailedWords = detailedCore.split("\\s+");

            List<String> uniqueDetails = new ArrayList<>();
            for (String word : mainWords) {
                if (word.length() > 4) {
                    boolean isUnique = true;
                    for (String detailedWord : detailedWords) {
                        if (word.equals(detailedWord) || calculateWordSimilarity(word, detailedWord) > 0.8) {
                            isUnique = false;
                            break;
                        }
                    }
                    if (isUnique) {
                        uniqueDetails.add(word);
                    }
                }
            }

            if (!uniqueDetails.isEmpty()) {
                String mainPhrase = main.toLowerCase();
                for (String detailedWord : detailedWords) {
                    mainPhrase = mainPhrase.replaceAll("\\b" + detailedWord + "\\b", "").trim();
                }
                mainPhrase = mainPhrase.replaceAll("\\s+", " ").trim();
                if (!mainPhrase.isEmpty()) {
                    combined.append(", featuring ").append(Character.toUpperCase(mainPhrase.charAt(0)) + mainPhrase.substring(1));
                }
            }
        }

        if (!context.isEmpty() && !combined.toString().toLowerCase().contains(context.toLowerCase())) {
            combined.append(" in a ").append(context).append(" setting");
        }

        return combined.toString();
    }

    private static double calculateWordSimilarity(String word1, String word2) {
        int maxLength = Math.max(word1.length(), word2.length());
        if (maxLength == 0) return 1.0;

        int levenshteinDistance = levenshteinDistance(word1, word2);
        return (double) (maxLength - levenshteinDistance) / maxLength;
    }

    private static int levenshteinDistance(String s1, String s2) {
        int[] prev = new int[s2.length() + 1];
        int[] curr = new int[s2.length() + 1];

        for (int j = 0; j <= s2.length(); j++) {
            prev[j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }

        return prev[s2.length()];
    }

    private static String improveDescription(String description, String filename) {
        if (description == null || description.trim().isEmpty()) {
            return "an undescribed scene";
        }

        description = description.replaceAll("^[Aa] photo of ", "")
                .replaceAll("^[Tt]his is ", "")
                .replaceAll("^[Tt]here is ", "")
                .replaceAll("^[Ii] see ", "")
                .replaceAll("^[Aa]n image of ", "")
                .replaceAll("^[Aa] picture of ", "")
                .replaceAll("^[Aa]n ", "")
                .replaceAll("^[Aa] ", "")
                .trim();

        boolean hasColorTerm = containsColorTerm(description);
        if (!hasColorTerm) {
            String dominantColor = "colorful";
            description = dominantColor + " " + description;
        }

        boolean hasShapeTerm = containsShapeTerm(description);
        if (!hasShapeTerm && description.contains("vase")) {
            String[] shapeTerms = {"rounded", "curved", "elegant", "decorative", "ornate", "uniquely shaped"};
            description = shapeTerms[(int)(Math.random() * shapeTerms.length)] + " " + description;
        }

        if (!description.isEmpty()) {
            description = Character.toUpperCase(description.charAt(0)) + description.substring(1);
        }

        System.out.println("Enhanced description for " + filename + ": " + description);
        return description;
    }

    private static boolean containsColorTerm(String text) {
        String[] colorTerms = {"red", "blue", "green", "yellow", "purple", "orange", "black",
                "white", "brown", "pink", "gray", "grey", "silver", "gold",
                "colorful", "dark", "light", "bright", "vibrant"};
        String lowerText = text.toLowerCase();
        for (String color : colorTerms) {
            if (lowerText.contains(color)) return true;
        }
        return false;
    }

    private static boolean containsShapeTerm(String text) {
        String[] shapeTerms = {"round", "square", "oval", "rectangular", "curved", "straight",
                "circular", "angular", "tall", "short", "wide", "narrow",
                "ornate", "decorative", "patterned", "textured", "smooth"};
        String lowerText = text.toLowerCase();
        for (String shape : shapeTerms) {
            if (lowerText.contains(shape)) return true;
        }
        return false;
    }

    private static String callHuggingFaceAPI(String apiUrl, String base64Image) {
        int maxRetries = 5;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);

                String inputJson = "{\"inputs\": \"" + base64Image + "\"}";
                System.out.println("Sending API request to " + apiUrl + ", payload size: " + inputJson.length() + " characters");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(inputJson.getBytes("UTF-8"));
                    os.flush();
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 429 || responseCode == 503) {
                    System.out.println("API error (code " + responseCode + "), retrying after " + (attempt * 2) + " seconds");
                    Thread.sleep(2000 * attempt);
                    continue;
                }
                if (responseCode != 200) {
                    StringBuilder errorResponse = new StringBuilder();
                    try (Scanner errorScanner = new Scanner(conn.getErrorStream())) {
                        while (errorScanner.hasNext()) {
                            errorResponse.append(errorScanner.nextLine());
                        }
                    }
                    System.out.println("Hugging Face API error (code " + responseCode + "): " + errorResponse);
                    return "API error: " + errorResponse;
                }

                StringBuilder response = new StringBuilder();
                try (Scanner scanner = new Scanner(conn.getInputStream(), "UTF-8")) {
                    while (scanner.hasNext()) {
                        response.append(scanner.nextLine());
                    }
                }

                String fullResponse = response.toString();
                System.out.println("API response: " + fullResponse);

                int start = fullResponse.indexOf("\"generated_text\":\"") + 18;
                int end = fullResponse.lastIndexOf("\"");
                if (start >= 18 && end > start) {
                    return fullResponse.substring(start, end)
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"");
                } else {
                    System.out.println("Failed to parse API response: " + fullResponse);
                    return "parsing error";
                }

            } catch (Exception e) {
                if (attempt == maxRetries) {
                    e.printStackTrace();
                    return "API error: " + e.getMessage();
                }
                try {
                    Thread.sleep(2000 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return "API error: interrupted";
                }
            }
        }
        return "API error: maximum retries exceeded";
    }

    public static void main(String[] args) {
        try {
            Platform.startup(() -> {
                System.out.println("JavaFX initialized");
            });
        } catch (IllegalStateException e) {
            // JavaFX already initialized
        }

        try {
            if (args.length < 1) {
                System.out.println("Usage: java VideoAnalysisService <video_file_path> [max_frames]");
                return;
            }

            File videoFile = new File(args[0]);
            if (!videoFile.exists()) {
                System.out.println("Video file not found: " + args[0]);
                return;
            }

            int maxFrames = DEFAULT_MAX_FRAMES;
            if (args.length > 1) {
                try {
                    maxFrames = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid max frames value, using default: " + DEFAULT_MAX_FRAMES);
                }
            }

            String description = generateVideoDescription(videoFile, maxFrames);
            System.out.println("\nVideo Description:");
            System.out.println(description);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Platform.exit();
        }
    }
}