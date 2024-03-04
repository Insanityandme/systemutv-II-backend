package se.myhappyplants.javalin.utils;

import java.util.Map;

public class ErrorResponse {
    public String title;
    public String type;
    public Map<String, String> details;

    public ErrorResponse() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}


