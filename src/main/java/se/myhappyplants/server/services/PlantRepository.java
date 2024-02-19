package se.myhappyplants.server.services;

import se.myhappyplants.shared.WaterCalculator;
import se.myhappyplants.javalin.plants.Plant;
import se.myhappyplants.shared.PlantDetails;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Class responsible for calling the database about plants.
 * Created by: Frida Jacobsson 2021-03-30
 * Updated by: Christopher O'Driscoll
 */

//Tar hand om kraven F.U.1
public class PlantRepository {

    private IQueryExecutor database;

    public PlantRepository(IQueryExecutor database) {
        this.database = database;
    }

    public ArrayList<Plant> getResult(String plantSearch) {
        ArrayList<Plant> plantList = new ArrayList<>();
        String query = "SELECT id, common_name, scientific_name, family, image_url FROM plantdetails WHERE scientific_name LIKE ? OR common_name LIKE ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, "%" + plantSearch + "%");
            preparedStatement.setString(2, "%" + plantSearch + "%");

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String plantId = resultSet.getString("id");
                String commonName = resultSet.getString("common_name");
                String scientificName = resultSet.getString("scientific_name");
                String familyName = resultSet.getString("family");
                String imageURL = resultSet.getString("image_url");
                plantList.add(new Plant(plantId, commonName, scientificName, familyName, imageURL));
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            plantList = null;
        }

        return plantList;
    }

    public PlantDetails getPlantDetails(Plant plant) {
        PlantDetails details = null;
        String query = "SELECT * FROM plantdetails WHERE plant_id = ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, plant.getPlantId());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String scientificName = resultSet.getString("scientific_name");
                String genus = resultSet.getString("genus");
                String family = resultSet.getString("family");
                String commonName = resultSet.getString("common_name");
                String imageURL = resultSet.getString("image_url");
                int light = resultSet.getInt("light");
                String wikipediaUrl = resultSet.getString("url_wikipedia_en");
                int waterFrequency = resultSet.getInt("water_frequency");

                details = new PlantDetails(genus, scientificName, light, waterFrequency, family);
            } else {
                System.out.println("No results found for plant_id: " + plant.getPlantId());
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return details;
    }



    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    public long getWaterFrequency(String plantId) throws IOException, InterruptedException {
        long waterFrequency = -1;
        String query = "SELECT water_frequency FROM plantdetails WHERE id = ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, plantId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String waterText = resultSet.getString("water_frequency");
                int water = (isNumeric(waterText)) ? Integer.parseInt(waterText) : -1;
                waterFrequency = WaterCalculator.calculateWaterFrequencyForWatering(water);
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return waterFrequency;
    }

}
