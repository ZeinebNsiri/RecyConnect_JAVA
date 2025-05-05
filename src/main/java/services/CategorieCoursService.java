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
        // Désactiver l'autocommit pour gérer la transaction
        conx.setAutoCommit(false);

        try {
            // 1. Supprimer les notes (rating) liées aux cours de la catégorie
            String deleteRatingsQuery = "DELETE r FROM rating r " +
                    "INNER JOIN cours c ON r.cours_id = c.id " +
                    "WHERE c.categorie_c_id = ?";
            try (PreparedStatement psRatings = conx.prepareStatement(deleteRatingsQuery)) {
                psRatings.setInt(1, categorieCours.getId());
                psRatings.executeUpdate();
            }

            // 2. Supprimer les cours de la catégorie
            String deleteCoursQuery = "DELETE FROM cours WHERE categorie_c_id = ?";
            try (PreparedStatement psCours = conx.prepareStatement(deleteCoursQuery)) {
                psCours.setInt(1, categorieCours.getId());
                psCours.executeUpdate();
            }

            // 3. Supprimer la catégorie
            String deleteCategorieQuery = "DELETE FROM categorie_cours WHERE id = ?";
            try (PreparedStatement psCategorie = conx.prepareStatement(deleteCategorieQuery)) {
                psCategorie.setInt(1, categorieCours.getId());
                psCategorie.executeUpdate();
            }

            // Valider la transaction
            conx.commit();
            System.out.println("Catégorie, cours et notes associés supprimés !");

        } catch (SQLException e) {
            // Annuler en cas d'erreur
            conx.rollback();
            throw e;
        } finally {
            conx.setAutoCommit(true);
        }
    }





}


