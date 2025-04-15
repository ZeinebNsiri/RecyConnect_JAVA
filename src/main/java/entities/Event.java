package entities;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalTime;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.InputStream;


public class Event {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty location = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalTime> time = new SimpleObjectProperty<>();
    private final StringProperty image = new SimpleStringProperty();
    private final IntegerProperty capacity = new SimpleIntegerProperty();
    private final IntegerProperty remaining = new SimpleIntegerProperty();
    private final StringProperty meetingLink = new SimpleStringProperty();
    private final StringProperty coordinates = new SimpleStringProperty();
    private final ObjectProperty<LocalTime> endTime = new SimpleObjectProperty<>();

    // Constructors
    public Event() {}

    public Event(int id, String name, String description, String location,
                 LocalDate date, LocalTime time, String image, int capacity,
                 int remaining, String meetingLink, String coordinates, LocalTime endTime) {
        setId(id);
        setName(name);
        setDescription(description);
        setLocation(location);
        setDate(date);
        setTime(time);
        setImage(image);
        setCapacity(capacity);
        setRemaining(remaining);
        setMeetingLink(meetingLink);
        setCoordinates(coordinates);
        setEndTime(endTime);
    }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty locationProperty() { return location; }
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public ObjectProperty<LocalTime> timeProperty() { return time; }
    public StringProperty imageProperty() { return image; }
    public IntegerProperty capacityProperty() { return capacity; }
    public IntegerProperty remainingProperty() { return remaining; }
    public StringProperty meetingLinkProperty() { return meetingLink; }
    public StringProperty coordinatesProperty() { return coordinates; }
    public ObjectProperty<LocalTime> endTimeProperty() { return endTime; }

    // Standard getters
    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getDescription() { return description.get(); }
    public String getLocation() { return location.get(); }
    public LocalDate getDate() { return date.get(); }
    public LocalTime getTime() { return time.get(); }
    public String getImage() { return image.get(); }
    public int getCapacity() { return capacity.get(); }
    public int getRemaining() { return remaining.get(); }
    public String getMeetingLink() { return meetingLink.get(); }
    public String getCoordinates() { return coordinates.get(); }
    public ImageView getImageView() {
        String filename = getImage(); // like "67c038565a819.png"
        if (filename == null || filename.isEmpty()) {
            return new ImageView(); // or optionally show nothing
        }

        // Full path to /uploads/ directory (you can adjust this if needed)
        String path = "file:uploads/" + filename;

        Image img = new Image(path, 50, 50, true, true);
        ImageView view = new ImageView(img);
        view.setFitHeight(50);
        view.setFitWidth(50);
        return view;
    }



    public LocalTime getEndTime() { return endTime.get(); }

    // Standard setters
    public void setId(int id) { this.id.set(id); }
    public void setName(String name) { this.name.set(name); }
    public void setDescription(String description) { this.description.set(description); }
    public void setLocation(String location) { this.location.set(location); }
    public void setDate(LocalDate date) { this.date.set(date); }
    public void setTime(LocalTime time) { this.time.set(time); }
    public void setImage(String image) { this.image.set(image); }
    public void setCapacity(int capacity) { this.capacity.set(capacity); }
    public void setRemaining(int remaining) { this.remaining.set(remaining); }
    public void setMeetingLink(String meetingLink) { this.meetingLink.set(meetingLink); }
    public void setCoordinates(String coordinates) { this.coordinates.set(coordinates); }
    public void setEndTime(LocalTime endTime) { this.endTime.set(endTime); }
}