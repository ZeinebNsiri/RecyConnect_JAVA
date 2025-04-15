package services;

import entities.Article;
import entities.CategorieArticle;
import entities.utilisateur;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class ArticleService implements IService<Article>{

    Connection con;

    public ArticleService() {
        con = utils.MyDataBase.getInstance().getConx();
    }

    @Override
    public List<Article> displayList() throws SQLException {
        String query = "SELECT * FROM `article` ";
        List<Article> articles = new ArrayList<>();
        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery(query);
        while (rs.next()){
            Article a =new Article(rs.getInt(1),rs.getInt(2),rs.getInt(3),rs.getString(4),rs.getString(5),rs.getInt(6),rs.getDouble(7),rs.getString(8),rs.getString(9));
            articles.add(a);
        }

        return articles;
    }

    @Override
    public void add(Article article) throws SQLException {
        String query = "INSERT INTO `article`(`categorie_id`, `utilisateur_id`, `nom_article`, `description_article`, `quantite_article`, `prix`, `image_article`, `localisation_article`) VALUES (?,?,?,?,?,?,?,?)";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1,article.getCategorie_id());
        ps.setInt(2,article.getUtilisateur_id());
        ps.setString(3,article.getNom_article());
        ps.setString(4,article.getDescription_article());
        ps.setInt(5,article.getQuantite_article());
        ps.setDouble(6,article.getPrix() );
        ps.setString(7,article.getImage_article());
        ps.setString(8,article.getLocalisation_article());
        ps.executeUpdate();
        System.out.println("article ajoutée");
    }

    @Override
    public void update(Article article) throws SQLException {
        String query = "UPDATE `article` SET `categorie_id`=?, `utilisateur_id`=?, `nom_article`=?, `description_article`=?, `quantite_article`=?, `prix`=?, `image_article`=?, `localisation_article`=? WHERE `id`=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, article.getCategorie_id());
        ps.setInt(2, article.getUtilisateur_id());
        ps.setString(3, article.getNom_article());
        ps.setString(4, article.getDescription_article());
        ps.setInt(5, article.getQuantite_article());
        ps.setDouble(6, article.getPrix());
        ps.setString(7, article.getImage_article());
        ps.setString(8, article.getLocalisation_article());
        ps.setInt(9, article.getId());
        ps.executeUpdate();
        System.out.println("Article modifié");
    }

    @Override
    public void delete(Article article) throws SQLException {
        String query = "DELETE FROM `article` WHERE `id` = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, article.getId());
        ps.executeUpdate();
        System.out.println("Article supprimé");
    }



    public utilisateur getUtilisateurById(int id) throws SQLException {
        String query = "SELECT * FROM utilisateur WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new utilisateur(
                    rs.getInt("id"),
                    rs.getString("email"),
                    rs.getString("nom_user"),
                    rs.getString("prenom"),
                    rs.getString("roles"),
                    rs.getString("num_tel"),
                    rs.getString("adresse"),
                    rs.getString("password"),
                    rs.getBoolean("status"),
                    rs.getString("matricule_fiscale"),
                    rs.getString("photo_profil")
            );
        }
        return null;
    }


    public CategorieArticle getCategorieById(int id) throws SQLException {
        String query = "SELECT * FROM categorie_article WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new CategorieArticle(
                    rs.getInt("id"),
                    rs.getString("nom_categorie"),
                    rs.getString("image_categorie"),
                    rs.getString("description_categorie")
            );
        }
        return null;
    }

}
