package se.myhappyplants.javalin.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.http.InternalServerErrorResponse;
import se.myhappyplants.shared.WaterCalculator;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Helper {
    public static Connection getConnection() {
        Connection database;

        try {
            database = DbConnection.getInstance().getConnection();
        } catch (SQLException e) {
            throw new InternalServerErrorResponse("Unable to establish connection to database");
        }
        return database;
    }

    public static String objecToJson(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        // This is necessary for LocalDate objects to work with Jackson...
        mapper.registerModule(new JavaTimeModule());

        String json;

        try {
            json = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return json;
    }

    public static long calculateWaterFrequencyForWatering(int waterFrequency) {
        long waterFrequencyMilli;
        long week = 604000000L;
        if (waterFrequency <= 200) {
            waterFrequencyMilli = week * 4;
        } else if (waterFrequency <= 400) {
            waterFrequencyMilli = week * 3;
        } else if (waterFrequency <= 600) {
            waterFrequencyMilli = week * 2;
        } else if (waterFrequency <= 800) {
            waterFrequencyMilli = week;
        } else {
            waterFrequencyMilli = week / 2;
        }
        return waterFrequencyMilli;
    }

    public static long getWaterFrequency(String plantId)  {
        Connection database = getConnection();

        long waterFrequency = -1;
        String query = "SELECT water_frequency FROM plantdetails WHERE id = ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, plantId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String waterText = resultSet.getString("water_frequency");
                int water = (isNumeric(waterText)) ? Integer.parseInt(waterText) : -1;
                waterFrequency = calculateWaterFrequencyForWatering(water);
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return waterFrequency;
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
}
