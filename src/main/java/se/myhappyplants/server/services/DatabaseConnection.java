package se.myhappyplants.server.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private Connection conn;
    private String databaseName;

    public DatabaseConnection(String databaseName) {
        this.databaseName = databaseName;
    }

    public DatabaseConnection() {
        this.databaseName = "myhappyplants";
    }


    //Tar hand om kraven F.U.1
    private Connection connect() throws SQLException {
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

    public Connection getConnection() {
        try {
            return connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

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
