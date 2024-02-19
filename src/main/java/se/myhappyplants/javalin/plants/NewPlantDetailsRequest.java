package se.myhappyplants.javalin.plants;

public class NewPlantDetailsRequest {
    public String genus;
    public String scientificName;
    public int light;
    public int waterFrequency;
    public String family;

    public NewPlantDetailsRequest() {}

    public NewPlantDetailsRequest(String genus, String scientificName, int light, int waterFrequency, String family) {
        this.scientificName = scientificName;
        this.genus = genus;
        this.light = light;
        this.waterFrequency = waterFrequency;
        this.family = family;
    }

    public String getScientificName() {
        return scientificName;
    }

    public String getGenus() {
        return genus;
    }

    public int getLight() {
        return light;
    }

    public int getWaterFrequency() {
        return waterFrequency;
    }

    public String getFamily() {
        return family;
    }
}
