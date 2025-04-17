package services;

import entities.Article;
import entities.CategorieArticle;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class CateArtService implements IService<CategorieArticle> {

    Connection con;
    public CateArtService() {
        con = utils.MyDataBase.getInstance().getConx();
    }

    @Override
    public List<CategorieArticle> displayList() throws SQLException {
        String query = "SELECT * FROM `categorie_article` ";
        List<CategorieArticle> catArts = new ArrayList<>();
        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery(query);
        while (rs.next()){
            CategorieArticle c =new CategorieArticle(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4));
            catArts.add(c);
        }

        return catArts;
    }

    @Override
    public void add(CategorieArticle categorieArticle) throws SQLException {
        String query = "INSERT INTO `categorie_article`(`nom_categorie`, `description_categorie`, `image_categorie`) VALUES (?,?,?)";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1,categorieArticle.getNom_categorie());
        ps.setString(2,categorieArticle.getDescription_categorie());
        ps.setString(3,categorieArticle.getImage_categorie());
        ps.executeUpdate();
        System.out.println("categorie article ajoutée");
    }


    @Override
    public void update(CategorieArticle categorieArticle) throws SQLException {
        String query = "UPDATE `categorie_article` SET `nom_categorie`= ?,`description_categorie`= ?,`image_categorie`= ? WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, categorieArticle.getNom_categorie());
        ps.setString(2, categorieArticle.getDescription_categorie());
        ps.setString(3, categorieArticle.getImage_categorie());
        ps.setInt(4, categorieArticle.getId());
        ps.executeUpdate();
        System.out.println("categorie article modifiée");
    }

    @Override
    public void delete(CategorieArticle categorieArticle)throws SQLException{
        // Suppression des articles liés à la catégorie
        String deleteArticlesQuery = "DELETE FROM `article` WHERE `categorie_id` = ?";
        PreparedStatement psArticles = con.prepareStatement(deleteArticlesQuery);
        psArticles.setInt(1, categorieArticle.getId());
        psArticles.executeUpdate();
        System.out.println("Articles supprimés pour la catégorie " + categorieArticle.getId());

        // Suppression de la catégorie
        String query = "DELETE FROM `categorie_article` WHERE `id` = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, categorieArticle.getId());
        ps.executeUpdate();
        System.out.println("Catégorie article supprimée");
    }
}
