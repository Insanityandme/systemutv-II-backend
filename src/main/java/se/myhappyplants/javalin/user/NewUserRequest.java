package se.myhappyplants.javalin.user;

public class NewUserRequest {
    public String email;
    public String username;
    public String password;

    public NewUserRequest() {
    }

    public NewUserRequest(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }
}
