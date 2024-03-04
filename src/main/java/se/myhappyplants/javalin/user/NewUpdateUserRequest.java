package se.myhappyplants.javalin.user;

public class NewUpdateUserRequest {
    public String email;
    public String username;
    public String oldPassword;
    public String newPassword;
    public String avatarURL;
    public boolean notificationsActivated = true;
    public boolean funFactsActivated = true;
    public NewUpdateUserRequest() {}

    public NewUpdateUserRequest(String email, String username, String oldPassword, String newPassword, String avatarURL, boolean notificationsActivated, boolean funFactsActivated) {
        this.email = email;
        this.username = username;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.avatarURL = avatarURL;
        this.notificationsActivated = notificationsActivated;
        this.funFactsActivated = funFactsActivated;
    }
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword() {
        return newPassword;
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
        this.notificationsActivated = notificationsActivated;
    }


    public boolean isNotificationsActivated() {
        return notificationsActivated;
    }

    public boolean isFunFactsActivated() {
        return funFactsActivated;
    }


}


