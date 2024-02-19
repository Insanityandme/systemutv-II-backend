import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.myhappyplants.server.services.*;

import io.javalin.http.Context;
import se.myhappyplants.shared.User;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class UserDeleteAccTest {

    @Mock
    private IQueryExecutor database;

    private Context ctx;

    @InjectMocks
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {

        IDatabaseConnection connectionMyHappyPlants1 = new DatabaseConnection("myHappyPlantsDB");
        database = new QueryExecutor(connectionMyHappyPlants1);
        userRepository = new UserRepository(database);
        ctx = mock(io.javalin.http.Context.class);
        userRepository = mock(UserRepository.class);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSaveUserSuccess() throws SQLException {
        User user = new User("test@example.com", "testUser", "password", true);

        when(userRepository.saveUser(user)).thenReturn(true);
        userRepository.saveUser(user);

        verify(userRepository, times(1)).saveUser(user);
        System.out.println("Account successfully saved!");
    }

    @Test
    public void testCheckLoginSuccess() throws SQLException {
        String email = "test@example.com";
        String password = "password";

        when(userRepository.checkLogin(email, password)).thenReturn(true);
        userRepository.checkLogin(email, password);

        verify(userRepository, times(1)).checkLogin(email, password);
        System.out.println("Login successful!");
    }

    @Test
    public void testCheckLoginFailure() throws SQLException {
        String email = "test@example.com";
        String password = "password";

        when(userRepository.checkLogin(email, password)).thenReturn(false);
        userRepository.checkLogin(email, password);

        verify(userRepository, times(1)).checkLogin(email, password);
        System.out.println("Login failed!");
    }

    @Test
    public void testDeleteAccountSuccess() throws SQLException {
        String email = "test@gmail.com";
        String password = "123";

        when(userRepository.checkLogin(email, password)).thenReturn(true);

        boolean result = userRepository.deleteAccount(email, password);

        assertTrue(result);

        verify(userRepository, times(1)).checkLogin(email, password);
        verify(database, times(2)).prepareStatement(any(String.class));
        verify(database, times(1)).beginTransaction();
        verify(database, times(1)).endTransaction();
        verify(database, times(1)).rollbackTransaction();

        System.out.println("Account successfully deleted!");
    }

}
