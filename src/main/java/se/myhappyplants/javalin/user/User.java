package se.myhappyplants.javalin.user;

public class User {
    public int id;
    public final String email;
    public String username;
    public String password;
    public boolean isNotificationsActivated;
    public boolean funFactsActivated;

    public User(int id, String email, String username, boolean isNotificationsActivated, boolean funFactsActivated) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.isNotificationsActivated = isNotificationsActivated;
        this.funFactsActivated = funFactsActivated;
    }

    public User(String email, String username, boolean isNotificationsActivated, boolean funFactsActivated) {
        this.email = email;
        this.username = username;
        this.isNotificationsActivated = isNotificationsActivated;
        this.funFactsActivated = funFactsActivated;
    }

    public int getId() {
        return id;
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
}
