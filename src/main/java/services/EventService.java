package services;

import entities.Event;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventService implements IService<Event> {
    private final Connection cnx;

    public EventService() {
        this.cnx = MyDataBase.getInstance().getConx();
    }

    @Override
        public void add(Event event) throws SQLException {
            String query = "INSERT INTO evenement (nom_event, description_event, lieu_event, date_event, " +
                    "heure_event, image_event, capacite, nb_restant, google_meet_link, " +
                    "map_coordinates, end_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, event.getName());
                stmt.setString(2, event.getDescription());
                stmt.setString(3, event.getLocation());
                stmt.setDate(4, Date.valueOf(event.getDate()));
                stmt.setTime(5, Time.valueOf(event.getTime()));
                stmt.setString(6, event.getImage());
                stmt.setInt(7, event.getCapacity());
                stmt.setInt(8, event.getRemaining());
                stmt.setString(9, event.getMeetingLink());
                stmt.setString(10, event.getCoordinates());
                stmt.setTime(11, Time.valueOf(event.getEndTime()));

                stmt.executeUpdate();
            }
        }

    public void decrementRemainingPlaces(int eventId, int nbPlacesReserved) throws SQLException {
        String sql = "UPDATE evenement SET nb_restant = nb_restant - ? WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, nbPlacesReserved);
            stmt.setInt(2, eventId);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Event> displayList() throws SQLException {
        List<Event> events = new ArrayList<>();
        String query = "SELECT * FROM evenement";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                events.add(new Event(
                        rs.getInt("id"),
                        rs.getString("nom_event"),
                        rs.getString("description_event"),
                        rs.getString("lieu_event"),
                        rs.getDate("date_event").toLocalDate(),
                        rs.getTime("heure_event").toLocalTime(),
                        rs.getString("image_event"),
                        rs.getInt("capacite"),
                        rs.getInt("nb_restant"),
                        rs.getString("google_meet_link"),
                        rs.getString("map_coordinates"),
                        rs.getTime("end_time").toLocalTime()
                ));
            }
        }
        return events;
    }

    @Override
    public void delete(Event event) throws SQLException {
        String query = "DELETE FROM evenement WHERE id=?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, event.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void update(Event event) throws SQLException {
        String query = "UPDATE evenement SET nom_event=?, description_event=?, lieu_event=?, " +
                "date_event=?, heure_event=?, image_event=?, capacite=?, nb_restant=?, " +
                "google_meet_link=?, map_coordinates=?, end_time=? WHERE id=?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, event.getName());
            stmt.setString(2, event.getDescription());
            stmt.setString(3, event.getLocation());
            stmt.setDate(4, Date.valueOf(event.getDate()));
            stmt.setTime(5, Time.valueOf(event.getTime()));
            stmt.setString(6, event.getImage());
            stmt.setInt(7, event.getCapacity());
            stmt.setInt(8, event.getRemaining());
            stmt.setString(9, event.getMeetingLink());
            stmt.setString(10, event.getCoordinates());
            stmt.setTime(11, Time.valueOf(event.getEndTime()));
            stmt.setInt(12, event.getId());

            stmt.executeUpdate();
        }
    }

    public List<Event> getAllEvents() throws SQLException {
        return displayList();
    }

    public boolean deleteEvent(int id) throws SQLException {
        Event e = new Event();
        e.setId(id);
        delete(e);
        return true;
    }

    public Event getEventById(int id) throws SQLException {
        String query = "SELECT * FROM evenement WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Event(
                        rs.getInt("id"),
                        rs.getString("nom_event"),
                        rs.getString("description_event"),
                        rs.getString("lieu_event"),
                        rs.getDate("date_event").toLocalDate(),
                        rs.getTime("heure_event").toLocalTime(),
                        rs.getString("image_event"),
                        rs.getInt("capacite"),
                        rs.getInt("nb_restant"),
                        rs.getString("google_meet_link"),
                        rs.getString("map_coordinates"),
                        rs.getTime("end_time").toLocalTime()
                );
            }
        }
        return null;
    }
}
