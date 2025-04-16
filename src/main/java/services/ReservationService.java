package services;

import entities.Reservation;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationService implements IService<Reservation> {

    private final Connection cnx;

    public ReservationService() {
        this.cnx = MyDataBase.getInstance().getConx();
    }

    @Override
    public List<Reservation> displayList() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservation";
        Statement stmt = cnx.createStatement();
        ResultSet rs = stmt.executeQuery(query);

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
        return reservations;
    }

    @Override
    public void add(Reservation res) throws SQLException {
        String sql = "INSERT INTO reservation (event_id, nom, email, num_tel, nb_places, demandes_specailes, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = cnx.prepareStatement(sql);
        stmt.setInt(1, res.getEventId());
        stmt.setString(2, res.getNom());
        stmt.setString(3, res.getEmail());
        stmt.setString(4, res.getNumTel());
        stmt.setInt(5, res.getNbPlaces());
        stmt.setString(6, res.getdemandes_speciales());
        stmt.setString(7, res.getStatus());
        stmt.executeUpdate();
    }

    @Override
    public void delete(Reservation res) throws SQLException {
        String sql = "DELETE FROM reservation WHERE id=?";
        PreparedStatement stmt = cnx.prepareStatement(sql);
        stmt.setInt(1, res.getId());
        stmt.executeUpdate();
    }

    @Override
    public void update(Reservation res) throws SQLException {
        String sql = "UPDATE reservation SET event_id=?, nom=?, email=?, num_tel=?, nb_places=?, demandes_specailes=?, status=? WHERE id=?";
        PreparedStatement stmt = cnx.prepareStatement(sql);
        stmt.setInt(1, res.getEventId());
        stmt.setString(2, res.getNom());
        stmt.setString(3, res.getEmail());
        stmt.setString(4, res.getNumTel());
        stmt.setInt(5, res.getNbPlaces());
        stmt.setString(6, res.getdemandes_speciales());
        stmt.setString(7, res.getStatus());
        stmt.setInt(8, res.getId());
        stmt.executeUpdate();
    }
}
