package controllers;

import entities.LigneCommande;
import entities.Commande;
import entities.utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;
import services.CommandeService;
import services.LigneCommandeService;
import services.PaymeeService;
import utils.SessionPanier;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommandeController {

    private final CommandeService commandeService = new CommandeService();
    private final LigneCommandeService ligneCommandeService = new LigneCommandeService();

    private static final String PAIEMENT_LIVRAISON = "Payé par livraison";
    private static final String PAIEMENT_VISA = "Payé par VISA";
    private utilisateur user = utils.Session.getInstance().getCurrentUser();
    @FXML
    private Button finaliserBtn;

    @FXML
    private RadioButton livraisonRadio;

    @FXML
    private RadioButton visaRadio;

    @FXML
    private void PayerCommande() {
        try {
            if (!livraisonRadio.isSelected() && !visaRadio.isSelected()) {
                showAlert(Alert.AlertType.WARNING, "Veuillez sélectionner un mode de paiement.");
                return;
            }


            double totalPanier = SessionPanier.getTotalPanier();

            if (totalPanier <= 0) {
                showAlert(Alert.AlertType.ERROR, "Le montant de la commande doit être supérieur à zéro.");
                return;
            }

            // Déterminer ou créer la commande
            Commande commande = commandeService.getCommandeEnCoursParUtilisateur(user.getId());
            String nouveauStatut = livraisonRadio.isSelected() ? PAIEMENT_LIVRAISON : PAIEMENT_VISA;

            if (commande == null) {
                commande = new Commande();
                commande.setDateCommande(java.time.LocalDateTime.now());
                commande.setPrixTotal(totalPanier);
                commande.setStatut(nouveauStatut);
                commandeService.addCommande(commande);
            } else {
                commande.setPrixTotal(totalPanier);
                commande.setStatut(nouveauStatut);
                commandeService.updateCommande(commande);
            }

            // Mettre à jour les lignes de commande
            List<LigneCommande> lignes = ligneCommandeService.getLignesEnAttenteParUtilisateur(user.getId());
            for (LigneCommande ligne : lignes) {
                ligne.setEtat("confirmée");
                ligne.setCommandeId(commande.getId());
                ligneCommandeService.updateEtatEtCommande(ligne);
            }

            if (visaRadio.isSelected()) {
                // Appel réel à PaymeeService
                String amountStr = String.format(Locale.US, "%.3f", totalPanier);

                Map<String, String> result = PaymeeService.createPayment(
                        totalPanier,
                        "Commande #" + commande.getId(),
                        user.getPrenom(),
                        user.getNom_user(),
                        user.getEmail(),
                        "+216" + user.getNum_tel(),
                        "https://www.webhook_url.tn", // webhook
                        "CMD-" + commande.getId()
                );

                String token = result.get("payment_token");
                String urlPaiement = result.get("payment_url");

                System.out.println("Token: " + token);
                System.out.println("URL Paiement: " + urlPaiement);

                showAlert(Alert.AlertType.INFORMATION, "Commande créée. Redirection vers le paiement Paymee...");

                // Charger la vue WebView
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Paymee.fxml"));
                Parent root = loader.load();
                // Initialiser le contrôleur avec l'URL
                PaymeeController controller = loader.getController();
                controller.init(urlPaiement);

                // Afficher dans une nouvelle fenêtre
                Stage stage = new Stage();
                stage.setTitle("Paiement sécurisé via Paymee");
                stage.setScene(new Scene(root));
                stage.show();

            } else {
                showAlert(Alert.AlertType.INFORMATION, "Commande créée avec succès. Paiement à la livraison.");
            }


        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur lors du paiement : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
