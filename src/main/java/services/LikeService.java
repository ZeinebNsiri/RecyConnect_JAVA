package services;

import entities.Like;

import utils.MyDataBase; // ton utilitaire de connexion
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LikeService {
    private Connection connection;

    public LikeService() {
        connection = MyDataBase.getInstance().getConx(); // adapte selon ton projet
    }

    public boolean hasUserLikedPost(int user, int post) {
        String sql = "SELECT * FROM `like` WHERE user_like_id = ? AND post_like_id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, user);
            pst.setInt(2, post);
            ResultSet rs = pst.executeQuery();
            return rs.next(); // true s'il y a une ligne
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addLike(int user, int post) {
        String sql = "INSERT INTO `like` (user_like_id, post_like_id) VALUES (?, ?)";
        String updatePostSQL = "UPDATE post SET nbr_jaime = nbr_jaime + 1 WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql);
             PreparedStatement updatePost = connection.prepareStatement(updatePostSQL)) {
            pst.setInt(1, user);
            pst.setInt(2, post);
            pst.executeUpdate();

            updatePost.setInt(1, post);
            updatePost.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeLike(int user, int post) {
        String sql = "DELETE FROM `like` WHERE user_like_id = ? AND post_like_id = ?";
        String updatePostSQL = "UPDATE post SET nbr_jaime = nbr_jaime - 1 WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql);
             PreparedStatement updatePost = connection.prepareStatement(updatePostSQL)) {
            pst.setInt(1, user);
            pst.setInt(2, post);
            pst.executeUpdate();

            updatePost.setInt(1, post);
            updatePost.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getLikesCountForPost(int postId) {
        String sql = "SELECT COUNT(*) AS like_count FROM `like` WHERE post_like_id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, postId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("like_count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
