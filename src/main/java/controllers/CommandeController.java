package controllers;

import entities.LigneCommande;
import entities.Commande;
import entities.utilisateur;
import services.CommandeService;
import services.LigneCommandeService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import utils.SessionPanier;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class CommandeController {

    private CommandeService commandeService = new CommandeService();
    private LigneCommandeService ligneCommandeService = new LigneCommandeService();

    @FXML
    private Button finaliserBtn;

    @FXML
    public void finaliserCommande(ActionEvent event) throws SQLException {
        utilisateur u = new utilisateur(
                1,                          // id
                "exemple@mail.com",         // email
                "Mnif",                     // nom_user
                "Sahar",                    // prenom
                "ROLE_CLIENT",              // roles
                "12345678",                 // num_tel
                "Tunis, Tunisie",           // adresse
                "motdepasse123",            // password
                true,                       // status
                "MF123456",                 // matricule_fiscale
                "photo.jpg"                 // photo_profil
        );


        // Créer une commande (il faut instancier la classe Commande, pas CommandeController)
        Commande commande = new Commande();
        commande.setDateCommande(LocalDateTime.now()); // Date actuelle
        commande.setStatut("En attente");  // Statut de la commande
        commande.setPrixTotal(SessionPanier.getTotalPanier()); // Prix total du panier

        // Ajouter la commande dans la base de données et récupérer l'ID
        int commandeId = commandeService.addCommande(commande); // Retourne l'ID de la commande insérée
        System.out.println("Commande ajoutée avec l'ID : " + commandeId);
        LigneCommandeService ligneCommandeService = new LigneCommandeService();
        List<LigneCommande> liste = ligneCommandeService.getLignesEnAttenteParUtilisateur(u.getId());
        // Associer chaque ligne de commande à cette commande
        for (LigneCommande ligne : liste) {
            ligne.setEtat("confirmée");
            ligneCommandeService.updateEtat(ligne); // Ajouter la ligne de commande à la base
        }

        // Redirection vers la page commande.fxml
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("commande.fxml"));
            Parent page = loader.load();
            Scene newScene = new Scene(page);
            Stage stage = (Stage) finaliserBtn.getScene().getWindow();
            stage.setScene(newScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Optionnellement, vider le panier après la commande
        SessionPanier.viderPanier();
    }
}