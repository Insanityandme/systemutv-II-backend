package se.myhappyplants.javalin.user;

import java.io.File;

public class User {
    private int id;
    private final String email;
    private String username;
    private String password;
    private String avatarURL;
    private boolean isNotificationsActivated = true;
    private boolean funFactsActivated = true;

    /**
     * Constructor used when registering a new user account
     *
     * @param email
     * @param username
     * @param password
     * @param isNotificationsActivated
     */
    public User(String email, String username, String password, boolean isNotificationsActivated) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.isNotificationsActivated = isNotificationsActivated;
    }

    /**
     * Simple constructor for login requests
     *
     * @param email
     * @param password
     */
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(int uniqueID, String email, String username, boolean notificationsActivated) {
        this.id = uniqueID;
        this.email = email;
        this.username = username;
        this.isNotificationsActivated = notificationsActivated;
    }

    /**
     * Constructor used to return a users details from the database
     *
     * @param id                 A user's unique id in the database
     * @param email                    Email address
     * @param username                 Username
     * @param isNotificationsActivated True if notifications wanted
     * @param funFactsActivated        True if fun facts wanted
     */
    public User(int id, String email, String username, boolean isNotificationsActivated, boolean funFactsActivated) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.isNotificationsActivated = isNotificationsActivated;
        this.funFactsActivated = funFactsActivated;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean areNotificationsActivated() {
        return isNotificationsActivated;
    }

    public void setIsNotificationsActivated(boolean notificationsActivated) {
        this.isNotificationsActivated = notificationsActivated;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatar(String pathToImg) {
        this.avatarURL = new File(pathToImg).toURI().toString();
    }

    public boolean areFunFactsActivated() {
        return funFactsActivated;
    }

    public void setFunFactsActivated(boolean funFactsActivated) {
        this.funFactsActivated = funFactsActivated;
    }
}
