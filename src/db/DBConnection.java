package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // ── Change these to match your local MySQL setup ──────────────────────
    private static final String URL      = "jdbc:mysql://localhost:3306/student_db";
    private static final String USER     = "root";
    private static final String PASSWORD = "#tanyasql123#";   // ← change this
    // ─────────────────────────────────────────────────────────────────────

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found!", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}