package services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import entities.Post;
import entities.utilisateur;
import enums.PostTag;
import utils.MyDataBase;
import com.fasterxml.jackson.databind.ObjectMapper;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.JsonNode;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.*;

public class PostService implements IService<Post>{
    Connection conx;
    private String currentRole = "USER";
    public PostService() {conx = MyDataBase.getInstance().getConx();}


    public List<String> getMediaForPost(int postId) {
        List<String> mediaUrls = new ArrayList<>();
        String sql = "SELECT chemin FROM media_post WHERE post_id = ?";

        try {
            PreparedStatement ps = conx.prepareStatement(sql);
            ps.setInt(1, postId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String fileName = rs.getString("chemin");
                String fullPath = "C:\\Users\\samar\\Desktop\\PI_RecyConnect_TechSquad\\public\\Posts\\uploads\\" + fileName;
                mediaUrls.add(fullPath);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return mediaUrls;
    }

    public utilisateur getUserPById(int userId) {
        utilisateur user = new utilisateur();
        String sql = "SELECT * FROM utilisateur WHERE id = ?";
        try (PreparedStatement ps = conx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user.setId(rs.getInt("id"));
                user.setPrenom(rs.getString("prenom"));
                user.setPhoto_profil(rs.getString("photo_profil"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }



    @Override
    public List<Post> displayList() throws SQLException {
        List<Post> posts = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();


        String sql;
        if (currentRole.equalsIgnoreCase("ADMIN")) {
            sql = "SELECT * FROM post";
        } else {
            sql = "SELECT * FROM post WHERE status_post = true";
        }
        try {
            Statement stmt = conx.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Post p = new Post();
                p.setId(rs.getInt("id"));
                p.setUser_p_id(rs.getInt("user_p_id"));
                p.setContenu(rs.getString("contenu"));
                p.setDate_publication(rs.getTimestamp("date_publication").toLocalDateTime());
                p.setNbr_jaime(rs.getInt("nbr_jaime"));
                p.setStatus_post(rs.getBoolean("status_post"));

                String tagsJson = rs.getString("tags");
                if (tagsJson != null && !tagsJson.isEmpty()) {
                    List<String> tagLabels = null;
                    try {
                        tagLabels = objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    List<PostTag> tags = new ArrayList<>();
                    for (String label : tagLabels) {
                        try {
                            tags.add(PostTag.fromLabel(label));
                        } catch (IllegalArgumentException e) {
                            System.err.println("Tag inconnu ignoré: " + label);
                        }
                    }
                    p.setTags(tags);
                }

                posts.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return posts;
    }


    public Map<Post, List<String>> getPostsWithMediaByUser(int userId) {
        Map<Post, List<String>> postMediaMap = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        String sql = "SELECT * FROM post WHERE user_p_id = ?";

        try (PreparedStatement ps = conx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Post p = new Post();
                p.setId(rs.getInt("id"));
                p.setUser_p_id(rs.getInt("user_p_id"));
                p.setContenu(rs.getString("contenu"));
                p.setDate_publication(rs.getTimestamp("date_publication").toLocalDateTime());
                p.setNbr_jaime(rs.getInt("nbr_jaime"));
                p.setStatus_post(rs.getBoolean("status_post"));
                String tagsJson = rs.getString("tags");
                if (tagsJson != null && !tagsJson.isEmpty()) {
                    List<String> tagLabels = null;
                    try {
                        tagLabels = objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    List<PostTag> tags = new ArrayList<>();
                    for (String label : tagLabels) {
                        try {
                            tags.add(PostTag.fromLabel(label));
                        } catch (IllegalArgumentException e) {
                            System.err.println("Tag inconnu ignoré: " + label);
                        }
                    }
                    p.setTags(tags);
                }

                List<String> mediaList = getMediaForPost(p.getId());

                postMediaMap.put(p, mediaList);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return postMediaMap;
    }



    @Override
    public void add(Post post) throws SQLException {

    }

    public void addWithMedia(Post post, List<String> mediaFilePaths) throws SQLException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        String tagsJson = mapper.writeValueAsString(post.getTags().stream().map(PostTag::getLabel).toList());

        String query = "INSERT INTO post(user_p_id, contenu, date_publication, nbr_jaime, status_post, tags) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, post.getUser_p_id());
            ps.setString(2, post.getContenu());
            ps.setTimestamp(3, Timestamp.valueOf(post.getDate_publication()));
            ps.setInt(4, post.getNbr_jaime());
            ps.setBoolean(5, post.isStatus_post());
            ps.setString(6, tagsJson);

            ps.executeUpdate();
            System.out.println("Post ajouté avec succès");

            int postId = -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    postId = rs.getInt(1);
                }
            }

            String uploadDir = "C:\\Users\\samar\\Desktop\\PI_RecyConnect_TechSquad\\public\\Posts\\uploads";
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) uploadFolder.mkdirs();

            if (postId != -1 && mediaFilePaths != null) {
                for (String path : mediaFilePaths) {
                    File sourceFile = new File(path);
                    if (!sourceFile.exists()) continue;

                    String extension = getFileExtension(sourceFile);
                    String uniqueFileName = UUID.randomUUID() + "." + extension;
                    File destFile = new File(uploadFolder, uniqueFileName);
                    Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    String mediaInsertSQL = "INSERT INTO media_post(post_id, chemin) VALUES (?, ?)";
                    try (PreparedStatement mediaPs = conx.prepareStatement(mediaInsertSQL)) {
                        mediaPs.setInt(1, postId);
                        mediaPs.setString(2, uniqueFileName);
                        mediaPs.executeUpdate();
                    }
                }
                System.out.println("Images ajoutées avec succès");
            }
        }
    }


    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf(".");
        if (lastIndex == -1) return ""; // pas d'extension
        return name.substring(lastIndex + 1);
    }



    @Override
    public void delete(Post post) throws SQLException {
        String deleteMediaSQL = "DELETE FROM media_post WHERE post_id = ?";
        PreparedStatement psMedia = conx.prepareStatement(deleteMediaSQL);
        psMedia.setInt(1, post.getId());
        psMedia.executeUpdate();


        String deleteComments = "DELETE FROM commentaire WHERE post_com_id = ?";
        PreparedStatement psComments = conx.prepareStatement(deleteComments);
        psComments.setInt(1, post.getId());
        psComments.executeUpdate();

        String deletePostSQL = "DELETE FROM post WHERE id = ?";
        PreparedStatement psPost = conx.prepareStatement(deletePostSQL);
        psPost.setInt(1, post.getId());
        psPost.executeUpdate();

        System.out.println("Post supprimé avec succès (et ses images aussi).");
    }


    @Override
    public void update(Post post) throws SQLException {
        update(post, null);
    }

    public void update(Post post, List<String> newMediaPaths) throws SQLException {
        String updateQuery = "UPDATE post SET contenu = ? WHERE id = ?";
        try (PreparedStatement ps = conx.prepareStatement(updateQuery)) {
            ps.setString(1, post.getContenu());


            ps.setInt(2, post.getId());
            ps.executeUpdate();
            System.out.println("Contenu du post mis à jour !");
        }

        // 2. Mise à jour des médias (si fourni)
        if (newMediaPaths != null) {
            // Supprimer les anciennes images
            String deleteMediaQuery = "DELETE FROM media_post WHERE post_id = ?";
            try (PreparedStatement ps = conx.prepareStatement(deleteMediaQuery)) {
                ps.setInt(1, post.getId());
                ps.executeUpdate();
                System.out.println("Anciennes images supprimées !");
            }

            // Ajouter les nouvelles images
            for (String path : newMediaPaths) {
                String insertMediaQuery = "INSERT INTO media_post(post_id, chemin) VALUES (?, ?)";
                try (PreparedStatement ps = conx.prepareStatement(insertMediaQuery)) {
                    ps.setInt(1, post.getId());
                    ps.setString(2, new File(path).getName());
                    ps.executeUpdate();
                }
            }
            System.out.println("Nouvelles images ajoutées !");
        }
    }



    public void approuverPost(Post post) throws SQLException {
        String sql = "UPDATE post SET status_post = ? WHERE id = ?";
        try (PreparedStatement ps = conx.prepareStatement(sql)) {
            ps.setBoolean(1, true);
            ps.setInt(2, post.getId());
            ps.executeUpdate();
            System.out.println("Post approuvé !");
        }
    }

    public void rejeterPost(Post post) throws SQLException {
        String sql = "UPDATE post SET status_post = ? WHERE id = ?";
        try (PreparedStatement ps = conx.prepareStatement(sql)) {
            ps.setBoolean(1, false);
            ps.setInt(2, post.getId());
            ps.executeUpdate();
            System.out.println("Post rejeté !");
        }
    }


    public boolean analyzePostContent(String content) {
        try {
            HttpResponse<JsonNode> response = Unirest.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=AIzaSyCvcwyNuZeyE5WaLCaCJZfaRc0gEG86LSM")
                    .header("Content-Type", "application/json")
                    .body(new JSONObject()
                            .put("contents", List.of(
                                    Map.of("parts", List.of(
                                            Map.of("text", "Analyse ce contenu attentivement : \"" + content + "\". Si tu détectes un langage de haine, racisme, ou agression, réponds uniquement avec le mot 'Toxique'. Sinon, réponds uniquement 'Non Toxique'.")
                                    ))
                            ))
                    ).asJson();

            JSONObject responseBody = response.getBody().getObject();

            // Vérifier s'il y a des erreurs d'abord
            if (responseBody.has("error")) {
                System.err.println("Erreur de l'API Gemini: " + responseBody.getJSONObject("error").getString("message"));
                return false;
            }

            // Vérifier si 'candidates' existe
            if (!responseBody.has("candidates")) {
                System.err.println("Réponse inattendue: " + responseBody.toString());
                return false;
            }

            // Extraire la réponse de Gemini
            JSONArray candidates = responseBody.getJSONArray("candidates");
            if (candidates.isEmpty()) {
                System.err.println("Pas de candidats retournés par Gemini.");
                return false;
            }

            JSONObject firstCandidate = candidates.getJSONObject(0);
            JSONObject contentObject = firstCandidate.getJSONObject("content");
            JSONArray partsArray = contentObject.getJSONArray("parts");
            String result = partsArray.getJSONObject(0).getString("text").toLowerCase().trim();

            // Vérifier si le contenu est toxique
            if (result.equals("toxique")) {
                // Construire le mail HTML
                String htmlContent = "<html>"
                        + "<body style='font-family: Arial, sans-serif; margin: 0; padding: 0;'>"
                        + "<div style='background-color: #f44336; padding: 20px; color: white; text-align: center;'>"
                        + "<h1>Alerte de Contenu Inapproprié 🚨</h1>"
                        + "</div>"
                        + "<div style='padding: 20px; text-align: center;'>"
                        + "<img src='cid:logo' alt='Logo' width='100' style='margin-bottom: 20px;'/>\n"
                        + "<p>Bonjour Admin,</p>"
                        + "<p>Un contenu potentiellement <strong>violent, raciste ou haineux</strong> a été détecté sur la plateforme.</p>"
                        + "<div style='margin-top: 20px; padding: 10px; background-color: #eee; border-radius: 5px;'>"
                        + "<p style='color: #333;'>" + escapeHtml(content) + "</p>"
                        + "</div>"
                        + "<p style='margin-top: 20px;'>Merci de vérifier rapidement !</p>"
                        + "</div>"
                        + "</body>"
                        + "</html>";

                // Envoyer le mail à l'admin
                MailService.sendMail("samartouil2018@gmail.com", "Alerte : Contenu Inapproprié détecté", htmlContent);

                return true;
            }

            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Pour éviter toute injection HTML dans l'email
     */
    private String escapeHtml(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }




    public Map<YearMonth, Integer> getPostCountByMonth() {
        Map<YearMonth, Integer> postCountByMonth = new HashMap<>();
        String sql = "SELECT date_publication FROM post";

        try (Statement stmt = conx.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Timestamp datePublication = rs.getTimestamp("date_publication");
                LocalDate localDate = datePublication.toLocalDateTime().toLocalDate();
                YearMonth yearMonth = YearMonth.from(localDate);  // Extrait l'année et le mois

                // Comptabilise le nombre de posts pour chaque mois
                postCountByMonth.put(yearMonth, postCountByMonth.getOrDefault(yearMonth, 0) + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return postCountByMonth;
    }



    public List<Post> getTop5PostsByLikes() {
        List<Post> topPosts = new ArrayList<>();
        String sql = "SELECT * FROM post ORDER BY nbr_jaime DESC LIMIT 5";  // Récupérer les 5 posts les plus likés

        try (Statement stmt = conx.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Post p = new Post();
                p.setId(rs.getInt("id"));
                p.setUser_p_id(rs.getInt("user_p_id"));
                p.setContenu(rs.getString("contenu"));
                p.setDate_publication(rs.getTimestamp("date_publication").toLocalDateTime());
                p.setNbr_jaime(rs.getInt("nbr_jaime"));
                p.setStatus_post(rs.getBoolean("status_post"));
                // Ajoute à la liste
                topPosts.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return topPosts;
    }







}
