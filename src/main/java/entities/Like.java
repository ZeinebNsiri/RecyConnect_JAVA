package entities;

public class Like {
    private int id;
    private int post_like_id;
    private int user_like_id;

    public Like() {
    }

    public Like(int id, int post_like_id, int user_like_id) {
        this.id = id;
        this.post_like_id = post_like_id;
        this.user_like_id = user_like_id;
    }

    public Like(int post_like_id, int user_like_id) {
        this.post_like_id = post_like_id;
        this.user_like_id = user_like_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPostLike() {
        return post_like_id;
    }

    public void setPostLike(int post_like_id) {
        this.post_like_id = post_like_id;
    }

    public int getUserLike() {
        return user_like_id;
    }

    public void setUserLike(int user_like_id) {
        this.user_like_id = user_like_id;
    }

    @Override
    public String toString() {
        return "Like{" +
                "id=" + id +
                ", postLike=" + post_like_id +
                ", userLike=" + user_like_id +
                '}';
    }
}
