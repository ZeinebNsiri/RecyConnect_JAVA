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
    public List<Commande> getCommandesAvecDetailsParUtilisateur(int userId) throws SQLException {
        List<Commande> commandes = new ArrayList<>();

        String query = "SELECT c.id AS commande_id, c.date_commande, c.prix_total, c.statut, " +
                "lc.id AS ligne_commande_id, lc.quantite_c, lc.prix_c, " +
                "a.nom_article, u.nom_user " +
                "FROM commande c " +
                "JOIN ligne_commande lc ON c.id = lc.commande_id_id " +
                "JOIN article a ON lc.article_c_id = a.id " +
                "JOIN utilisateur u ON lc.user_c_id = u.id " +
                "WHERE lc.user_c_id = ?";

        try (Connection conn = MyDataBase.getInstance().getConx();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            Map<Integer, Commande> commandeMap = new HashMap<>();

            while (rs.next()) {
                int commandeId = rs.getInt("commande_id");

                Commande commande = commandeMap.get(commandeId);
                if (commande == null) {
                    commande = new Commande();
                    commande.setId(commandeId);
                    commande.setDateCommande(rs.getTimestamp("date_commande").toLocalDateTime());
                    commande.setPrixTotal(rs.getDouble("prix_total"));
                    commande.setStatut(rs.getString("statut"));
                    commande.setLigneCommandes(new ArrayList<>()); // initialize the list

                    commandeMap.put(commandeId, commande);
                }

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

                commande.getLigneCommandes().add(ligneCommande);
            }

            commandes.addAll(commandeMap.values());
        }

        return commandes;
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



}