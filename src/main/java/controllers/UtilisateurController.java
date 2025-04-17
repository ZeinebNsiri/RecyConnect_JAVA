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

    @FXML private TableView<utilisateur> tableUtilisateur;
    @FXML private TableColumn<utilisateur, Integer> idColumn;
    @FXML private TableColumn<utilisateur, String> nomColumn;
    @FXML private TableColumn<utilisateur, String> prenomColumn;
    @FXML private TableColumn<utilisateur, String> emailColumn;
    @FXML private TableColumn<utilisateur, String> telColumn;
    @FXML private TableColumn<utilisateur, String> roleColumn;
    @FXML private TableColumn<utilisateur, String> matriculeColumn;
    @FXML private TableColumn<utilisateur, Boolean> etatColumn;
    @FXML private ComboBox<String> globalFilter;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private List<utilisateur> allUsers;

    @FXML
    public void initialize() {
        try {
            tableUtilisateur.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            idColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getId()).asObject());
            nomColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNom_user()));
            prenomColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getPrenom()));
            emailColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getEmail()));
            telColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNum_tel()));
            matriculeColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getMatricule_fiscale()));

            roleColumn.setCellValueFactory(cell -> {
                String role = cell.getValue().getRoles();
                String display = role.contains("PROFESSIONNEL") ? "Professionnel" :
                        role.contains("USER") ? "Particulier" : "Inconnu";
                return new javafx.beans.property.SimpleStringProperty(display);
            });


            etatColumn.setCellFactory(col -> new TableCell<>() {
                private final ToggleButton toggle = new ToggleButton();

                @Override
                protected void updateItem(Boolean status, boolean empty) {
                    super.updateItem(status, empty);
                    if (empty || status == null) {
                        setGraphic(null);
                    } else {
                        toggle.setSelected(status);
                        updateToggleStyle(status);

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
                                utilisateurService.update(u);
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }

                            updateToggleStyle(newStatus);
                        });

                        setGraphic(toggle);
                    }
                }

                private void updateToggleStyle(boolean status) {
                    toggle.setStyle(status
                            ? "-fx-background-color: #14532d; -fx-background-radius: 15; -fx-text-fill: white;"
                            : "-fx-background-color: lightgray; -fx-background-radius: 15;");
                }
            });

            etatColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleBooleanProperty(cell.getValue().isStatus()));


            allUsers = utilisateurService.displayList().stream()
                    .filter(u -> u.getRoles().contains("ROLE_USER") || u.getRoles().contains("ROLE_PROFESSIONNEL"))
                    .collect(Collectors.toList());

            tableUtilisateur.setItems(FXCollections.observableArrayList(allUsers));


            globalFilter.setItems(FXCollections.observableArrayList(
                    "Tous les utilisateurs",
                    "Les particuliers",
                    "Les professionnels",
                    "Les comptes activé",
                    "Les comptes désactivé"
            ));
            globalFilter.setValue("Tous les utilisateurs");
            globalFilter.setOnAction(e -> appliquerFiltre());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void appliquerFiltre() {
        String selected = globalFilter.getValue();

        List<utilisateur> filtrés = allUsers.stream().filter(u -> {
            return switch (selected) {
                case "Les particuliers" -> u.getRoles().contains("ROLE_USER");
                case "Les professionnels" -> u.getRoles().contains("ROLE_PROFESSIONNEL");
                case "Les comptes activé" -> u.isStatus();
                case "Les comptes désactivé" -> !u.isStatus();
                default -> true; // "Tous les utilisateurs"
            };
        }).collect(Collectors.toList());

        tableUtilisateur.setItems(FXCollections.observableArrayList(filtrés));
    }
}
