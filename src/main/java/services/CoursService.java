package services;

import entities.Cours;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
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
        return List.of();
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
