package se.myhappyplants.javalin.utils;

import io.javalin.http.InternalServerErrorResponse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
    private static DbConnection instance = null;
    public static String path = "myHappyPlantsDB.db";
    private final Connection conn;

    // Tar hand om kraven F.U.1
    private DbConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            final String DB_URL = "jdbc:sqlite:./src/main/resources/" + path;

            // NOTE: We should have user and password for our SQLite database
            // final String USER = "";
            // final String PASS = "";
            conn = DriverManager.getConnection(DB_URL);

            System.out.println("Connected to database");
        } catch (ClassNotFoundException | SQLException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
    }

    public static Connection getConnection() {
        Connection database;

        try {
            DbConnection instance = getInstance();
            database = instance.conn;
        } catch (SQLException e) {
            throw new InternalServerErrorResponse("Unable to establish connection to database");
        }
        return database;
    }

    private static DbConnection getInstance() throws SQLException {
        if (instance != null && !instance.conn.isClosed()) {
            instance.conn.setAutoCommit(true);
            return instance;
        }

        instance = new DbConnection();

        return instance;
    }
}
