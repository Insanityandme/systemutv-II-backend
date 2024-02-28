package se.myhappyplants.javalin.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
    private static DbConnection instance = null;
    private Connection conn = null;
    private String path = "myHappyPlantsDB.db";

    // Tar hand om kraven F.U.1
    private DbConnection() {
    }


    private void init() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            final String DB_URL = "jdbc:sqlite:./src/main/resources/" + path;
            final String USER = "";
            final String PASS = "";
            conn = DriverManager.getConnection(DB_URL);

            System.out.println("Connected to database");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
    }

    public Connection getConnection() {
        return conn;
    }

    public static DbConnection getInstance() throws SQLException {
        if (instance != null && !instance.getConnection().isClosed()) {
            instance.getConnection().setAutoCommit(true);
            return instance;
        }

        instance = new DbConnection();
        instance.init();

        return instance;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
