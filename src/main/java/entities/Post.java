package entities;

import java.time.LocalDateTime;
import java.util.List;

public class Post {

    private int id ;
    private int user_p;
    private String contenu;
    private LocalDateTime date_publication;
    private int nbr_jaime;
    private boolean status_post;


    public Post() {}

    public Post(int id, int user_p, String contenu, LocalDateTime date_publication, int nbr_jaime, boolean status_post) {
        this.id = id;
        this.user_p = user_p;
        this.contenu = contenu;
        this.date_publication = date_publication;
        this.nbr_jaime = nbr_jaime;
        this.status_post = status_post;

    }


    // Getters et Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUser_p_id() { return user_p; }
    public void setUser_p_id(int user_p_id) { this.user_p = user_p_id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public LocalDateTime getDate_publication() { return date_publication; }
    public void setDate_publication(LocalDateTime date_publication) { this.date_publication = date_publication; }

    public int getNbr_jaime() { return nbr_jaime; }
    public void setNbr_jaime(int nbr_jaime) { this.nbr_jaime = nbr_jaime; }

    public boolean isStatus_post() { return status_post; }
    public void setStatus_post(boolean status_post) { this.status_post = status_post; }


    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", user_p=" + user_p +
                ", contenu='" + contenu + '\'' +
                ", date_publication=" + date_publication +
                ", nbr_jaime=" + nbr_jaime +
                ", status_post=" + status_post +
                '}';
    }


}