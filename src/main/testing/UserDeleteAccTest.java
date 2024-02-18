import io.javalin.Javalin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import se.myhappyplants.server.services.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;



public class UserDeleteAccTest {

    @Mock
    private IQueryExecutor database;

    @InjectMocks
    private static UserRepository userRepository;

    private static Javalin app;

    @BeforeAll
    public static void setUp() {
        IDatabaseConnection connectionMyHappyPlants1 = new DatabaseConnection("myHappyPlantsDB");
        IQueryExecutor realQueryExecutor = new QueryExecutor(connectionMyHappyPlants1);
        userRepository = new UserRepository(realQueryExecutor);
        app = Javalin.create().start(7000);

    }

    @AfterAll
    public static void tearDown() {
        app.stop();
    }

    @Test
    public void testDeleteAccountSuccess() throws SQLException {
        when(userRepository.checkLogin(any(String.class), any(String.class))).thenReturn(true);

        PreparedStatement preparedStatementMock = mock(PreparedStatement.class);
        when(database.prepareStatement(any())).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeUpdate()).thenReturn(1);

        ResultSet resultSetMock = mock(ResultSet.class);
        when(resultSetMock.next()).thenReturn(true);
        when(resultSetMock.getInt("id")).thenReturn(1);
        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);

        assertTrue(userRepository.deleteAccount("test@example.com", "password"));

        verify(userRepository, times(1)).checkLogin(any(String.class), any(String.class));
        verify(database, times(2)).prepareStatement(any(String.class));
        verify(resultSetMock, times(1)).next();
        verify(resultSetMock, times(1)).getInt("id");
        verify(preparedStatementMock, times(2)).executeUpdate();
        verify(database, times(1)).beginTransaction();
        verify(database, times(1)).endTransaction();
    }


    @Test
    public void testDeleteAccountFailure() throws SQLException {
        when(userRepository.checkLogin(any(), any())).thenReturn(false);

        assertFalse(userRepository.deleteAccount("test@example.com", "password"));

        verify(userRepository, times(1)).checkLogin(any(), any());
        verify(database, never()).prepareStatement(any(String.class));
        verify(database, never()).beginTransaction();
        verify(database, never()).endTransaction();
    }
}
