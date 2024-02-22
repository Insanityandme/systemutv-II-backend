package se.myhappyplants.javalin.user;

public class NewDeleteRequest {
    public String password;

    public NewDeleteRequest() {}
    public NewDeleteRequest(String password) {
        this.password = password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
