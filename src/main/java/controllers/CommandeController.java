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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class CommandeController {

    private final CommandeService commandeService = new CommandeService();
    private final LigneCommandeService ligneCommandeService = new LigneCommandeService();

    private static final String PAIEMENT_LIVRAISON = "Payé par livraison";
    private static final String PAIEMENT_VISA = "Payé par VISA";

    @FXML
    private Button finaliserBtn;

    @FXML
    private RadioButton livraisonRadio;

    @FXML
    private RadioButton visaRadio;

    @FXML
    private void finaliserCommande() {
        try {
            // Vérification du mode de paiement sélectionné
            if (!livraisonRadio.isSelected() && !visaRadio.isSelected()) {
                showAlert(Alert.AlertType.WARNING, "Veuillez sélectionner un mode de paiement.");
                return;
            }

            // Utilisateur fictif (à remplacer par l'utilisateur connecté réel)
            utilisateur u = getUtilisateurConnecte();

            // Récupération de la commande en cours pour l'utilisateur
            Commande commande = commandeService.getCommandeEnCoursParUtilisateur(u.getId());
            if (commande == null) {
                commande = new Commande();
                commande.setDateCommande(LocalDateTime.now());
            }

            // Mise à jour du statut de la commande en fonction du mode de paiement
            String nouveauStatut = livraisonRadio.isSelected() ? PAIEMENT_LIVRAISON : PAIEMENT_VISA;
            commande.setStatut(nouveauStatut);
            commande.setPrixTotal(SessionPanier.getTotalPanier());

            // Créer ou mettre à jour la commande
            if (commande.getId() == 0) {
                int id = commandeService.addCommande(commande);
                commande.setId(id);
            } else {
                commandeService.updateCommande(commande);
            }

            // Associer les lignes de commande à la commande
            List<LigneCommande> lignes = ligneCommandeService.getLignesEnAttenteParUtilisateur(u.getId());
            for (LigneCommande ligne : lignes) {
                ligne.setEtat("confirmée");
                ligne.setCommandeId(commande.getId());
                ligneCommandeService.updateEtatEtCommande(ligne);
            }

            // Si le paiement est par VISA, traiter via Paymee
            if (visaRadio.isSelected()) {
                // Vérifier que le montant de la commande est supérieur à zéro avant de procéder
                if (commande.getPrixTotal() > 0) {
                    // Création du paiement via Paymee
                    Map<String, String> paymentData = PaymeeService.createPayment(
                            commande.getPrixTotal(),               // Montant de la commande
                            "Commande CMD-" + commande.getId(),    // Note de commande
                            u.getPrenom(),                         // Prénom de l'utilisateur
                            u.getNom_user(),                       // Nom de l'utilisateur
                            u.getEmail(),                          // Email de l'utilisateur
                            u.getNum_tel(),                        // Téléphone de l'utilisateur
                            "https://votre-webhook-url.tn",        // URL webhook pour recevoir des notifications
                            "CMD-" + commande.getId()              // ID de la commande pour le suivi
                    );

                    // Récupérer l'URL du paiement
                    String paymentUrl = paymentData.get("payment_url");

                    // Si l'URL du paiement est obtenue, ouvrir la page de paiement
                    if (paymentUrl != null) {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/paymee.fxml"));
                        Parent root = loader.load();
                        PaymeeController controller = loader.getController();
                        controller.init(paymentUrl); // Initialiser avec l'URL de paiement

                        Scene scene = new Scene(root);
                        Stage stage = new Stage();
                        stage.setTitle("Paiement sécurisé - Paymee");
                        stage.setScene(scene);
                        stage.show();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Impossible de créer le paiement Paymee. Veuillez réessayer.");
                    }
                } else {
                    // Si le montant est inférieur ou égal à zéro, afficher une erreur
                    showAlert(Alert.AlertType.ERROR, "Le montant de la commande doit être supérieur à zéro.");
                }

            } else {
                // Paiement par livraison
                SessionPanier.viderPanier(); // Vider le panier après la commande

                // Rediriger vers la page de confirmation de commande
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/commande.fxml"));
                Parent page = loader.load();
                Scene newScene = new Scene(page);
                Stage stage = (Stage) finaliserBtn.getScene().getWindow();
                stage.setScene(newScene);
                stage.show();
            }

        } catch (Exception e) {
            e.printStackTrace();

            // Gestion des erreurs
            if (e instanceof java.net.MalformedURLException) {
                showAlert(Alert.AlertType.ERROR, "L'URL de Paymee semble incorrecte.");
            } else if (e instanceof java.io.IOException) {
                showAlert(Alert.AlertType.ERROR, "Problème de connexion avec le serveur de paiement.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Une erreur est survenue lors du traitement de votre commande.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private utilisateur getUtilisateurConnecte() {
        // Remplacer par la vraie récupération de l'utilisateur connecté
        return new utilisateur(1, "exemple@mail.com", "Mnif", "Sahar",
                "ROLE_CLIENT", "12345678", "Tunis", "motdepasse123", true, "MF123456", "photo.jpg");
    }
}
