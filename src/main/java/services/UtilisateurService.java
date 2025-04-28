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

            utilisateur user =new utilisateur(rs.getInt(1),rs.getString(2),rs.getString(4),rs.getString(5),rs.getString("roles"),rs.getString(6),rs.getString(7),rs.getString(8),rs.getBoolean(10),rs.getString(9),rs.getString(11), rs.getString(12));
            utilisateurs.add(user);
        }
        return utilisateurs;
    }

    @Override
    public void add(utilisateur utilisateur) throws SQLException {
        String query = "INSERT INTO `utilisateur`(`email`, `roles`, `nom_user`, `prenom`, `num_tel`, `password`, `matricule_fiscale`, `status`,`face_image`) VALUES (?,?,?,?,?,?,?,?,?)";
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
        ps.setString(9, utilisateur.getFace_image());


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
        String query = "UPDATE `utilisateur` SET `email`=?, `roles`=?, `nom_user`=?, `prenom`=?, `num_tel`=?, `password`=?, `matricule_fiscale`=?, `status`=? , `photo_profil`=?,`adresse`=?,`bannedBy`=? WHERE `id`=?";
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
        ps.setString(10, utilisateur.getAdresse());
        ps.setString(11,utilisateur.getBannedBy());
        ps.setInt(12, utilisateur.getId());

        ps.executeUpdate();
        System.out.println("Utilisateur updated successfully!");
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    }

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

    public List<utilisateur> searchUtilisateurs(String email, String tel, String role) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM utilisateur WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // Ajouter des filtres selon les critères fournis
        if (email != null && !email.isEmpty()) {
            sql.append(" AND LOWER(email) LIKE ?");
            params.add("%" + email.toLowerCase() + "%");
        }

        if (tel != null && !tel.isEmpty()) {
            sql.append(" AND num_tel LIKE ?");
            params.add("%" + tel + "%");
        }

        if (role != null && !role.equals("Tous les rôles")) {
            if (role.equals("Particulier")) {
                sql.append(" AND roles LIKE ?");
                params.add("%ROLE_USER%");
            } else if (role.equals("Professionnel")) {
                sql.append(" AND roles LIKE ?");
                params.add("%ROLE_PROFESSIONNEL%");
            }
        }

        // Filtrer pour n'avoir que les utilisateurs et professionnels
        sql.append(" AND (roles LIKE ? OR roles LIKE ?)");
        params.add("%ROLE_USER%");
        params.add("%ROLE_PROFESSIONNEL%");

        List<utilisateur> utilisateurs = new ArrayList<>();
        PreparedStatement stmt = conx.prepareStatement(sql.toString());

        // Définir les paramètres de la requête
        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            utilisateur user = new utilisateur(
                    rs.getInt(1),
                    rs.getString(2),
                    rs.getString(4),
                    rs.getString(5),
                    rs.getString("roles"),
                    rs.getString(6),
                    rs.getString(7),
                    rs.getString(8),
                    rs.getBoolean(10),
                    rs.getString(9),
                    rs.getString(11)
            );
            utilisateurs.add(user);
        }

        return utilisateurs;
    }
    public int getCountByStatus(boolean status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE status = ? AND (roles LIKE ? OR roles LIKE ?)";
        try (PreparedStatement stmt = conx.prepareStatement(sql)) {
            stmt.setBoolean(1, status);
            stmt.setString(2,"%ROLE_USER%");
            stmt.setString(3,"%ROLE_PROFESSIONNEL%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getCountByRole(String role) throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE roles LIKE ?";
        try (PreparedStatement stmt = conx.prepareStatement(sql)) {
            stmt.setString(1, "%" + role + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public void banUser(int userId) throws SQLException {
        String updateStatusQuery = "UPDATE utilisateur SET status = false, ban_time = ? ,bannedBy = ?  WHERE id = ?";
        PreparedStatement ps = conx.prepareStatement(updateStatusQuery);


        long currentTime = System.currentTimeMillis();
        long banDuration = (2 * 60 * 60 * 1000) + (15 * 60 * 1000);
        long banUntil = currentTime + banDuration;

        ps.setLong(1, banUntil);
        ps.setString(2,"USER");
        ps.setInt(3, userId);

        ps.executeUpdate();
    }
    public boolean ReactiverUser (int userId) throws SQLException {
        String query = "SELECT ban_time , bannedBy FROM utilisateur WHERE id = ? AND status = false ";
        PreparedStatement ps = conx.prepareStatement(query);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();


        if (rs.next()) {
            String bannedBy = rs.getString("bannedBy");
            if(bannedBy.equals("ADMIN")) {
                return false;
            }

            long banTime = rs.getLong("ban_time");
            long currentTime = System.currentTimeMillis();


            if (banTime <= currentTime) {

                String updateStatusQuery = "UPDATE utilisateur SET status = true WHERE id = ?";
                PreparedStatement updatePs = conx.prepareStatement(updateStatusQuery);
                updatePs.setInt(1, userId);
                updatePs.executeUpdate();

                System.out.println("Compte réactivé.");
                return true;
            }
            return false;
        }
        return true;
    }


}
