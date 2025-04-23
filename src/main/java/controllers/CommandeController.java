package controllers;

import entities.LigneCommande;
import entities.Commande;
import entities.utilisateur;
import javafx.scene.control.RadioButton;
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
import javafx.scene.control.Alert;
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
    private RadioButton livraisonRadio;

    @FXML
    private RadioButton visaRadio;


    @FXML
    public void finaliserCommande(ActionEvent event) throws SQLException {
        // ✅ Vérifier si un mode de paiement est sélectionné
        if (!livraisonRadio.isSelected() && !visaRadio.isSelected()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Mode de paiement requis");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un mode de paiement avant de continuer.");
            alert.showAndWait();
            return;
        }

        // ✅ Utilisateur fictif (à remplacer par session utilisateur réelle)
        utilisateur u = new utilisateur(
                1,
                "exemple@mail.com",
                "Mnif",
                "Sahar",
                "ROLE_CLIENT",
                "12345678",
                "Tunis, Tunisie",
                "motdepasse123",
                true,
                "MF123456",
                "photo.jpg"
        );

        // Création de la commande
        Commande commande = new Commande();
        commande.setDateCommande(LocalDateTime.now());

        // Définir le statut en fonction du choix utilisateur
        if (livraisonRadio.isSelected()) {
            commande.setStatut("Payé par livraison");
        } else if (visaRadio.isSelected()) {
            commande.setStatut("Payé par VISA");
        }

        commande.setPrixTotal(SessionPanier.getTotalPanier());

        // Sauvegarder la commande
        int commandeId = commandeService.addCommande(commande);
        System.out.println("Commande ajoutée avec l'ID : " + commandeId);

        // Mise à jour des lignes de commande
        LigneCommandeService ligneCommandeService = new LigneCommandeService();
        List<LigneCommande> liste = ligneCommandeService.getLignesEnAttenteParUtilisateur(u.getId());
        for (LigneCommande ligne : liste) {
            ligne.setEtat("confirmée");
            ligneCommandeService.updateEtat(ligne);
        }


        //  Redirection vers commande.fxml
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

        // Vider le panier
        SessionPanier.viderPanier();
    }


}