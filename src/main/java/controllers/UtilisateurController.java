package controllers;

import entities.utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.UtilisateurService;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class UtilisateurController {

    @FXML
    private TableView<utilisateur> tableUtilisateur;
    @FXML
    private TableColumn<utilisateur, Integer> idColumn;
    @FXML
    private TableColumn<utilisateur, String> nomColumn;
    @FXML
    private TableColumn<utilisateur, String> prenomColumn;
    @FXML
    private TableColumn<utilisateur, String> emailColumn;
    @FXML
    private TableColumn<utilisateur, String> telColumn;
    @FXML
    private TableColumn<utilisateur, String> roleColumn;
    @FXML
    private TableColumn<utilisateur, String> matriculeColumn;
    @FXML
    private TableColumn<utilisateur, Boolean> etatColumn;

    private final UtilisateurService utilisateurService = new UtilisateurService();

    @FXML
    public void initialize() {
        try {
            tableUtilisateur.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            List<utilisateur> utilisateurs = utilisateurService.displayList();


            List<utilisateur> filtrés = utilisateurs.stream()
                    .filter(u -> {
                        String role = u.getRoles();
                        return role.contains("ROLE_USER") || role.contains("ROLE_PROFESSIONNEL");
                    })
                    .collect(Collectors.toList());

            ObservableList<utilisateur> data = FXCollections.observableArrayList(filtrés);

            // Colonnes classiques
            idColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getId()).asObject());
            nomColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNom_user()));
            prenomColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getPrenom()));
            emailColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getEmail()));
            telColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNum_tel()));
            matriculeColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getMatricule_fiscale()));

            // Affichage lisible des rôles
            roleColumn.setCellValueFactory(cell -> {
                String rawRole = cell.getValue().getRoles();
                String displayRole = "Inconnu";
                if (rawRole.contains("ROLE_ADMIN")) displayRole = "Administrateur";
                else if (rawRole.contains("ROLE_USER")) displayRole = "Particulier";
                else if (rawRole.contains("ROLE_PROF")) displayRole = "Professionnel";
                return new javafx.beans.property.SimpleStringProperty(displayRole);
            });

            // Toggle switch pour l'état
            etatColumn.setCellFactory(col -> new TableCell<>() {
                private final ToggleButton toggle = new ToggleButton();

                @Override
                protected void updateItem(Boolean status, boolean empty) {
                    super.updateItem(status, empty);
                    if (empty || status == null) {
                        setGraphic(null);
                    } else {
                        toggle.setSelected(status);
                        toggle.setStyle(toggle.isSelected()
                                ? "-fx-background-color: #14532d; -fx-background-radius: 15; -fx-text-fill: white;"
                                : "-fx-background-color: lightgray; -fx-background-radius: 15;");

                        toggle.setOnAction(e -> {
                            utilisateur u = getTableView().getItems().get(getIndex());
                            boolean newStatus = toggle.isSelected();
                            u.setStatus(newStatus);
                            if (u.getRoles().contains("Professionnel")) {
                                u.setRoles("ROLE_PROFESSIONNEL");
                            } else {
                                u.setRoles("ROLE_USER");
                            }
                            try {
                                utilisateurService.update(u); // Optionnel
                            } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                            }
                            toggle.setStyle(newStatus
                                    ? "-fx-background-color: #14532d; -fx-background-radius: 15; -fx-text-fill: white;"
                                    : "-fx-background-color: lightgray; -fx-background-radius: 15;");
                        });

                        setGraphic(toggle);
                    }
                }
            });

            etatColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleBooleanProperty(cell.getValue().isStatus()));
            tableUtilisateur.setItems(data);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
