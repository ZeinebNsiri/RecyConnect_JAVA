package entities;

import java.time.LocalDate;

public class Event {
    private int id;
    private String name;
    private String location;
    private LocalDate date;
    private String type;
    private String description;

    public Event() {}

    public Event(int id, String name, String location, LocalDate date, String type, String description) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.date = date;
        this.type = type;
        this.description = description;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}