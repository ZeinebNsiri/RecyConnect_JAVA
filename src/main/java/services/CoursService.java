package services;

import entities.CategorieCours;
import entities.Cours;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CoursService implements IService<Cours> {

    Connection conx;

    public CoursService() {
        conx = MyDataBase.getInstance().getConx();
    }

    @Override
    public List<Cours> displayList() throws SQLException {
        List<Cours> coursList = new ArrayList<>();

        String query = "SELECT c.id, c.image_cours, c.titre_cours, c.description_cours, c.video, " +
                "cc.id AS cat_id, cc.nom_categorie, cc.description_categorie " +
                "FROM cours c " +
                "JOIN categorie_cours cc ON c.categorie_c_id = cc.id";

        Statement stm = conx.createStatement();
        ResultSet rs = stm.executeQuery(query);

        while (rs.next()) {
            int coursId         = rs.getInt("id");
            String imageCours   = rs.getString("image_cours");
            String titreCours   = rs.getString("titre_cours");
            String descCours    = rs.getString("description_cours");
            String video        = rs.getString("video");

            int catId           = rs.getInt("cat_id");
            String catNom       = rs.getString("nom_categorie");
            String catDesc      = rs.getString("description_categorie");

            CategorieCours cat = new CategorieCours(catId, catNom, catDesc);

            Cours cours = new Cours(coursId, imageCours, titreCours, cat, descCours, video);
            coursList.add(cours);
        }
        return coursList;
    }

    @Override
    public void add(Cours cours) throws SQLException {
        String query = "INSERT INTO cours (categorie_c_id, titre_cours, description_cours, video, image_cours) VALUES (?, ?, ?, ?, ?)";
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
        String query = "UPDATE cours SET categorie_c_id = ?, titre_cours = ?, description_cours = ?, video = ?, image_cours = ? WHERE id = ?";
        try (PreparedStatement ps = conx.prepareStatement(query)) {
            ps.setInt(1, cours.getCategorieCours().getId());
            ps.setString(2, cours.getTitreCours());
            ps.setString(3, cours.getDescriptionCours());
            ps.setString(4, cours.getVideo());
            ps.setString(5, cours.getImageCours());
            ps.setInt(6, cours.getId());
            ps.executeUpdate();
            System.out.println("Cours mis à jour !");
        }
    }

    @Override
    public void delete(Cours cours) throws SQLException {
        String query = "DELETE FROM cours WHERE id = ?";
        try (PreparedStatement ps = conx.prepareStatement(query)) {
            ps.setInt(1, cours.getId());
            ps.executeUpdate();
            System.out.println("Cours supprimé !");
        }
    }

    // ✨⭐ Rating methods below 👇

    public void addOrUpdateRating(int userId, int coursId, int note) throws SQLException {
        if (note == 0) {
            // reset only stars (not delete from db)
            System.out.println("Note remise à zéro côté interface (pas enregistrée)");
            return;
        }

        PreparedStatement check = conx.prepareStatement("SELECT id FROM rating WHERE user_id = ? AND cours_id = ?");
        check.setInt(1, userId);
        check.setInt(2, coursId);
        ResultSet rs = check.executeQuery();

        if (rs.next()) {
            int ratingId = rs.getInt("id");
            PreparedStatement update = conx.prepareStatement("UPDATE rating SET note = ?, date_rate = ? WHERE id = ?");
            update.setInt(1, note);
            update.setDate(2, Date.valueOf(LocalDate.now()));
            update.setInt(3, ratingId);
            update.executeUpdate();
            System.out.println("Note mise à jour !");
        } else {
            PreparedStatement insert = conx.prepareStatement("INSERT INTO rating (user_id, cours_id, note, date_rate) VALUES (?, ?, ?, ?)");
            insert.setInt(1, userId);
            insert.setInt(2, coursId);
            insert.setInt(3, note);
            insert.setDate(4, Date.valueOf(LocalDate.now()));
            insert.executeUpdate();
            System.out.println("Note ajoutée !");
        }
    }

    public void resetRatingStarsVisualOnly() {
        // This can be used if needed separately (currently the controller does it directly)
        System.out.println("Remise visuelle des étoiles faite");
    }

    public int getAverageRatingForCours(int coursId) {
        String query = "SELECT AVG(note) as avg_note FROM rating WHERE note > 0 AND cours_id = ?";
        try (PreparedStatement ps = conx.prepareStatement(query)) {
            ps.setInt(1, coursId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return (int) Math.round(rs.getDouble("avg_note"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
