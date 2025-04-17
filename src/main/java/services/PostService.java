package services;

import entities.Post;
import entities.utilisateur;
import utils.MyDataBase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PostService implements IService<Post>{
    Connection conx;
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
                String fullPath = "src/main/resources/uploads/" + fileName;
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
        String sql = "SELECT * FROM post";

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


                posts.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return posts;
    }

    @Override
    public void add(Post post) throws SQLException {

    }

    public void addWithMedia(Post post, List<String> mediaFilePaths) throws SQLException, IOException {
        String query = "INSERT INTO post(user_p_id, contenu, date_publication, nbr_jaime, status_post) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = conx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, post.getUser_p_id());
        ps.setString(2, post.getContenu());
        ps.setTimestamp(3, Timestamp.valueOf(post.getDate_publication()));
        ps.setInt(4, post.getNbr_jaime());
        ps.setBoolean(5, post.isStatus_post());
        ps.executeUpdate();

        System.out.println("Post ajouté avec succès");

        ResultSet rs = ps.getGeneratedKeys();
        int postId = -1;
        if (rs.next()) {
            postId = rs.getInt(1);
        }

        String uploadDir = "src/main/resources/uploads";
        File uploadFolder = new File(uploadDir);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        if (postId != -1 && mediaFilePaths != null) {
            for (String path : mediaFilePaths) {
                File sourceFile = new File(path);
                if (!sourceFile.exists()) continue;

                String extension = getFileExtension(sourceFile);
                String uniqueFileName = UUID.randomUUID() + "." + extension;
                File destFile = new File(uploadFolder, uniqueFileName);

                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Insertion en base
                String mediaInsertSQL = "INSERT INTO media_post(post_id, chemin) VALUES (?, ?)";
                PreparedStatement mediaPs = conx.prepareStatement(mediaInsertSQL);
                mediaPs.setInt(1, postId);
                mediaPs.setString(2, uniqueFileName);
                mediaPs.executeUpdate();
            }
            System.out.println("Images ajoutées avec succès");
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
                    ps.setString(2, path);
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



}
