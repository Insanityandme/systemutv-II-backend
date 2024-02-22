import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import se.myhappyplants.server.controller.ResponseController;
import se.myhappyplants.server.services.*;
import se.myhappyplants.shared.User;

import java.net.UnknownHostException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;

public class UserChangeFunFacts {

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
    public void turnOnFunFacts(){
        boolean result = userRepositorySpy.changeFunFacts(testUser,true);
        assertTrue(result);
    }

    @Test
    public void turnOffFunFacts(){
        boolean result = userRepositorySpy.changeFunFacts(testUser,false);
        assertTrue(result);
    }
}
