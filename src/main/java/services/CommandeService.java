package services;

import entities.Article;
import entities.Commande;
import entities.utilisateur;
import utils.MyDataBase;
import entities.LigneCommande;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.*;

public class CommandeService {

    private Connection conx;

    public CommandeService() {
        conx = MyDataBase.getInstance().getConx();
    }

    // Retirer le static pour rendre cette méthode non statique
    public Commande addCommande(Commande commande) {
        String sql = "INSERT INTO commande (date_commande, prix_total, statut) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, Timestamp.valueOf(commande.getDateCommande()));
            stmt.setDouble(2, commande.getPrixTotal());
            stmt.setString(3, commande.getStatut());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                commande.setId(rs.getInt(1)); // ✅ Très important
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return commande;
    }



    public void updateCommande(Commande commande) throws SQLException {
        String query = "UPDATE commande SET statut = ?, prix_total = ?, date_commande = ? WHERE id = ?";
        PreparedStatement ps = conx.prepareStatement(query);
        ps.setString(1, commande.getStatut());
        ps.setDouble(2, commande.getPrixTotal());
        ps.setTimestamp(3, Timestamp.valueOf(commande.getDateCommande()));
        ps.setInt(4, commande.getId());

        ps.executeUpdate();
    }
    public List<Commande> getCommandesAvecDetailsParUtilisateur() throws SQLException {
        List<Commande> commandes = new ArrayList<>();

        // Requête SQL pour récupérer toutes les commandes et leurs lignes
        String query = "SELECT c.id AS commande_id, c.date_commande, c.prix_total, c.statut, " +
                "lc.id AS ligne_commande_id, lc.quantite_c, lc.prix_c, " +
                "a.nom_article, u.nom_user " +
                "FROM commande c " +
                "JOIN ligne_commande lc ON c.id = lc.commande_id_id " +
                "JOIN article a ON lc.article_c_id = a.id " +
                "JOIN utilisateur u ON lc.user_c_id = u.id";


            PreparedStatement stmt = conx.prepareStatement(query) ;

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Récupérer les informations de chaque commande
                Commande commande = new Commande();
                commande.setId(rs.getInt("commande_id"));
                commande.setDateCommande(rs.getTimestamp("date_commande").toLocalDateTime());
                commande.setPrixTotal(rs.getDouble("prix_total"));
                commande.setStatut(rs.getString("statut"));

                // Créer la ligne de commande associée
                LigneCommande ligneCommande = new LigneCommande();
                ligneCommande.setId(rs.getInt("ligne_commande_id"));
                ligneCommande.setQuantite(rs.getInt("quantite_c"));
                ligneCommande.setPrix(rs.getDouble("prix_c"));

                Article article = new Article();
                article.setNom_article(rs.getString("nom_article"));
                ligneCommande.setArticle(article);

                utilisateur user = new utilisateur();
                user.setNom_user(rs.getString("nom_user"));
                ligneCommande.setUtilisateur(user);

                // Afficher directement les détails (impression ou ajout à la liste)
                // Ajout de la ligne de commande à la commande
                commande.getLigneCommandes().add(ligneCommande);

                // Ajouter la commande à la liste de commandes (si vous ne voulez pas de doublons de commandes, vérifiez d'abord)
                commandes.add(commande);
            }
            System.out.println(commandes);


        return commandes;  // Retourne toutes les commandes récupérées
    }



    public void updateStatutCommande(int idCommande, String nouveauStatut) {
        try (Connection connection = MyDataBase.getInstance().getConx()) {
            String sql = "UPDATE commande SET statut = ? WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, nouveauStatut);
            ps.setInt(2, idCommande);
            ps.executeUpdate();
            System.out.println("Statut de la commande mis à jour !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Commande getCommandeEnCoursParUtilisateur(int userId) throws SQLException {
        String sql = "SELECT c.* FROM commande c INNER JOIN ligne_commande lc ON lc.commande_id_id = c.id WHERE c.statut = 'En attente' AND lc.user_c_id = ? ORDER BY c.date_commande DESC LIMIT 1";

        try (PreparedStatement ps = conx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Commande commande = new Commande();
                commande.setId(rs.getInt("id"));
                commande.setDateCommande(rs.getTimestamp("date_commande").toLocalDateTime());
                commande.setStatut(rs.getString("statut"));
                commande.setPrixTotal(rs.getDouble("prix_total"));
                return commande;
            }
            return null;
        }

    }
    public List<Commande> getAllCommandes() {
        List<Commande> commandes = new ArrayList<>();

        String sql = "SELECT * FROM commande";
        try (PreparedStatement ps = conx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Commande cmd = new Commande();
                cmd.setId(rs.getInt("id"));
                cmd.setDateCommande(rs.getTimestamp("date_commande").toLocalDateTime());
                cmd.setPrixTotal(rs.getDouble("prix_total"));
                cmd.setStatut(rs.getString("statut"));
                commandes.add(cmd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return commandes;
    }




}