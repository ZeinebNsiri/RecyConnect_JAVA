package services;

import entities.Commentaire;
import entities.utilisateur;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;


import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.JsonNode;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

public class CommentaireService {
    private  Connection conx;

    public CommentaireService() {conx = MyDataBase.getInstance().getConx();}


    public void showAlertToxicComment() {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Commentaire bloqué");
        alert.setHeaderText(null);
        alert.setContentText("🚫 Votre commentaire a été bloqué car il contient un contenu inapproprié.");
        alert.showAndWait();
    }

    // ✅ 1. Ajouter un commentaire
    public void ajouter(Commentaire c) {

        if (analyzeCommentaireContent(c.getContenuCom())) {
            System.out.println("❌ Contenu toxique détecté, commentaire bloqué et signalé à l'admin.");
            showAlertToxicComment();
            return; // Ne pas insérer le commentaire
        }

        String sql = "INSERT INTO commentaire (contenu_com, user_com_id, date_com, post_com_id, parent_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conx.prepareStatement(sql)) {
            ps.setString(1, c.getContenuCom());
            ps.setInt(2, c.getUserComId());
            ps.setTimestamp(3, Timestamp.valueOf(c.getDateCom()));
            ps.setInt(4, c.getPostComId());
            if (c.getParentId() != null) {
                ps.setInt(5, c.getParentId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.executeUpdate();
            System.out.println("✅ Commentaire ajouté !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'ajout du commentaire : " + e.getMessage());
        }
    }

    // ✅ 2. Supprimer un commentaire par ID
    public void supprimer(int id) {
        String sql = "DELETE FROM commentaire WHERE id = ?";
        try (PreparedStatement ps = conx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Commentaire supprimé !");
            } else {
                System.out.println("⚠️ Aucun commentaire avec cet ID !");
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }

    // ✅ 3. Afficher tous les commentaires
    public List<Commentaire> afficherTous() {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = "SELECT * FROM commentaire";
        try (Statement st = conx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Commentaire c = new Commentaire();
                c.setId(rs.getInt("id"));
                c.setContenuCom(rs.getString("contenu_com"));
                c.setUserComId(rs.getInt("user_com_id"));
                c.setDateCom(rs.getTimestamp("date_com").toLocalDateTime());
                c.setPostComId(rs.getInt("post_com_id"));
                int parentId = rs.getInt("parent_id");
                c.setParentId(rs.wasNull() ? null : parentId);
                commentaires.add(c);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'affichage : " + e.getMessage());
        }
        return commentaires;
    }


    // ✅ 4. Récupérer les commentaires par ID de post
    public List<Commentaire> getCommentairesByPost(int postId) {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = "SELECT * FROM commentaire WHERE post_com_id = ? ORDER BY date_com ASC";
        try (PreparedStatement ps = conx.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Commentaire c = new Commentaire();
                    c.setId(rs.getInt("id"));
                    c.setContenuCom(rs.getString("contenu_com"));
                    c.setUserComId(rs.getInt("user_com_id"));
                    c.setDateCom(rs.getTimestamp("date_com").toLocalDateTime());
                    c.setPostComId(rs.getInt("post_com_id"));
                    int parentId = rs.getInt("parent_id");
                    c.setParentId(rs.wasNull() ? null : parentId);
                    commentaires.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération des commentaires du post : " + e.getMessage());
        }
        return commentaires;
    }

    public utilisateur getUserComById(int userId) {
        utilisateur user = null;
        String sql = "SELECT * FROM utilisateur WHERE id = ?";
        try (PreparedStatement ps = conx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = new utilisateur();
                    user.setId(rs.getInt("id"));
                    user.setNom_user(rs.getString("nom_user"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));
                    user.setPhoto_profil(rs.getString("photo_profil"));
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération de l'utilisateur : " + e.getMessage());
        }
        return user;
    }

    public List<Commentaire> getReplies(int parentId) {
        List<Commentaire> replies = new ArrayList<>();
        String sql = "SELECT * FROM commentaire WHERE parent_id = ?";
        try (PreparedStatement ps = conx.prepareStatement(sql)) {
            ps.setInt(1, parentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Commentaire c = new Commentaire();
                    c.setId(rs.getInt("id"));
                    c.setContenuCom(rs.getString("contenu_com"));
                    c.setUserComId(rs.getInt("user_com_id"));
                    c.setDateCom(rs.getTimestamp("date_com").toLocalDateTime());
                    c.setPostComId(rs.getInt("post_com_id"));
                    int parent = rs.getInt("parent_id");
                    c.setParentId(rs.wasNull() ? null : parent);
                    replies.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération des réponses : " + e.getMessage());
        }
        return replies;
    }

    public int getCommentCountForPost(int postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM commentaire WHERE post_com_id = ?";
        try (PreparedStatement stmt = conx.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }


    public boolean analyzeCommentaireContent(String content) {
        try {
            HttpResponse<JsonNode> response = Unirest.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=AIzaSyCvcwyNuZeyE5WaLCaCJZfaRc0gEG86LSM")
                    .header("Content-Type", "application/json")
                    .body(new JSONObject()
                            .put("contents", List.of(
                                    Map.of("parts", List.of(
                                            Map.of("text", "Analyse ce commentaire attentivement : \"" + content + "\". Si tu détectes un langage de haine, racisme, ou agression, réponds uniquement avec le mot 'Toxique'. Sinon, réponds uniquement 'Non Toxique'.")
                                    ))
                            ))
                    ).asJson();

            JSONObject responseBody = response.getBody().getObject();

            if (responseBody.has("error")) {
                System.err.println("Erreur de l'API Gemini (commentaire) : " + responseBody.getJSONObject("error").getString("message"));
                return false;
            }

            if (!responseBody.has("candidates")) {
                System.err.println("Réponse inattendue de l'API Gemini (commentaire) : " + responseBody.toString());
                return false;
            }

            JSONArray candidates = responseBody.getJSONArray("candidates");
            if (candidates.isEmpty()) {
                System.err.println("Pas de réponse de modération (commentaire).");
                return false;
            }

            JSONObject firstCandidate = candidates.getJSONObject(0);
            JSONObject contentObject = firstCandidate.getJSONObject("content");
            JSONArray partsArray = contentObject.getJSONArray("parts");
            String result = partsArray.getJSONObject(0).getString("text").toLowerCase().trim();

            if (result.equals("toxique")) {
                // Envoyer une alerte à l’admin
                String htmlContent = "<html>"
                        + "<body style='font-family: Arial, sans-serif; margin: 0; padding: 0;'>"
                        + "<div style='background-color: #f44336; padding: 20px; color: white; text-align: center;'>"
                        + "<h1>Alerte de Commentaire Toxique 🚫</h1>"
                        + "</div>"
                        + "<div style='padding: 20px; text-align: center;'>"
                        + "<img src='src/main/resources/images/mainlogo.png' alt='Logo' width='100' style='margin-bottom: 20px;'/>"
                        + "<p>Bonjour Admin,</p>"
                        + "<p>Un <strong>commentaire toxique</strong> a été détecté.</p>"
                        + "<div style='margin-top: 20px; padding: 10px; background-color: #eee; border-radius: 5px;'>"
                        + "<p style='color: #333;'>" + escapeHtml(content) + "</p>"
                        + "</div>"
                        + "<p style='margin-top: 20px;'>Merci de vérifier cela rapidement.</p>"
                        + "</div>"
                        + "</body>"
                        + "</html>";

                MailService.sendMail("samartouil2018@gmail.com", "Alerte : Commentaire Toxique détecté", htmlContent);

                return true; // toxique détecté
            }

            return false; // non toxique

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private String escapeHtml(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }




}
