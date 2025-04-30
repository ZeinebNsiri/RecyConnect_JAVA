package services;

import entities.Commentaire;
import entities.utilisateur;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService {
    private  Connection conx;

    public CommentaireService() {conx = MyDataBase.getInstance().getConx();}

    // ✅ 1. Ajouter un commentaire
    public void ajouter(Commentaire c) {
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
                    user.setNom_user(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));
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



}
