package se.myhappyplants.server.services;

import org.mindrot.jbcrypt.BCrypt;
import se.myhappyplants.shared.User;

import java.net.UnknownHostException;
import java.sql.*;

/**
 * Class responsible for calling the database about users.
 * Created by: Frida Jacobsson 2021-03-30
 * Updated by: Frida Jacobsson 2021-05-21
 */

//Tar hand om kraven F.U.1
public class UserRepository {

    private IQueryExecutor database;

    public UserRepository(IQueryExecutor database){
        this.database = database;
    }

    /**
     * Method to save a new user using BCrypt.
     *
     * @param user An instance of a newly created User that should be stored in the database.
     * @return A boolean value, true if the user was stored successfully
     */
    public boolean saveUser(User user) {
        boolean success = false;
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        String sqlSafeUsername = user.getUsername().replace("'", "''");
        String query = "INSERT INTO user (username, email, password, notification_activated, fun_facts_activated) VALUES (?, ?, ?, 1, 1);";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, sqlSafeUsername);
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, hashedPassword);

            preparedStatement.executeUpdate();
            success = true;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return success;
    }

    /**
     * Method to check if a user exists in database.
     * Purpose of method is to make it possible for user to log in
     *
     * @param email    typed email from client and the application
     * @param password typed password from client and the application
     * @return A boolean value, true if the user exist in database and the password is correct
     */
    public boolean checkLogin(String email, String password) {
        boolean isVerified = false;
        String query = "SELECT password FROM user WHERE email = ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, email);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String hashedPassword = resultSet.getString("password");
                isVerified = BCrypt.checkpw(password, hashedPassword);
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return isVerified;
    }

    /**
     * Method to get information (id, username and notification status) about a specific user
     *
     * @param email ??
     * @return a new instance of USer
     */
    public User getUserDetails(String email) {
        User user = null;
        int uniqueID = 0;
        String username = null;
        boolean notificationActivated = false;
        boolean funFactsActivated = false;
        String query = "SELECT id, username, notification_activated, fun_facts_activated FROM user WHERE email = ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, email);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                uniqueID = resultSet.getInt("id");
                username = resultSet.getString("username");
                notificationActivated = resultSet.getBoolean("notification_activated");
                funFactsActivated = resultSet.getBoolean("fun_facts_activated");
            }
            user = new User(uniqueID, email, username, notificationActivated, funFactsActivated);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return user;
    }

    /**
     * Method to delete a user and all plants in user library at once
     * author: Frida Jacobsson
     *
     * @param email
     * @param password
     * @return boolean value, false if transaction is rolled back
     * @throws SQLException
     */

    public boolean deleteAccount(String email, String password) {
        boolean accountDeleted = false;
        if (checkLogin(email, password)) {
            String querySelect = "SELECT id FROM user WHERE email = ?;";

            try (PreparedStatement preparedStatementSelect = database.prepareStatement(querySelect)) {
                preparedStatementSelect.setString(1, email);

                ResultSet resultSet = preparedStatementSelect.executeQuery();

                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String queryDeletePlants = "DELETE FROM Plant WHERE user_id = ?;";
                    String queryDeleteUser = "DELETE FROM User WHERE id = ?;";

                    try (PreparedStatement preparedStatementDeletePlants = database.prepareStatement(queryDeletePlants);
                         PreparedStatement preparedStatementDeleteUser = database.prepareStatement(queryDeleteUser)) {

                        preparedStatementDeletePlants.setInt(1, id);
                        preparedStatementDeleteUser.setInt(1, id);

                        database.beginTransaction();

                        preparedStatementDeletePlants.executeUpdate();
                        preparedStatementDeleteUser.executeUpdate();

                        database.endTransaction();
                        accountDeleted = true;
                    } catch (SQLException sqlException) {
                        try {
                            database.rollbackTransaction();
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    }
                }
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }

        return accountDeleted;
    }

    public boolean changeNotifications(User user, boolean notifications) {
        boolean notificationsChanged = false;
        int notificationsActivated = notifications ? 1 : 0;
        String query = "UPDATE user SET notification_activated = ? WHERE email = ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setInt(1, notificationsActivated);
            preparedStatement.setString(2, user.getEmail());

            preparedStatement.executeUpdate();
            notificationsChanged = true;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return notificationsChanged;
    }

    public boolean changeFunFacts(User user, Boolean funFactsActivated) {
        boolean funFactsChanged = false;
        int funFactsBitValue = funFactsActivated ? 1 : 0;
        String query = "UPDATE user SET fun_facts_activated = ? WHERE email = ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setInt(1, funFactsBitValue);
            preparedStatement.setString(2, user.getEmail());

            preparedStatement.executeUpdate();
            funFactsChanged = true;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return funFactsChanged;
    }
}

