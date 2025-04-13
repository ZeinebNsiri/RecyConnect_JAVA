package entities;

public class Like {
    private int id;
    private Post postLike;
    private utilisateur userLike;

    public Like() {
    }

    public Like(int id, Post postLike, utilisateur userLike) {
        this.id = id;
        this.postLike = postLike;
        this.userLike = userLike;
    }

    public Like(Post postLike, utilisateur userLike) {
        this.postLike = postLike;
        this.userLike = userLike;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Post getPostLike() {
        return postLike;
    }

    public void setPostLike(Post postLike) {
        this.postLike = postLike;
    }

    public utilisateur getUserLike() {
        return userLike;
    }

    public void setUserLike(utilisateur userLike) {
        this.userLike = userLike;
    }

    @Override
    public String toString() {
        return "Like{" +
                "id=" + id +
                ", postLike=" + postLike +
                ", userLike=" + userLike +
                '}';
    }
}
