package se.myhappyplants.javalin.user;

import se.myhappyplants.server.services.DatabaseConnection;
import se.myhappyplants.server.services.IQueryExecutor;
import se.myhappyplants.server.services.QueryExecutor;
import se.myhappyplants.server.services.UserRepository;
import se.myhappyplants.shared.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

// This is a service, it should be independent from Javalin
public class UserService {
    private static Map<Integer, User> users = new HashMap<>();
    private static AtomicInteger lastId;
    private DatabaseConnection connectionMyHappyPlants1 = new DatabaseConnection("myHappyPlantsDB");
    private IQueryExecutor databaseMyHappyPlants = new QueryExecutor(connectionMyHappyPlants1);
    private UserRepository userRepository = new UserRepository(databaseMyHappyPlants);

    static {
        // users.put(0, new User(0, "alice@alice.java", "Alice", "123"));
        // users.put(1, new User(1, "bob@bob.java", "Bob", "123"));
        lastId = new AtomicInteger(users.size());
    }

    public static void save(String password, String username, String email) {
        int id = lastId.incrementAndGet();
        //  users.put(id, new User(id, email, username, password));
        // userRepository.registerUser(new User(email, username, password, true));

    }

    public static Collection<User> getAll() {
        return users.values();
    }

    public static void update(int userId, String email, String username, String password) {
        // users.put(userId, new User(userId, email, username, password));
    }

    public static User findById(int userId) {
        return users.get(userId);
    }

    public User findByEmail(String email) {
        return userRepository.getUserDetails(email);
    }

    public static void delete(int userId) {
        users.remove(userId);
    }

}
