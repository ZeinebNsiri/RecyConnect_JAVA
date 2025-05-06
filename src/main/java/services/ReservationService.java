    package services;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.SQLException;
    import entities.Reservation;
    import utils.MyDataBase;
    import java.sql.*;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.regex.Pattern;

    public class ReservationService implements IService<Reservation> {
        private final Connection cnx;

        // Validation patterns
        private static final Pattern EMAIL_PATTERN =
                Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
        private static final Pattern PHONE_PATTERN =
                Pattern.compile("^(\\+\\d{1,3}[- ]?)?\\d{8,15}$");
        private static final Pattern NAME_PATTERN =
                Pattern.compile("^[\\p{L} .'-]+$");

        public ReservationService() {
            this.cnx = MyDataBase.getInstance().getConx();
        }

        @Override
        public void add(Reservation res) throws SQLException, IllegalArgumentException {
            validateReservation(res);

            String sql = "INSERT INTO reservation (event_id, nom, email, num_tel, nb_places, demandes_speciales, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, res.getEventId());
                stmt.setString(2, res.getNom());
                stmt.setString(3, res.getEmail());
                stmt.setString(4, res.getNumTel());
                stmt.setInt(5, res.getNbPlaces());
                stmt.setString(6, res.getdemandes_speciales());
                stmt.setString(7, "active");

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Creating reservation failed, no rows affected.");
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        res.setId(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Creating reservation failed, no ID obtained.");
                    }
                }
            }
        }

        private void validateReservation(Reservation res) throws IllegalArgumentException {
            if (res == null) {
                throw new IllegalArgumentException("Reservation cannot be null");
            }

            // Name validation
            if (res.getNom() == null || res.getNom().trim().isEmpty()) {
                throw new IllegalArgumentException("Name is required");
            }
            if (!NAME_PATTERN.matcher(res.getNom()).matches()) {
                throw new IllegalArgumentException("Invalid name format (only letters and basic punctuation allowed)");
            }

            // Email validation
            if (res.getEmail() == null || res.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Email is required");
            }
            if (!EMAIL_PATTERN.matcher(res.getEmail()).matches()) {
                throw new IllegalArgumentException("Invalid email format (example@domain.com)");
            }

            // Phone validation
            if (res.getNumTel() == null || res.getNumTel().trim().isEmpty()) {
                throw new IllegalArgumentException("Phone number is required");
            }
            if (!PHONE_PATTERN.matcher(res.getNumTel()).matches()) {
                throw new IllegalArgumentException("Invalid phone format (8-15 digits)");
            }

            // Places validation
            if (res.getNbPlaces() <= 0) {
                throw new IllegalArgumentException("Number of places must be positive");
            }

            // Event ID validation
            if (res.getEventId() <= 0) {
                throw new IllegalArgumentException("Invalid event ID");
            }
        }

        @Override
        public void update(Reservation res) throws SQLException, IllegalArgumentException {
            validateReservation(res);

            String sql = "UPDATE reservation SET event_id=?, nom=?, email=?, num_tel=?, nb_places=?, " +
                    "demandes_speciales=?, status=? WHERE id=?";

            try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
                stmt.setInt(1, res.getEventId());
                stmt.setString(2, res.getNom());
                stmt.setString(3, res.getEmail());
                stmt.setString(4, res.getNumTel());
                stmt.setInt(5, res.getNbPlaces());
                stmt.setString(6, res.getdemandes_speciales());
                stmt.setString(7, res.getStatus());
                stmt.setInt(8, res.getId());

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("No reservation found with ID: " + res.getId());
                }
            }
        }

        @Override
        public void delete(Reservation res) throws SQLException {
            String sql = "DELETE FROM reservation WHERE id=?";

            try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
                stmt.setInt(1, res.getId());

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("No reservation found with ID: " + res.getId());
                }
            }
        }

        @Override
        public List<Reservation> displayList() throws SQLException {
            return getAllReservations();
        }

        public List<Reservation> getAllReservations() throws SQLException {
            List<Reservation> reservations = new ArrayList<>();
            String query = "SELECT * FROM reservation ORDER BY id DESC";

            try (Statement stmt = cnx.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    reservations.add(new Reservation(
                            rs.getInt("id"),
                            rs.getInt("event_id"),
                            rs.getString("nom"),
                            rs.getString("email"),
                            rs.getString("num_tel"),
                            rs.getInt("nb_places"),
                            rs.getString("demandes_speciales"),
                            rs.getString("status")
                    ));
                }
            }
            return reservations;
        }

        public void cancelReservation(int reservationId) throws SQLException {
            String query = "UPDATE reservation SET status = 'cancelled' WHERE id = ?";

            try (PreparedStatement stmt = cnx.prepareStatement(query)) {
                stmt.setInt(1, reservationId);

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("No reservation found with ID: " + reservationId);
                }
            }
        }

        public List<Reservation> getReservationsByUser(String email) throws SQLException {
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email cannot be empty");
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new IllegalArgumentException("Invalid email format");
            }

            List<Reservation> reservations = new ArrayList<>();
            String query = "SELECT * FROM reservation WHERE email = ? ORDER BY id DESC";

            try (PreparedStatement stmt = cnx.prepareStatement(query)) {
                stmt.setString(1, email);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        reservations.add(new Reservation(
                                rs.getInt("id"),
                                rs.getInt("event_id"),
                                rs.getString("nom"),
                                rs.getString("email"),
                                rs.getString("num_tel"),
                                rs.getInt("nb_places"),
                                rs.getString("demandes_speciales"),
                                rs.getString("status")
                        ));
                    }
                }
            }
            return reservations;
        }

        public Reservation getReservationById(int id) throws SQLException {
            String query = "SELECT * FROM reservation WHERE id = ?";

            try (PreparedStatement stmt = cnx.prepareStatement(query)) {
                stmt.setInt(1, id);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new Reservation(
                                rs.getInt("id"),
                                rs.getInt("event_id"),
                                rs.getString("nom"),
                                rs.getString("email"),
                                rs.getString("num_tel"),
                                rs.getInt("nb_places"),
                                rs.getString("demandes_speciales"),
                                rs.getString("status")
                        );
                    } else {
                        throw new SQLException("No reservation found with ID: " + id);
                    }
                }
            }
        }
        public boolean isUserRegisteredForEvent(int eventId, String nom) throws SQLException {
            String sql = "SELECT COUNT(*) FROM reservation WHERE event_id = ? AND nom = ? AND status = 'Active'";
            try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
                stmt.setInt(1, eventId);
                stmt.setString(2, nom);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        }


    }