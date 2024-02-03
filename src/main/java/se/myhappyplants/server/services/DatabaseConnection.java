package se.myhappyplants.server.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Class for handling connection with a specific database
 * Created by: Frida Jacobsson 2021-05-21
 */
public class DatabaseConnection implements IDatabaseConnection {

    private Connection conn;
    private String databaseName;

    public DatabaseConnection(String databaseName) {
        this.databaseName = databaseName;
    }

    private Connection createConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                String dbURL = "jdbc:sqlite:./src/main/resources/" + databaseName + ".db";
                conn = DriverManager.getConnection(dbURL);
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC driver not found", e);
            }
        }

        return conn;
    }

    @Override
    public Connection getConnection() {
        try {
            return createConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                conn = null;
            }
        }
    }
}
