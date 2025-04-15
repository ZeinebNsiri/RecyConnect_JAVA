package entities;

import java.util.List;

public class utilisateur {
    private int id;
    private String email;
    private String nom_user;
    private String prenom;
    private String roles;
    private String num_tel;
    private String adresse;
    private String password;
    private boolean status;
    private String matricule_fiscale;
    private String photo_profil;


    public utilisateur(int id, String email, String nom_user, String prenom, String roles, String num_tel, String adresse, boolean status, String matricule_fiscale, String photo_profil) {
        this.id = id;
        this.email = email;
        this.nom_user = nom_user;
        this.prenom = prenom;
        this.roles = roles;
        this.num_tel = num_tel;
        this.adresse = adresse;
        this.status = status;
        this.matricule_fiscale = matricule_fiscale;
        this.photo_profil = photo_profil;
    }

    public utilisateur(int id, String email, String nom_user, String prenom, String roles, String num_tel, String adresse, String password, boolean status, String matricule_fiscale, String photo_profil) {
        this.id = id;
        this.email = email;
        this.nom_user = nom_user;
        this.prenom = prenom;
        this.roles = roles;
        this.num_tel = num_tel;
        this.adresse = adresse;
        this.password = password;
        this.status = status;
        this.matricule_fiscale = matricule_fiscale;
        this.photo_profil = photo_profil;
    }

    public utilisateur(String email, String nom_user, String prenom, String roles, String num_tel, String adresse, String password, boolean status, String matricule_fiscale, String photo_profil) {
        this.email = email;
        this.nom_user = nom_user;
        this.prenom = prenom;
        this.roles = roles;
        this.num_tel = num_tel;
        this.adresse = adresse;
        this.password = password;
        this.status = status;
        this.matricule_fiscale = matricule_fiscale;
        this.photo_profil = photo_profil;
    }

    public utilisateur() {
    }

    public utilisateur(String email, String nom_user, String prenom, String roles, String num_tel, String password, boolean status) {
        this.email = email;
        this.nom_user = nom_user;
        this.prenom = prenom;
        this.roles = roles;
        this.num_tel = num_tel;
        this.password = password;
        this.status = status;
    }

    public utilisateur(String email, String nom_user, String roles, String num_tel, String password, boolean status, String matricule_fiscale) {
        this.email = email;
        this.nom_user = nom_user;
        this.roles = roles;
        this.num_tel = num_tel;
        this.password = password;
        this.status = status;
        this.matricule_fiscale = matricule_fiscale;
    }

    // Standard getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNom_user() {
        return nom_user;
    }

    public void setNom_user(String nom_user) {
        this.nom_user = nom_user;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getRoles() {
        String Role = "[\"" + roles + "\"]";
        return Role;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getNum_tel() {
        return num_tel;
    }

    public void setNum_tel(String num_tel) {
        this.num_tel = num_tel;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMatricule_fiscale() {
        return matricule_fiscale;
    }

    public void setMatricule_fiscale(String matricule_fiscale) {
        this.matricule_fiscale = matricule_fiscale;
    }

    public String getPhoto_profil() {
        return photo_profil;
    }

    public void setPhoto_profil(String photo_profil) {
        this.photo_profil = photo_profil;
    }

    @Override
    public String toString() {
        return "utilisateur{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", nom_user='" + nom_user + '\'' +
                ", prenom='" + prenom + '\'' +
                ", roles='" + roles + '\'' +
                ", num_tel='" + num_tel + '\'' +
                ", adresse='" + adresse + '\'' +
                ", password='" + password + '\'' +
                ", status=" + status +
                ", matricule_fiscale='" + matricule_fiscale + '\'' +
                ", photo_profil='" + photo_profil + '\'' +
                '}';
    }
}