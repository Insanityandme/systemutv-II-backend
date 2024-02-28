package se.myhappyplants.javalin.plant;


/**
 * Class defining a plant
 * Created by: Frida Jacobsson
 * Updated by: Linn Borgström, Eric Simonson, Susanne Vikström
 */
public class Plant {
   private int id;
   private String commonName;
   private String scientificName;
   private String familyName;
   private String imageURL;
   private String nickname;
   private String lastWatered;
   private long waterFrequency;
   private String genus;
   private int light;
   private String family;

    public Plant() {}
    /**
     * Creates a plant object from information
     * in the Species database
     *
     * @param id             Unique plant id in Species database
     * @param commonName     Common name
     * @param scientificName Scientific name
     * @param familyName     Family name
     * @param imageURL       Image location
     */
    public Plant(int id, String commonName, String scientificName, String familyName, String imageURL) {
        this.id = id;
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.familyName = familyName;
        this.imageURL = imageURL;
    }

    public Plant(String nickname, int id, String lastWatered, long waterFrequency) {
        this.nickname = nickname;
        this.id = id;
        this.lastWatered = lastWatered;
        this.waterFrequency = waterFrequency;
    }

    public Plant(String nickname, int id, String lastWatered) {
        this.nickname = nickname;
        this.id = id;
        this.lastWatered = lastWatered;
    }

    /**
     * Creates a plant object from a users library
     * in the MyHappyPlants database
     *
     * @param nickname
     * @param id             Unique plant id in Species database
     * @param lastWatered    String the plant was last watered
     * @param waterFrequency How often the plant needs water in milliseconds
     * @param imageURL       Image location
     */
    public Plant(String nickname, int id, String lastWatered, long waterFrequency, String imageURL) {
        this.nickname = nickname;
        this.id = id;
        this.lastWatered = lastWatered;
        this.waterFrequency = waterFrequency;
        this.imageURL = imageURL;
    }

    /**
     * Creates a plant object that can be used to update
     * a users library in the MyHappyPlants database
     *
     * @param nickname
     * @param id          Unique plant id in Species database
     * @param lastWatered String the plant was last watered
     * @param imageURL    Image location
     */
    public Plant(String nickname, int id, String lastWatered, String imageURL) {
        this.nickname = nickname;
        this.id = id;
        this.lastWatered = lastWatered;
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

    public int getId() {
        return id;
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

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
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
        String httpImageURL = imageURL;
        return httpImageURL;
    }

    public String getLastWatered() {
        return lastWatered;
    }

    public void setLastWatered(String lastWatered) {
        this.lastWatered = lastWatered;
    }

    /**
     * Converts time since last water from milliseconds
     * into days, then returns the value as
     * an explanation text
     *
     * @return Days since last water
     */
    public String getDaysUntilWater() {
        long millisSinceLastWatered = System.currentTimeMillis();
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