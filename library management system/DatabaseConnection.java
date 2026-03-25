package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton utility class for managing JDBC database connections.
 * Uses a single shared connection for the application lifecycle.
 */
public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC";
    private static final String USER     = "root";       // Change to your MySQL username
    private static final String PASSWORD = "password";   // Change to your MySQL password

    private static Connection connection = null;

    // Private constructor – prevent instantiation
    private DatabaseConnection() {}

    /**
     * Returns the singleton Connection. Creates it on first call.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[DB] Connected to library_db successfully.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found. Add mysql-connector-java to classpath.", e);
            }
        }
        return connection;
    }

    /**
     * Closes the shared connection (call on app shutdown).
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DB] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DB] Error closing connection: " + e.getMessage());
            }
        }
    }
}
