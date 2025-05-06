package entities;

import java.time.LocalDateTime;

public class Commentaire {
    private int id;
    private String contenuCom;
    private int userComId;
    private LocalDateTime dateCom;
    private int postComId;
    private Integer parentId; // Peut être null si c'est un commentaire principal

    public Commentaire() {}

    public Commentaire(int id, String contenuCom, int userComId, LocalDateTime dateCom, int postComId, Integer parentId) {
        this.id = id;
        this.contenuCom = contenuCom;
        this.userComId = userComId;
        this.dateCom = dateCom;
        this.postComId = postComId;
        this.parentId = parentId;
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

    public int getUserComId() {
        return userComId;
    }

    public void setUserComId(int userComId) {
        this.userComId = userComId;
    }

    public LocalDateTime getDateCom() {
        return dateCom;
    }

    public void setDateCom(LocalDateTime dateCom) {
        this.dateCom = dateCom;
    }

    public int getPostComId() {
        return postComId;
    }

    public void setPostComId(int postComId) {
        this.postComId = postComId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        return "Commentaire{" +
                "id=" + id +
                ", contenuCom='" + contenuCom + '\'' +
                ", userComId=" + userComId +
                ", dateCom=" + dateCom +
                ", postComId=" + postComId +
                ", parentId=" + (parentId != null ? parentId : "null") +
                '}';
    }
}
