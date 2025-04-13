package entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Commentaire {
    private int id;
    private String contenuCom;
    private utilisateur userCom;
    private LocalDateTime dateCom;
    private Post postCom;
    private Commentaire parent;
    private List<Commentaire> replies;

    public Commentaire() {
        this.replies = new ArrayList<>();
    }

    public Commentaire(int id, String contenuCom, utilisateur userCom, LocalDateTime dateCom, Post postCom, Commentaire parent) {
        this.id = id;
        this.contenuCom = contenuCom;
        this.userCom = userCom;
        this.dateCom = dateCom;
        this.postCom = postCom;
        this.parent = parent;
        this.replies = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContenuCom() {
        return contenuCom;
    }

    public void setContenuCom(String contenuCom) {
        this.contenuCom = contenuCom;
    }

    public utilisateur getUserCom() {
        return userCom;
    }

    public void setUserCom(utilisateur userCom) {
        this.userCom = userCom;
    }

    public LocalDateTime getDateCom() {
        return dateCom;
    }

    public void setDateCom(LocalDateTime dateCom) {
        this.dateCom = dateCom;
    }

    public Post getPostCom() {
        return postCom;
    }

    public void setPostCom(Post postCom) {
        this.postCom = postCom;
    }

    public Commentaire getParent() {
        return parent;
    }

    public void setParent(Commentaire parent) {
        this.parent = parent;
    }

    public List<Commentaire> getReplies() {
        return replies;
    }

    public void setReplies(List<Commentaire> replies) {
        this.replies = replies;
    }

    public void addReply(Commentaire reply) {
        this.replies.add(reply);
        reply.setParent(this);
    }

    public void removeReply(Commentaire reply) {
        this.replies.remove(reply);
        if (reply.getParent() == this) {
            reply.setParent(null);
        }
    }

    @Override
    public String toString() {
        return "Commentaire{" +
                "id=" + id +
                ", contenuCom='" + contenuCom + '\'' +
                ", userCom=" + (userCom != null ? userCom.getPrenom() : "null") +
                ", dateCom=" + dateCom +
                ", postCom=" + (postCom != null ? postCom.getId() : "null") +
                ", parentId=" + (parent != null ? parent.getId() : "null") +
                ", repliesCount=" + replies.size() +
                '}';
    }
}
