package services;

import entities.Notification;
import org.mindrot.jbcrypt.BCrypt;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationService implements IService<Notification> {
    Connection conx;
    Statement stm;
    public NotificationService() {
        this.conx = MyDataBase.getInstance().getConx();
    }
    @Override
    public List<Notification> displayList() throws SQLException {
        List<Notification> notifs = new ArrayList<>();
        String sql = "SELECT * FROM notification ORDER BY created_at DESC";
        PreparedStatement ps = conx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Notification n = new Notification();
            n.setId(rs.getInt("id"));
            n.setMessage(rs.getString("message"));
            n.setIs_read(rs.getBoolean("is_read"));
            n.setCreated_at(rs.getTimestamp("created_at").toLocalDateTime());
            notifs.add(n);
        }
        return notifs;
    }
    public List<Notification> getUnread() throws SQLException {
        List<Notification> notifs = new ArrayList<>();
        String sql = "SELECT * FROM notification WHERE is_read = false ORDER BY created_at DESC";
        PreparedStatement ps = conx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Notification n = new Notification();
            n.setId(rs.getInt("id"));
            n.setMessage(rs.getString("message"));
            n.setIs_read(rs.getBoolean("is_read"));
            n.setCreated_at(rs.getTimestamp("created_at").toLocalDateTime());
            notifs.add(n);
        }
        return notifs;
    }
    public int countUnread() throws SQLException {
        String sql = "SELECT COUNT(*) FROM notification WHERE is_read = false";
        PreparedStatement ps = conx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
        return 0;
    }
    public void markAllAsRead() throws SQLException {
        String sql = "UPDATE notification SET is_read = true WHERE is_read = false";
        PreparedStatement ps = conx.prepareStatement(sql);
        ps.executeUpdate();
    }

    @Override
    public void add(Notification notification) throws SQLException {
        String query = "INSERT INTO `notification`(`message`, `is_read`, `created_at`) VALUES (?,?,?)";
        PreparedStatement ps = conx.prepareStatement(query);
        Timestamp timestamp = Timestamp.valueOf(notification.getCreated_at());

        ps.setString(1, notification.getMessage());
        ps.setBoolean(2, notification.isIs_read());
        ps.setTimestamp(3, timestamp);

        ps.executeUpdate();
    }

    @Override
    public void delete(Notification notification) throws SQLException {

    }

    @Override
    public void update(Notification notification) throws SQLException {

    }
}
