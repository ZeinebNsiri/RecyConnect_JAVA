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
import org.h2.util.json.JSONObject;
import services.CommandeService;
import services.LigneCommandeService;
import services.PaymeeService;
import utils.SessionPanier;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
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
    private void PayerCommande() {
        try {
            // Vérification du mode de paiement sélectionné
            if (!livraisonRadio.isSelected() && !visaRadio.isSelected()) {
                showAlert(Alert.AlertType.WARNING, "Veuillez sélectionner un mode de paiement.");
                return;
            }

            utilisateur u = getUtilisateurConnecte();

            // Stocker le total du panier une seule fois
            double totalPanier = SessionPanier.getTotalPanier();

            if (totalPanier <= 0) {
                showAlert(Alert.AlertType.ERROR, "Le montant de la commande doit être supérieur à zéro.");
                return;
            }

            // Récupérer ou créer la commande
            Commande commande = commandeService.getCommandeEnCoursParUtilisateur(u.getId());
            String nouveauStatut = livraisonRadio.isSelected() ? PAIEMENT_LIVRAISON : PAIEMENT_VISA;

            if (commande == null) {
                commande = new Commande();
                commande.setDateCommande(LocalDateTime.now());
                commande.setPrixTotal(totalPanier);
                commande.setStatut(nouveauStatut);
                commandeService.addCommande(commande);
            } else {
                commande.setPrixTotal(totalPanier);
                commande.setStatut(nouveauStatut);
                commandeService.updateCommande(commande);
            }

            // Mise à jour des lignes de commande
            List<LigneCommande> lignes = ligneCommandeService.getLignesEnAttenteParUtilisateur(u.getId());
            for (LigneCommande ligne : lignes) {
                ligne.setEtat("confirmée");
                ligne.setCommandeId(commande.getId());
                ligneCommandeService.updateEtatEtCommande(ligne);
            }

            // Traitement selon le mode de paiement
            if (visaRadio.isSelected()) {
                // Paiement via Paymee
                // Forcer un format décimal à 3 chiffres après la virgule
                // Forcer un format décimal à 3 chiffres après la virgule
                String montantFormate = String.format(Locale.US, "%.3f", totalPanier);

// Construction d'un objet JSON avec le montant
                org.json.JSONObject json = new org.json.JSONObject();
                json.put("amount", montantFormate);
                json.put("note", "Paiement test");

// Affichage pour debug
                System.out.println(json.toString());


// Appel à PaymeeService avec les bons paramètres (ajuste selon ta méthode réelle)
                Map<String, String> paymentData = PaymeeService.createPayment(
                        Double.parseDouble(montantFormate), // envoie le montant en double
                        "Commande CMD-" + commande.getId(),
                        u.getPrenom(),
                        u.getNom_user(),
                        u.getEmail(),
                        u.getNum_tel(),
                        "https://yourdomain.com/webhook",
                        "CMD-" + commande.getId()
                );


                String paymentUrl = paymentData.get("payment_url");



                if (paymentUrl != null) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/paymee.fxml"));
                    Parent root = loader.load();
                    PaymeeController controller = loader.getController();
                    controller.init(paymentUrl);

                    Stage stage = new Stage();
                    stage.setTitle("Paiement sécurisé - Paymee");
                    stage.setScene(new Scene(root));
                    stage.show();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Impossible de créer le paiement Paymee. Veuillez réessayer.");
                }

            } else {
                // Paiement à la livraison : vider le panier et passer à la page commande
                SessionPanier.viderPanier();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/commande.fxml"));
                Parent page = loader.load();
                Scene newScene = new Scene(page);
                Stage stage = (Stage) finaliserBtn.getScene().getWindow();
                stage.setScene(newScene);
                stage.show();
            }


        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof java.net.MalformedURLException) {
                showAlert(Alert.AlertType.ERROR, "L'URL du serveur de paiement est incorrecte.");
            } else if (e instanceof java.io.IOException) {
                showAlert(Alert.AlertType.ERROR, "Erreur de connexion avec le serveur.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Une erreur s’est produite lors du traitement de la commande.");
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
        // Simulation utilisateur connecté — à remplacer par l’authentification réelle
        return new utilisateur(1, "exemple@mail.com", "Mnif", "Sahar",
                "ROLE_CLIENT", "12345678", "Tunis", "motdepasse123", true, "MF123456", "photo.jpg");
    }

}

