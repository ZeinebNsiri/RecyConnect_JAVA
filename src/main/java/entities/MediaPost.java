package entities;

public class MediaPost {

    private int id;
    private String chemin;
    private Post post;

    public MediaPost() {
    }

    public MediaPost(int id, String chemin, Post post) {
        this.id = id;
        this.chemin = chemin;
        this.post = post;
    }

    public MediaPost(String chemin, Post post) {
        this.chemin = chemin;
        this.post = post;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getChemin() {
        return chemin;
    }

    public void setChemin(String chemin) {
        this.chemin = chemin;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    @Override
    public String toString() {
        return "MediaPost{" +
                "id=" + id +
                ", chemin='" + chemin + '\'' +
                ", post=" + post +
                '}';
    }
}