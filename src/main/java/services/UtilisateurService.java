package services;

import entities.utilisateur;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;
import org.mindrot.jbcrypt.BCrypt;

public class UtilisateurService implements IService<utilisateur> {

    Connection conx;
    Statement stm;

    public UtilisateurService() {
        conx = MyDataBase.getInstance().getConx();
    }

    @Override
    public List<utilisateur> displayList()throws SQLException {
        String sql = "SELECT * FROM utilisateur";
        List<utilisateur> utilisateurs = new ArrayList<>();
        Statement stm = conx.createStatement();
        ResultSet rs = stm.executeQuery(sql);
        while (rs.next()){
            //utilisateur user =new user(rs.getInt(1),rs.getString("name"),rs.getString("lastname"));
            // utilisateurs.add(user);
        }
        return utilisateurs;
    }

    @Override
    public void add(utilisateur utilisateur) throws SQLException {
        String query = "INSERT INTO `utilisateur`(`email`, `roles`, `nom_user`, `prenom`, `num_tel`, `password`, `matricule_fiscale`, `status`) VALUES (?,?,?,?,?,?,?,?)";
        PreparedStatement ps = conx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        String pass = BCrypt.hashpw(utilisateur.getPassword(), BCrypt.gensalt(13));
        pass = pass.replaceFirst("^\\$2y\\$", "\\$2a\\$");
        ps.setString(1, utilisateur.getEmail());
        ps.setString(2, utilisateur.getRoles());
        ps.setString(3, utilisateur.getNom_user());
        ps.setString(4, utilisateur.getPrenom());
        ps.setString(5, utilisateur.getNum_tel());
        ps.setString(6, pass);
        ps.setString(7, utilisateur.getMatricule_fiscale());
        ps.setBoolean(8, utilisateur.isStatus());

        ps.executeUpdate();
        System.out.println("Utilisateur added successfully!");

        ResultSet rs = ps.getGeneratedKeys();
        int generatedId = -1;
        if (rs.next()) {
            generatedId = rs.getInt(1);
            utilisateur.setId(generatedId); // Mettre à jour l'objet utilisateur avec l'ID généré
        }

        System.out.println("Utilisateur added successfully with ID: " + generatedId);
    }


    @Override
    public void delete(utilisateur utilisateur) throws SQLException {
        String query = "DELETE FROM `utilisateur` WHERE `email`=?";
        PreparedStatement ps = conx.prepareStatement(query);

        ps.setString(1, utilisateur.getEmail());

        ps.executeUpdate();
        System.out.println("Utilisateur deleted successfully!");
    }

    @Override
    public void update(utilisateur utilisateur) throws SQLException {
        String query = "UPDATE `utilisateur` SET `email`=?, `roles`=?, `nom_user`=?, `prenom`=?, `num_tel`=?, `password`=?, `matricule_fiscale`=?, `status`=? WHERE `id`=?";
        PreparedStatement ps = conx.prepareStatement(query);

        ps.setString(1, utilisateur.getEmail());
        ps.setString(2, utilisateur.getRoles());
        ps.setString(3, utilisateur.getNom_user());
        ps.setString(4, utilisateur.getPrenom());
        ps.setString(5, utilisateur.getNum_tel());
        ps.setString(6, utilisateur.getPassword());
        ps.setString(7, utilisateur.getMatricule_fiscale());
        ps.setBoolean(8, utilisateur.isStatus());
        ps.setInt(9, utilisateur.getId());

        ps.executeUpdate();
        System.out.println("Utilisateur updated successfully!");
    }
}
