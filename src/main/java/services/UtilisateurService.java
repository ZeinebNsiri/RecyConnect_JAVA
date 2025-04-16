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

            utilisateur user =new utilisateur(rs.getInt(1),rs.getString(2),rs.getString(4),rs.getString(5),rs.getString("roles"),rs.getString(6),rs.getString(7),rs.getString(8),rs.getBoolean(10),rs.getString(9),rs.getString(11));
            utilisateurs.add(user);
        }
        return utilisateurs;
    }

    @Override
    public void add(utilisateur utilisateur) throws SQLException {
        String query = "INSERT INTO `utilisateur`(`email`, `roles`, `nom_user`, `prenom`, `num_tel`, `password`, `matricule_fiscale`, `status`) VALUES (?,?,?,?,?,?,?,?)";
        PreparedStatement ps = conx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        String pass = BCrypt.hashpw(utilisateur.getPassword(), BCrypt.gensalt(13));
        pass = pass.replaceFirst("^\\$2a\\$", "\\$2y\\$");
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
        String query = "UPDATE `utilisateur` SET `email`=?, `roles`=?, `nom_user`=?, `prenom`=?, `num_tel`=?, `password`=?, `matricule_fiscale`=?, `status`=? , `photo_profil`=? WHERE `id`=?";
        PreparedStatement ps = conx.prepareStatement(query);

        ps.setString(1, utilisateur.getEmail());
        ps.setString(2, utilisateur.getRoles());
        ps.setString(3, utilisateur.getNom_user());
        ps.setString(4, utilisateur.getPrenom());
        ps.setString(5, utilisateur.getNum_tel());
        ps.setString(6, utilisateur.getPassword());
        ps.setString(7, utilisateur.getMatricule_fiscale());
        ps.setBoolean(8, utilisateur.isStatus());
        ps.setString(9, utilisateur.getPhoto_profil());
        ps.setInt(10, utilisateur.getId());

        ps.executeUpdate();
        System.out.println("Utilisateur updated successfully!");
    }
    // Vérifie si l'email est valide
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    }


    // Vérifie si le numéro est exactement 8 chiffres
    public static boolean isValidPhoneNumber(String phone) {
        return phone.matches("^\\d{8}$");
    }

    public static boolean isValidMatriculeFiscale(String matricule) {
        return matricule != null && matricule.matches("^\\d{13}$");
    }

    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;

        boolean Upper = password.matches(".*[A-Z].*");
        boolean Num = password.matches(".*\\d.*");
        boolean Symbol = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
        boolean Lower = password.matches(".*[a-z].*");
        return Upper && Num && Symbol && Lower;
    }

    // Affiche les erreurs (facultatif)
    public  String getPasswordError(String password) {
        if (password == null || password.length() < 8)
            return "Le mot de passe doit contenir au moins 8 caractères.";
        if (!password.matches(".*[A-Z].*"))
            return "Le mot de passe doit contenir au moins une majuscule.";
        if (!password.matches(".*[a-z].*"))
            return "Le mot de passe doit contenir au moins une minuscule.";
        if (!password.matches(".*\\d.*"))
            return "Le mot de passe doit contenir au moins un chiffre.";
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*"))
            return "Le mot de passe doit contenir au moins un symbole.";
        return "";
    }
}
