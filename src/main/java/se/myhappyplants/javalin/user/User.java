package se.myhappyplants.javalin.user;

public class User {
    public int id;
    public String email;
    public String username;
    public String password;

    public User(int id, String email, String username, String password) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
    }
}
