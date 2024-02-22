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

    public User(String email, String username, String password, String avatarURL, boolean isNotificationsActivated, boolean funFactsActivated) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.avatarURL = avatarURL;
        this.isNotificationsActivated = isNotificationsActivated;
        this.funFactsActivated = funFactsActivated;
    }

    public User(int id, String email, String username, boolean isNotificationsActivated, boolean funFactsActivated) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.isNotificationsActivated = isNotificationsActivated;
        this.funFactsActivated = funFactsActivated;
    }

    public User(String email, String username) {
        this.email = email;
        this.username = username;
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
