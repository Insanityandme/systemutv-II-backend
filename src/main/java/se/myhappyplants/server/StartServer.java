package se.myhappyplants.server;

import se.myhappyplants.server.controller.ResponseController;
import se.myhappyplants.server.services.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class that starts the server
 * Created by: Frida Jacobson, Eric Simonson, Anton Holm, Linn Borgström, Christopher O'Driscoll
 * Updated by: Frida Jacobsson 2021-05-21
 */
public class StartServer {
    public static void main(String[] args) {
        try {
            DatabaseConnection connectionMyHappyPlants1 = new DatabaseConnection("myHappyPlantsDB");
            IQueryExecutor databaseMyHappyPlants = new QueryExecutor(connectionMyHappyPlants1);
            UserRepository userRepository = new UserRepository(databaseMyHappyPlants);
            PlantRepository plantRepository = new PlantRepository(databaseMyHappyPlants);
            UserPlantRepository userPlantRepository = new UserPlantRepository(plantRepository, databaseMyHappyPlants);
            ResponseController responseController = new ResponseController(userRepository, userPlantRepository, plantRepository);

            //Starting the server
            ServerSocket serverSocket = new ServerSocket(2555);
            ExecutorService executor = Executors.newFixedThreadPool(5);
            System.out.println("Server running");

            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    executor.submit(new ClientHandlerTask(socket, responseController));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}