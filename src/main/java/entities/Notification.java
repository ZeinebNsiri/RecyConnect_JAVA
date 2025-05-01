package entities;
import java.time.LocalDateTime;

public class Notification {
    private int id;
    private String message;
    private boolean is_read;
    private LocalDateTime created_at;

    public Notification(int id, String message, boolean is_read, LocalDateTime created_at) {
        this.id = id;
        this.message = message;
        this.is_read = is_read;
        this.created_at = created_at;
    }

    public Notification(String message, boolean is_read, LocalDateTime created_at) {
        this.message = message;
        this.is_read = is_read;
        this.created_at = created_at;
    }

    public Notification() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIs_read() {
        return is_read;
    }

    public void setIs_read(boolean is_read) {
        this.is_read = is_read;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }
}
