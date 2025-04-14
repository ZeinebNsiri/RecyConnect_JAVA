package services;

import entities.Post;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                mediaUrls.add(rs.getString("chemin"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return mediaUrls;
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
        addWithMedia(post, null);
    }

    public void addWithMedia(Post post, List<String> mediaPaths) throws SQLException {
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

        if (postId != -1 && mediaPaths != null) {
            for (String path : mediaPaths) {
                String mediaQuery = "INSERT INTO media_post(post_id, chemin) VALUES (?, ?)";
                PreparedStatement mediaStm = conx.prepareStatement(mediaQuery);
                mediaStm.setInt(1, postId);
                mediaStm.setString(2, path);
                mediaStm.executeUpdate();
            }
            System.out.println("Images ajoutées avec succès");
        }
    }


    @Override
    public void delete(Post post) throws SQLException {

    }

    @Override
    public void update(Post post) throws SQLException {

    }


}
