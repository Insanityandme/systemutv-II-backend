package se.myhappyplants.shared;

public class PlantDetails {
    private String scientificName;
    private String commonName;
    private String family;
    private String genus;
    private int light;
    private int waterFrequency;

    public PlantDetails(String genus, String scientificName, int light, int waterFrequency, String family) {
        this.genus = genus;
        this.scientificName = scientificName;
        this.light = light;
        this.waterFrequency = waterFrequency;
        this.family = family;
    }
    public String getGenus() {
        return genus;
    }

    public String getFamily() {
        return family;
    }

    public String getCommonName() {
        return commonName;
    }

    public String getScientificName() {
        return scientificName;
    }

    public int getLight() {
        return light;
    }

    public int getWaterFrequency() {
        return waterFrequency;
    }
}
