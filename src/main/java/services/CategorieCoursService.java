package services;

import entities.CategorieCours;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieCoursService implements IService<CategorieCours> {



    Connection conx;
    Statement stm;

    public CategorieCoursService()
    {
        conx = MyDataBase.getInstance().getConx();

    }

    @Override
    public List<CategorieCours> displayList() throws SQLException {
        String query = "SELECT * FROM `categorie_cours`";
        List<CategorieCours> categories = new ArrayList<>();

        Statement stm = conx.createStatement();
        ResultSet rs = stm.executeQuery(query);

        while (rs.next()) {

            CategorieCours categorie = new CategorieCours(
                    rs.getInt("id"),
                    rs.getString("nom_categorie"),
                    rs.getString("description_categorie")
            );
            categories.add(categorie);
        }

        return categories;
    }

    @Override
    public void add(CategorieCours categorieCours) throws SQLException {

        String query = "INSERT INTO `categorie_cours`(`nom_categorie`, `description_categorie`) VALUES (?, ?)";


        PreparedStatement ps = conx.prepareStatement(query);
        ps.setString(1, categorieCours.getNomCategorie());
        ps.setString(2, categorieCours.getDescriptionCategorie());


        ps.executeUpdate();
        System.out.println("Categorie cours ajouté avec succès !");
    }

    @Override
    public void update(CategorieCours categorieCours) throws SQLException {
        String query = "UPDATE categorie_cours SET nom_categorie = ?, description_categorie = ? WHERE id = ?";

        try (PreparedStatement ps = conx.prepareStatement(query)) {
            ps.setString(1, categorieCours.getNomCategorie());
            ps.setString(2, categorieCours.getDescriptionCategorie());
            ps.setInt(3, categorieCours.getId());

            ps.executeUpdate();
            System.out.println("Catégorie cours mise à jour avec succès !");
        }
    }


    @Override
    public void delete(CategorieCours categorieCours) throws SQLException {
        // Supprimer les cours associés
        String deleteCoursQuery = "DELETE FROM cours WHERE categorie_c_id = ?";
        try (PreparedStatement psDeleteCours = conx.prepareStatement(deleteCoursQuery)) {
            psDeleteCours.setInt(1, categorieCours.getId());
            psDeleteCours.executeUpdate();
        }

        // Supprimer la catégorie
        String deleteCategorieQuery = "DELETE FROM categorie_cours WHERE id = ?";
        try (PreparedStatement psDeleteCategorie = conx.prepareStatement(deleteCategorieQuery)) {
            psDeleteCategorie.setInt(1, categorieCours.getId());
            psDeleteCategorie.executeUpdate();
            System.out.println("Catégorie cours supprimé avec succès !");
        }
    }




}


