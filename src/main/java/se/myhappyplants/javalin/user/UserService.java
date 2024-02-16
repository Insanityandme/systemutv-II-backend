package se.myhappyplants.javalin.user;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

// This is a service, it should be independent from Javalin
public class UserService {

    private static Map<Integer, User> users = new HashMap<>();
    private static AtomicInteger lastId;

    static {
        users.put(0, new User(0, "alice@alice.java", "Alice", "123"));
        users.put(1, new User(1, "bob@bob.java", "Bob", "123"));
        lastId = new AtomicInteger(users.size());
    }

    public static void save(String password, String username, String email) {
        int id = lastId.incrementAndGet();
        users.put(id, new User(id, email, username, password));
    }

    public static Collection<User> getAll() {
        return users.values();
    }

    public static void update(int userId, String email, String username, String password) {
        users.put(userId, new User(userId, email, username, password));
    }

    public static User findById(int userId) {
        return users.get(userId);
    }

    public static void delete(int userId) {
        users.remove(userId);
    }

}
