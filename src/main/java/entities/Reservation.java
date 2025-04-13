package entities;

public class Reservation {
    private int id;
    private int eventId;
    private String name;
    private String email;
    private String phone;
    private int places;
    private String status;

    public Reservation() {}

    public Reservation(int id, int eventId, String name, String email, String phone, int places, String status) {
        this.id = id;
        this.eventId = eventId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.places = places;
        this.status = status;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getPlaces() { return places; }
    public void setPlaces(int places) { this.places = places; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}