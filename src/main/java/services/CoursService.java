package services;

import entities.CategorieCours;
import entities.Cours;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursService implements IService<Cours> {

    Connection conx;
    Statement stm;

    public CoursService()
    {
        conx = MyDataBase.getInstance().getConx();

    }

    @Override
    public List<Cours> displayList() throws SQLException {
        List<Cours> coursList = new ArrayList<>();
        // On fait un JOIN avec la table categorie_cours pour récupérer aussi le nom_categorie
        String query = "SELECT c.id, c.image_cours, c.titre_cours, c.description_cours, c.video, "
                + "cc.id AS cat_id, cc.nom_categorie, cc.description_categorie "
                + "FROM cours c "
                + "JOIN categorie_cours cc ON c.categorie_c_id = cc.id";

        Statement stm = conx.createStatement();
        ResultSet rs = stm.executeQuery(query);

        while (rs.next()) {
            // Récupération des colonnes du cours
            int coursId         = rs.getInt("id");
            String imageCours   = rs.getString("image_cours");
            String titreCours   = rs.getString("titre_cours");
            String descCours    = rs.getString("description_cours");
            String video        = rs.getString("video");

            // Récupération des colonnes de la catégorie (grâce au JOIN)
            int catId               = rs.getInt("cat_id");
            String catNom           = rs.getString("nom_categorie");
            String catDescription   = rs.getString("description_categorie");

            // Construire la catégorie complète
            CategorieCours cat = new CategorieCours(catId, catNom, catDescription);

            // Construire l'objet Cours (ordre : id, imageCours, titreCours, cat, descCours, video)
            Cours cours = new Cours(
                    coursId,
                    imageCours,
                    titreCours,
                    cat,
                    descCours,
                    video
            );

            coursList.add(cours);
        }
        return coursList;
    }



    @Override
    public void add(Cours cours) throws SQLException {
        String query = "INSERT INTO cours (categorie_c_id, titre_cours, description_cours, video, image_cours) "
                + "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = conx.prepareStatement(query);
        ps.setInt(1, cours.getCategorieCours().getId());
        ps.setString(2, cours.getTitreCours());
        ps.setString(3, cours.getDescriptionCours());
        ps.setString(4, cours.getVideo());
        ps.setString(5, cours.getImageCours());
        ps.executeUpdate();
        System.out.println("Cours ajouté avec succès !");
    }


    @Override
    public void update(Cours cours) throws SQLException {

    }

    @Override
    public void delete(Cours cours) throws SQLException {

    }
}
