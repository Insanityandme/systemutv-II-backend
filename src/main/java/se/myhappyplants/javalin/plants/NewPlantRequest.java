package se.myhappyplants.javalin.plants;

import se.myhappyplants.client.model.PictureRandomizer;

import java.sql.Date;
import java.time.LocalDate;

public class NewPlantRequest {
    public String id;
    public String commonName;

    public String scientificName;
    public String familyName;
    public String imageURL;
    public String nickname;
    public Date lastWatered;
    public long waterFrequency;

    public NewPlantRequest() {}

    public NewPlantRequest(String id, String commonName, String scientificName, String familyName, String imageURL) {
        this.id = id;
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.familyName = familyName;
        this.imageURL = imageURL;
    }
    public String toString() {
        String toString = String.format("Common name: %s \tFamily name: %s \tScientific name: %s ", commonName, familyName, scientificName);
        return toString;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getCommonName() {
        return commonName;
    }

    public String getScientificName() {
        return scientificName;
    }

    public String getId() {
        return id;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    /**
     * Image location for selected plant
     *
     * @return URL location of image
     */

    //Tar hand om krav F.SI.1
    public String getImageURL() {
        if (imageURL == null) {
            imageURL = PictureRandomizer.getRandomPictureURL();
        }
        String httpImageURL = imageURL;
        return httpImageURL;
    }

    public Date getLastWatered() {
        return lastWatered;
    }

    public void setLastWatered(LocalDate localDate) {
        Date date = java.sql.Date.valueOf(localDate);
        this.lastWatered = date;
    }

    /**
     * Compares the length of time since the plant was watered
     * with recommended frequency of watering. Returns a decimal value
     * that can be used in a progress bar or indicator
     *
     * @return Double between 0.02 (max time elapsed) and 1.0 (min time elapsed)
     */
    public double getProgress() {
        long difference = System.currentTimeMillis() - lastWatered.getTime();
        difference -= 43000000l;
        double progress = 1.0 - ((double) difference / (double) waterFrequency);
        if (progress <= 0.02) {
            progress = 0.02;
        } else if (progress >= 0.95) {
            progress = 1.0;
        }
        return progress;
    }

    /**
     * Converts time since last water from milliseconds
     * into days, then returns the value as
     * an explanation text
     *
     * @return Days since last water
     */
    public String getDaysUntilWater() {
        long millisSinceLastWatered = System.currentTimeMillis() - lastWatered.getTime();
        long millisUntilNextWatering = waterFrequency - millisSinceLastWatered;
        long millisInADay = 86400000;

        double daysExactlyUntilWatering = (double) millisUntilNextWatering / (double) millisInADay;

        int daysUntilWatering = (int) daysExactlyUntilWatering;
        double decimals = daysExactlyUntilWatering - (int) daysExactlyUntilWatering;

        if (decimals > 0.5) {
            daysUntilWatering = (int) daysExactlyUntilWatering + 1;
        }

        if (daysUntilWatering > 0) {
            return String.format("Needs water in %d days", daysUntilWatering);
        } else if (daysUntilWatering == 0) {
            return "You need to water this plant now!";
        } else {
            return "This plant doesn't need watering right now.";
        }
    }
}
