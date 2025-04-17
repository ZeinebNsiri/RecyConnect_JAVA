package utils;

import entities.utilisateur;

public class Session {
    private static Session instance;
    private utilisateur currentUser;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void setCurrentUser(utilisateur user) {
        this.currentUser = user;
    }

    public utilisateur getCurrentUser() {
        return this.currentUser;
    }

    public void logout() {
        currentUser = null;
        instance = null;

    }
}