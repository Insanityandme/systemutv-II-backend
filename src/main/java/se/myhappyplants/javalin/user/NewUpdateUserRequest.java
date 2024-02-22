package se.myhappyplants.javalin.user;

public class NewUpdateUserRequest {
    public String email;
    public String username;
    public String password;
    public String avatarURL;
    public boolean isNotificationsActivated = true;
    public boolean funFactsActivated = true;
    public NewUpdateUserRequest() {}

    public NewUpdateUserRequest(String email, String username, String password, String avatarURL, boolean isNotificationsActivated, boolean funFactsActivated) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.avatarURL = avatarURL;
        this.isNotificationsActivated = isNotificationsActivated;
        this.funFactsActivated = funFactsActivated;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    public void setFunFactsActivated(boolean funFactsActivated) {
        this.funFactsActivated = funFactsActivated;
    }

    public void setNotificationsActivated(boolean notificationsActivated) {
        isNotificationsActivated = notificationsActivated;
    }


    public boolean isNotificationsActivated() {
        return isNotificationsActivated;
    }

    public boolean isFunFactsActivated() {
        return funFactsActivated;
    }


}


