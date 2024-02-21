
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import se.myhappyplants.server.controller.ResponseController;
import se.myhappyplants.server.services.*;
import se.myhappyplants.shared.User;
import java.net.UnknownHostException;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class UserDeleteAccTest {

    @Mock
    private IDatabaseConnection databaseConnectionMock;

    @Mock
    private IQueryExecutor databaseMock;

    @InjectMocks
    private DatabaseConnection databaseConnectionSpy;

    @InjectMocks
    private QueryExecutor queryExecutorSpy;

    @InjectMocks
    private UserRepository userRepositorySpy;

    @InjectMocks
    private PlantRepository plantRepositorySpy;

    @InjectMocks
    private UserPlantRepository userPlantRepositorySpy;

    @InjectMocks
    private ResponseController responseControllerSpy;

    private User testUser;

    @BeforeEach
    public void setUp() throws UnknownHostException, SQLException {
        databaseConnectionSpy = spy(new DatabaseConnection("myHappyPlantsDBTEST"));
        queryExecutorSpy = spy(new QueryExecutor(databaseConnectionSpy));
        userRepositorySpy = spy(new UserRepository(queryExecutorSpy));
        plantRepositorySpy = spy(new PlantRepository(queryExecutorSpy));
        userPlantRepositorySpy = spy(new UserPlantRepository(plantRepositorySpy, queryExecutorSpy));
        responseControllerSpy = spy(new ResponseController(userRepositorySpy, userPlantRepositorySpy, plantRepositorySpy));
        testUser = new User("test@gmail.com", "123", "123", true);
        if(!userRepositorySpy.checkLogin("test@gmail.com","123")){
            userRepositorySpy.saveUser(testUser);
        }



    }

    @Test
    public void deleteAccount_Successful() throws SQLException {
        boolean result = userRepositorySpy.deleteAccount("test@gmail.com", "123");
        assertTrue(result);

    }

    @Test
    public void deleteAccount_NonexistentUser_Failure() {
        boolean result = userRepositorySpy.deleteAccount("nonexistent@gmail.com", "password123");
        assertFalse(result);

    }

    @Test
    public void deleteAccount_IncorrectPassword_Failure() {
        boolean result = userRepositorySpy.deleteAccount("test@gmail.com", "incorrectPassword");
        assertFalse(result);
    }





}
