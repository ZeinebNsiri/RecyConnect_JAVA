package services;

import entities.utilisateur;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {

    private Connection connection;

    public UserService() {
        connection = MyDataBase.getInstance().getConx();
    }

    public utilisateur getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            utilisateur user = new utilisateur();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setNom_user(rs.getString("nom_user"));
            user.setPrenom(rs.getString("prenom"));
            user.setRoles(rs.getString("roles"));
            user.setNum_tel(rs.getString("num_tel"));
            user.setAdresse(rs.getString("adresse"));
            user.setPassword(rs.getString("password"));
            user.setStatus(rs.getBoolean("status"));
            user.setMatricule_fiscale(rs.getString("matricule_fiscale"));
            user.setPhoto_profil(rs.getString("photo_profil"));
            return user;
        }
        return null;
    }
}
