import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.myhappyplants.server.services.IDatabaseConnection;
import se.myhappyplants.server.services.IQueryExecutor;
import se.myhappyplants.server.services.UserRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserDeleteAccTest {

    @Mock
    private IQueryExecutor databaseMyHappyPlants;

    @InjectMocks
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testDeleteAccountSuccess() throws SQLException {
        PreparedStatement preparedStatementMock = mock(PreparedStatement.class);
        when(databaseMyHappyPlants.prepareStatement(any(String.class))).thenReturn(preparedStatementMock);

        ResultSet resultSetMock = mock(ResultSet.class);
        when(resultSetMock.next()).thenReturn(true);
        when(databaseMyHappyPlants.executeQuery(any(String.class))).thenReturn(resultSetMock);

        when(userRepository.checkLogin(any(String.class), any(String.class))).thenReturn(true);

        assertTrue(userRepository.deleteAccount("test@example.com", "password"));

        verify(databaseMyHappyPlants, times(1)).prepareStatement(any(String.class));
        verify(resultSetMock, times(1)).next();
        verify(databaseMyHappyPlants, times(1)).executeQuery(any(String.class));
    }

    @Test
    public void testDeleteAccountFailure() throws SQLException {
        PreparedStatement preparedStatementMock = mock(PreparedStatement.class);
        when(databaseMyHappyPlants.prepareStatement(any(String.class))).thenReturn(preparedStatementMock);

        ResultSet resultSetMock = mock(ResultSet.class);
        when(resultSetMock.next()).thenReturn(false);
        when(databaseMyHappyPlants.executeQuery(any(String.class))).thenReturn(resultSetMock);

        when(userRepository.checkLogin(any(String.class), any(String.class))).thenReturn(false);

        assertFalse(userRepository.deleteAccount("test@example.com", "password"));

        verify(databaseMyHappyPlants, times(1)).prepareStatement(any(String.class));
        verify(resultSetMock, times(1)).next();
        verify(databaseMyHappyPlants, times(1)).executeQuery(any(String.class));
    }
}
