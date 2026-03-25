package dao;

import model.User;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Users.
 * Implements full CRUD operations using JDBC.
 */
public class UserDAO {

    // ── CREATE ───────────────────────────────────────────────────────────────────

    /**
     * Registers a new library member.
     * @return the generated user_id, or -1 on failure.
     */
    public int addUser(User user) throws SQLException {
        String sql = "INSERT INTO Users (name, email, phone) VALUES (?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                user.setUserId(id);
                System.out.println("[UserDAO] User registered with ID: " + id);
                return id;
            }
        }
        return -1;
    }

    // ── READ ─────────────────────────────────────────────────────────────────────

    /** Retrieves all users. */
    public List<User> getAllUsers() throws SQLException {
        String sql = "SELECT * FROM Users ORDER BY name";
        List<User> users = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) users.add(mapRow(rs));
        }
        return users;
    }

    /** Finds a user by primary key. */
    public Optional<User> getUserById(int userId) throws SQLException {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    /** Finds a user by email address. */
    public Optional<User> getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM Users WHERE email = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────────

    /** Updates user profile information. */
    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE Users SET name=?, email=?, phone=?, is_active=? WHERE user_id=?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setBoolean(4, user.isActive());
            ps.setInt(5, user.getUserId());
            int rows = ps.executeUpdate();
            System.out.println("[UserDAO] Updated " + rows + " user record(s).");
            return rows > 0;
        }
    }

    /** Deactivates a user account (soft delete). */
    public boolean deactivateUser(int userId) throws SQLException {
        String sql = "UPDATE Users SET is_active = FALSE WHERE user_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────────

    /** Permanently removes a user from the database. */
    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM Users WHERE user_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            int rows = ps.executeUpdate();
            System.out.println("[UserDAO] Deleted " + rows + " user record(s).");
            return rows > 0;
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────────

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setActive(rs.getBoolean("is_active"));
        Timestamp ts = rs.getTimestamp("membership_date");
        if (ts != null) u.setMembershipDate(ts.toLocalDateTime());
        return u;
    }
}
