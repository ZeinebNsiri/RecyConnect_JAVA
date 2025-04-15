package services;

import entities.Commande;
import utils.MyDataBase;

import java.sql.*;

public class CommandeService {

    private Connection conx;

    public CommandeService() {
        conx = MyDataBase.getInstance().getConx();
    }

    // Retirer le static pour rendre cette méthode non statique
    public int addCommande(Commande commande) throws SQLException {
        String query = "INSERT INTO `commande`(`date_commande`, `statut`, `prix_total`) VALUES (?, ?, ?)";
        PreparedStatement ps = conx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

        // Préparation de la requête d'insertion
        ps.setTimestamp(1, Timestamp.valueOf(commande.getDateCommande())); // date_commande
        ps.setString(2, commande.getStatut()); // statut
        ps.setDouble(3, commande.getPrixTotal()); // prix_total

        // Lancer l'insertion dans la base de données
        int rowsAffected = ps.executeUpdate();
        if (rowsAffected > 0) {
            // Si l'insertion est réussie, récupérer l'ID généré
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                commande.setId(generatedId); // Mise à jour de l'objet commande avec l'ID généré
                return generatedId;  // Retourner l'ID généré
            }
        }
        throw new SQLException("Échec de la création de la commande.");
    }

}
