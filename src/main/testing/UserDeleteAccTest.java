import io.javalin.http.BadRequestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.myhappyplants.server.services.*;

import io.javalin.http.Context;
import se.myhappyplants.shared.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class UserDeleteAccTest {

    @Mock
    private IQueryExecutor databaseMock;

    @InjectMocks
    private UserRepository userRepository;

    private Context context;

    @BeforeEach
    public void setUp() {
        databaseMock = mock(IQueryExecutor.class);
        userRepository = new UserRepository(databaseMock);
        context = mock(Context.class);

    }

    @Test
    public void testDeleteAccountSuccessfully() throws SQLException {



        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(databaseMock.prepareStatement("SELECT id FROM user WHERE email = ?;"))
                .thenReturn(preparedStatement);

        ResultSet resultSet = mock(ResultSet.class);
        when(databaseMock.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        when(context.queryParam("email")).thenReturn("test@example.com");
        when(context.queryParam("password")).thenReturn("password");
        verify(databaseMock).prepareStatement("SELECT id FROM user WHERE email = ?;");
        verify(databaseMock).executeQuery(anyString());

        verify(context).status(200);

    }

    @Test
    public void testDeleteAccountUnsuccessfulLogin() {

    }



}
