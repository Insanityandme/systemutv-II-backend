package se.myhappyplants.server.services;

import se.myhappyplants.shared.Plant;
import se.myhappyplants.shared.PlantDetails;
import se.myhappyplants.shared.User;

import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.*;
import java.time.LocalDate;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Class responsible for calling the database about a users library.
 * Created by: Linn Borgström
 * Updated by: Frida Jacobsson 2021-05-21
 */

//Tar hand om kraven F.U.1
public class UserPlantRepository {

    private PlantRepository plantRepository;
    private IQueryExecutor database;

    /**
     * Constructor that creates a connection to the database.
     *
     * @throws SQLException
     * @throws UnknownHostException
     */
    public UserPlantRepository(PlantRepository plantRepository, IQueryExecutor database) throws UnknownHostException, SQLException {
        this.plantRepository = plantRepository;
        this.database = database;

    }

    /**
     * Method to save a new plant in database
     * Author: Frida Jacobsson
     * Updated Frida Jacobsson 2021-04-29
     *
     * @param plant an instance of a newly created plant by user
     * @return a boolean value, true if the plant was stored successfully
     */

    public boolean savePlant(User user, Plant plant, PlantDetails details) {
        boolean success = false;
        String sqlSafeNickname = plant.getNickname().replace("'", "''");

        String plantQuery = "INSERT INTO plant (user_id, nickname, plant_id, last_watered, image_url) VALUES (?, ?, ?, ?, ?);";
        String detailsQuery = "INSERT INTO plantdetails (scientific_name, genus, family, common_name, image_url, light, url_wikipedia_en, water_frequency, plant_id)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (PreparedStatement plantStatement = database.prepareStatement(plantQuery);
             PreparedStatement detailsStatement = database.prepareStatement(detailsQuery)) {

            plantStatement.setInt(1, user.getUniqueId());
            plantStatement.setString(2, sqlSafeNickname);
            plantStatement.setString(3, plant.getPlantId());
            System.out.print(plant.getPlantId());
            plantStatement.setDate(4, plant.getLastWatered());
            plantStatement.setString(5, plant.getImageURL());
            plantStatement.executeUpdate();

            detailsStatement.setString(1, details.getScientificName());
            detailsStatement.setString(2, details.getGenus());
            detailsStatement.setString(3, details.getFamily());
            detailsStatement.setString(4, plant.getCommonName());
            detailsStatement.setString(5, plant.getImageURL());
            detailsStatement.setString(6, "0");
            detailsStatement.setString(7, "0");
            detailsStatement.setString(8, "0");
            detailsStatement.setString(9, plant.getPlantId());
            detailsStatement.executeUpdate();

            success = true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return success;
    }



    /**
     * Method that returns all the plants connected to the logged in user.
     * Author: Linn Borgström,
     * Updated by: Frida Jacobsson
     *
     * @return an arraylist if plants stored in the database
     */
    public ArrayList<Plant> getUserLibrary(User user) {
        ArrayList<Plant> plantList = new ArrayList<>();
        String query = "SELECT nickname, plant_id, last_watered, image_url FROM plant WHERE user_id = ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setInt(1, user.getUniqueId());

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String nickname = resultSet.getString("nickname");
                String plantId = resultSet.getString("plant_id");
                Date lastWatered = resultSet.getDate("last_watered");
                String imageURL = resultSet.getString("image_url");
                long waterFrequency = plantRepository.getWaterFrequency(plantId);
                plantList.add(new Plant(nickname, plantId, lastWatered, waterFrequency, imageURL));
            }
        } catch (SQLException | IOException | InterruptedException exception) {
            exception.printStackTrace();
        }

        return plantList;
    }





    /**
     * Method that returns one specific plant based on nickname.
     *
     * @param nickname
     * @return an instance of a specific plant from the database, null if no plant with the specific nickname exists
     */
    public Plant getPlant(User user, String nickname) {
        Plant plant = null;
        String sqlSafeNickname = nickname.replace("'", "''");
        String query = "SELECT nickname, plant_id, last_watered, image_url FROM plant WHERE user_id = ? AND nickname = ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setInt(1, user.getUniqueId());
            preparedStatement.setString(2, sqlSafeNickname);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String plantId = resultSet.getString("plant_id");
                Date lastWatered = resultSet.getDate("last_watered");
                String imageURL = resultSet.getString("image_url");
                long waterFrequency = plantRepository.getWaterFrequency(plantId);
                plant = new Plant(nickname, plantId, lastWatered, waterFrequency, imageURL);
            }
        } catch (SQLException | IOException | InterruptedException sqlException) {
            sqlException.printStackTrace();
        }

        return plant;
    }

    /**
     * Method that makes a query to delete a specific plant from table Plant
     *
     * @param user     the user that owns the plant
     * @param nickname nickname of the plant
     * @return boolean result depending on the result, false if exception
     */
    public boolean deletePlant(User user, String nickname) {
        boolean plantDeleted = false;
        String sqlSafeNickname = nickname.replace("'", "''");
        String plantQuery = "SELECT plant_id FROM plant WHERE user_id = ? AND nickname = ?;";
        String detailsQuery = "DELETE FROM plantdetails WHERE plant_id = ?;";

        try (PreparedStatement plantStatement = database.prepareStatement(plantQuery);
             PreparedStatement detailsStatement = database.prepareStatement(detailsQuery)) {

            plantStatement.setInt(1, user.getUniqueId());
            plantStatement.setString(2, sqlSafeNickname);

            ResultSet resultSet = plantStatement.executeQuery();

            if (resultSet.next()) {
                String plantId = resultSet.getString("plant_id");

                detailsStatement.setString(1, plantId);
                detailsStatement.executeUpdate();

                String deletePlantQuery = "DELETE FROM plant WHERE user_id = ? AND nickname = ?;";
                try (PreparedStatement deletePlantStatement = database.prepareStatement(deletePlantQuery)) {
                    deletePlantStatement.setInt(1, user.getUniqueId());
                    deletePlantStatement.setString(2, sqlSafeNickname);
                    deletePlantStatement.executeUpdate();
                }

                plantDeleted = true;
            }

        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return plantDeleted;
    }




    /**
     * Method that makes a query to change the last watered date of a specific plant in table Plant
     *
     * @param user     the user that owns the plant
     * @param nickname nickname of the plant
     * @param date     new data to change to
     * @return boolean result depending on the result, false if exception
     */
    public boolean changeLastWatered(User user, String nickname, LocalDate date) {
        boolean dateChanged = false;
        String sqlSafeNickname = nickname.replace("'", "''");
        String query = "UPDATE plant SET last_watered = ? WHERE user_id = ? AND nickname = ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setDate(1, Date.valueOf(date));
            preparedStatement.setInt(2, user.getUniqueId());
            preparedStatement.setString(3, sqlSafeNickname);

            preparedStatement.executeUpdate();
            dateChanged = true;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return dateChanged;
    }

    public boolean changeNickname(User user, String nickname, String newNickname) {
        boolean nicknameChanged = false;
        String sqlSafeNickname = nickname.replace("'", "''");
        String sqlSafeNewNickname = newNickname.replace("'", "''");
        String query = "UPDATE plant SET nickname = ? WHERE user_id = ? AND nickname = ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, sqlSafeNewNickname);
            preparedStatement.setInt(2, user.getUniqueId());
            preparedStatement.setString(3, sqlSafeNickname);

            preparedStatement.executeUpdate();
            nicknameChanged = true;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return nicknameChanged;
    }

    public boolean changeAllToWatered(User user) {
        boolean dateChanged = false;
        LocalDate date = java.time.LocalDate.now();
        String query = "UPDATE plant SET last_watered = ? WHERE user_id = ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setDate(1, Date.valueOf(date));
            preparedStatement.setInt(2, user.getUniqueId());

            preparedStatement.executeUpdate();
            dateChanged = true;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return dateChanged;
    }

    public boolean changePlantPicture(User user, Plant plant) {
        boolean pictureChanged = false;
        String nickname = plant.getNickname();
        String sqlSafeNickname = nickname.replace("'", "''");
        String query = "UPDATE plant SET image_url = ? WHERE user_id = ? AND nickname = ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, plant.getImageURL());
            preparedStatement.setInt(2, user.getUniqueId());
            preparedStatement.setString(3, sqlSafeNickname);

            preparedStatement.executeUpdate();
            pictureChanged = true;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return pictureChanged;
    }
}