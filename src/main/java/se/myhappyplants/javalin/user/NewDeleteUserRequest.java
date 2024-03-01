package se.myhappyplants.javalin.user;

public class NewDeleteUserRequest {
    public String password;

    public NewDeleteUserRequest() {}
    public NewDeleteUserRequest(String password) {
        this.password = password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
