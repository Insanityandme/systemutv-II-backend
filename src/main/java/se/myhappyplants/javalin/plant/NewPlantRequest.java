package se.myhappyplants.javalin.plant;

import se.myhappyplants.client.model.PictureRandomizer;

import java.time.LocalDate;

public class NewPlantRequest {
    public String id;
    public String commonName;

    public String scientificName;
    public String familyName;
    public String imageURL;
    public String nickname;
    public LocalDate lastWatered;
    public long waterFrequency;
    public String genus;
    public int light;
    public String family;

    public NewPlantRequest() {
    }

    public NewPlantRequest(String id, String commonName, String scientificName, String familyName, String imageURL,
                           String nickname, LocalDate lastWatered, long waterFrequency, int light, String genus, String family) {
        this.id = id;
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.familyName = familyName;
        this.imageURL = imageURL;
        this.nickname = nickname;
        this.lastWatered = lastWatered;
        this.waterFrequency = waterFrequency;
        this.light = light;
        this.genus = genus;
        this.family = family;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public long getWaterFrequency() {
        return waterFrequency;
    }

    public void setWaterFrequency(long waterFrequency) {
        this.waterFrequency = waterFrequency;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGenus() {
        return genus;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public void getCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getCommonName() {
        return commonName;
    }

    public int getLight() {
        return light;
    }

    public void setLight(int light) {
        this.light = light;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public LocalDate getLastWatered() {
        return lastWatered;
    }

    public void setLastWatered(LocalDate date) {
        this.lastWatered = date;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @Override
    public String toString() {
        return String.format("Plant: %s, %s, %s, %s, %s, %s, %s, %s, %s, %s",
                id, commonName, scientificName,
                familyName, imageURL, nickname,
                lastWatered, waterFrequency, light, genus, family);
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

        return imageURL;
    }

    /**
     * Compares the length of time since the plant was watered
     * with recommended frequency of watering. Returns a decimal value
     * that can be used in a progress bar or indicator
     *
     * @return Double between 0.02 (max time elapsed) and 1.0 (min time elapsed)
     */
    // public double getProgress() {
        // long difference = System.currentTimeMillis() - lastWatered.getTime();
       //  difference -= 43000000l;
       //  double progress = 1.0 - ((double) difference / (double) waterFrequency);
       //  if (progress <= 0.02) {
       //      progress = 0.02;
       //  } else if (progress >= 0.95) {
       //      progress = 1.0;
       //  }
       //  return progress;
    // }

    /**
     * Converts time since last water from milliseconds
     * into days, then returns the value as
     * an explanation text
     *
     * @return Days since last water
     */
    // public String getDaysUntilWater() {
    //     long millisSinceLastWatered = System.currentTimeMillis() - lastWatered.getTime();
    //     long millisUntilNextWatering = waterFrequency - millisSinceLastWatered;
    //     long millisInADay = 86400000;

    //     double daysExactlyUntilWatering = (double) millisUntilNextWatering / (double) millisInADay;

    //     int daysUntilWatering = (int) daysExactlyUntilWatering;
    //     double decimals = daysExactlyUntilWatering - (int) daysExactlyUntilWatering;

    //     if (decimals > 0.5) {
    //         daysUntilWatering = (int) daysExactlyUntilWatering + 1;
    //     }

    //     if (daysUntilWatering > 0) {
    //         return String.format("Needs water in %d days", daysUntilWatering);
    //     } else if (daysUntilWatering == 0) {
    //         return "You need to water this plant now!";
    //     } else {
    //         return "This plant doesn't need watering right now.";
    //     }
    // }
}
