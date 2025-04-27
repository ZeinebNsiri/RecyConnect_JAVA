package controllers;

import entities.utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import services.UtilisateurService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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

    // Nouveaux éléments pour la recherche
    @FXML private TextField searchEmail;
    @FXML private TextField searchTel;
    @FXML private ComboBox<String> searchRole;
    @FXML private Button btnSearch;
    @FXML private Button btnReset;

    // Nouveaux éléments pour la pagination
    @FXML private Button btnPrevious;
    @FXML private Button btnNext;
    @FXML private HBox paginationContainer;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private List<utilisateur> allUsers;
    private List<utilisateur> filteredUsers;

    // Variables pour la pagination
    private int currentPage = 1;
    private int totalPages = 1;
    private int rowsPerPage = 5; // Par défaut à 5 éléments par page
    private static final int MAX_PAGE_BUTTONS = 5;

    @FXML
    public void initialize() {
        try {
            tableUtilisateur.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            // Configuration des colonnes
            idColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        int indexOnPage = getIndex();
                        int absoluteIndex = (currentPage - 1) * MAX_PAGE_BUTTONS + indexOnPage + 1;
                        setText(String.valueOf(absoluteIndex));
                    }
                }
            });

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

            etatColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleBooleanProperty(cell.getValue().isStatus()));

            // Cell factory pour etatColumn avec toggle switch
            etatColumn.setCellFactory(col -> new TableCell<>() {
                private final StackPane toggleContainer = new StackPane();
                private final Circle knob = new Circle(10); // Cercle pour le switch
                private final Rectangle background = new Rectangle(40, 20); // Rectangle pour le fond du switch

                {
                    // Configuration du switch
                    background.setArcWidth(20);
                    background.setArcHeight(20);
                    knob.setFill(Color.WHITE);
                    knob.setEffect(new DropShadow(2, Color.GRAY));

                    toggleContainer.getChildren().addAll(background, knob);
                    toggleContainer.setMaxWidth(44);
                    toggleContainer.setPrefWidth(44);
                    toggleContainer.setPrefHeight(26);
                    toggleContainer.setCursor(Cursor.HAND);
                    setAlignment(Pos.CENTER);
                }

                @Override
                protected void updateItem(Boolean status, boolean empty) {
                    super.updateItem(status, empty);
                    if (empty || status == null) {
                        setGraphic(null);
                    } else {
                        updateToggleStyle(status, false);
                        toggleContainer.setOnMouseClicked(e -> {
                            utilisateur u = getTableView().getItems().get(getIndex());
                            boolean newStatus = !u.isStatus();
                            u.setStatus(newStatus);

                            // Préserver le rôle lors de la mise à jour
                            if (u.getRoles().contains("Professionnel")) {
                                u.setRoles("ROLE_PROFESSIONNEL");
                            } else {
                                u.setRoles("ROLE_USER");
                            }

                            try {
                                utilisateurService.update(u);
                                updateToggleStyle(newStatus, true);
                                getTableView().refresh();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                showAlert("Erreur", "Impossible de mettre à jour le statut.");
                            }
                        });
                        setGraphic(toggleContainer);
                    }
                }

                private void updateToggleStyle(boolean status, boolean animate) {
                    if (status) {
                        background.setFill(Color.web("#14532d"));
                        if (animate) {
                            TranslateTransition tt = new TranslateTransition(Duration.millis(100), knob);
                            tt.setToX(12);
                            tt.play();
                        } else {
                            knob.setTranslateX(12);
                        }
                    } else {
                        background.setFill(Color.LIGHTGRAY);
                        if (animate) {
                            TranslateTransition tt = new TranslateTransition(Duration.millis(100), knob);
                            tt.setToX(-12);
                            tt.play();
                        } else {
                            knob.setTranslateX(-12);
                        }
                    }
                }
            });

            // Initialisation du filtre global
            globalFilter.setItems(FXCollections.observableArrayList(
                    "Tous les utilisateurs",
                    "Les particuliers",
                    "Les professionnels",
                    "Les comptes activé",
                    "Les comptes désactivé"
            ));
            globalFilter.setValue("Tous les utilisateurs");
            globalFilter.setOnAction(e -> {
                appliquerFiltre();
                refreshPagination();
            });

            // Initialisation du combobox pour la recherche par rôle
            searchRole.setItems(FXCollections.observableArrayList(
                    "Tous les rôles",
                    "Particulier",
                    "Professionnel"
            ));
            searchRole.setValue("Tous les rôles");


            // Chargement des données
            allUsers = utilisateurService.displayList().stream()
                    .filter(u -> u.getRoles().contains("ROLE_USER") || u.getRoles().contains("ROLE_PROFESSIONNEL"))
                    .collect(Collectors.toList());

            filteredUsers = new ArrayList<>(allUsers);
            refreshPagination();
            loadTableData();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données des utilisateurs.");
        }
    }

    private void appliquerFiltre() {
        String selected = globalFilter.getValue();

        filteredUsers = allUsers.stream().filter(u -> {
            return switch (selected) {
                case "Les particuliers" -> u.getRoles().contains("ROLE_USER");
                case "Les professionnels" -> u.getRoles().contains("ROLE_PROFESSIONNEL");
                case "Les comptes activé" -> u.isStatus();
                case "Les comptes désactivé" -> !u.isStatus();
                default -> true; // "Tous les utilisateurs"
            };
        }).collect(Collectors.toList());

        currentPage = 1; // Retour à la première page lors d'un filtrage
        loadTableData();
    }

    @FXML
    private void handleSearch() {
        String email = searchEmail.getText().trim().toLowerCase();
        String tel = searchTel.getText().trim();
        String role = searchRole.getValue();

        try {
            filteredUsers = utilisateurService.searchUtilisateurs(email, tel, role);
            currentPage = 1;
            refreshPagination();
            loadTableData();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la recherche");
        }
    }


    @FXML
    private void handlePrevious() {
        if (currentPage > 1) {
            currentPage--;
            loadTableData();
            updatePaginationButtons();
        }
    }

    @FXML
    private void handleNext() {
        if (currentPage < totalPages) {
            currentPage++;
            loadTableData();
            updatePaginationButtons();
        }
    }

    private void handlePageButtonClick(int page) {
        currentPage = page;
        loadTableData();
        updatePaginationButtons();
    }

    private void refreshPagination() {
        totalPages = (int) Math.ceil((double) filteredUsers.size() / rowsPerPage);
        if (totalPages == 0) totalPages = 1; // Au moins une page même vide

        // Assurer que la page courante est valide
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        updatePaginationButtons();
    }

    private void updatePaginationButtons() {
        paginationContainer.getChildren().clear();

        // Déterminer la plage de boutons à afficher
        int startPage = Math.max(1, currentPage - (MAX_PAGE_BUTTONS / 2));
        int endPage = Math.min(totalPages, startPage + MAX_PAGE_BUTTONS - 1);

        // Ajuster la plage si nécessaire
        if (endPage - startPage + 1 < MAX_PAGE_BUTTONS) {
            startPage = Math.max(1, endPage - MAX_PAGE_BUTTONS + 1);
        }

        for (int i = startPage; i <= endPage; i++) {
            Button pageButton = new Button(String.valueOf(i));
            int pageNum = i;

            pageButton.getStyleClass().add(i == currentPage ? "btn-pagination-active" : "btn-pagination");


            pageButton.getStylesheets().add(getClass().getResource("/AffichageUsers.css").toExternalForm());

            pageButton.setOnAction(e -> handlePageButtonClick(pageNum));
            paginationContainer.getChildren().add(pageButton);
        }


        // Activer/désactiver les boutons précédent/suivant
        btnPrevious.setDisable(currentPage == 1);
        btnNext.setDisable(currentPage == totalPages);
    }

    private void loadTableData() {
        int fromIndex = (currentPage - 1) * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, filteredUsers.size());

        List<utilisateur> pageItems;
        if (fromIndex < toIndex) {
            pageItems = filteredUsers.subList(fromIndex, toIndex);
        } else {
            pageItems = new ArrayList<>(); // Page vide
        }

        tableUtilisateur.setItems(FXCollections.observableArrayList(pageItems));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    private void Statistiques() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BaseAdmin.fxml"));
            Parent root = loader.load();
            BaseAdminController baseAdminController = loader.getController();
            baseAdminController.showStatistiquesUserView();

            btnNext.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}