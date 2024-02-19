package se.myhappyplants.javalin.login;

public class NewLoginRequest {
    public String email;
    public String password;

    public NewLoginRequest() {
    }

    public NewLoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
