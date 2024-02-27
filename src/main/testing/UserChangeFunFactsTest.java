import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.myhappyplants.javalin.Javalin;
import se.myhappyplants.javalin.user.NewUpdateUserRequest;
import se.myhappyplants.javalin.utils.DbConnection;

import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class UserChangeFunFactsTest {

    private final Context ctx = mock(Context.class);



    @BeforeEach
    public void setUp() throws SQLException {




    }

    @Test
    public void PATCH_UserChangeFunFactsOn_200_Success() throws JsonProcessingException {

        NewUpdateUserRequest userRequest = new NewUpdateUserRequest();
        userRequest.funFactsActivated = true;
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonUserRequest = objectMapper.writeValueAsString(userRequest);

        doReturn("22").when(ctx).pathParam("id");
        when(ctx.body()).thenReturn(jsonUserRequest);


        Javalin.updateUser(ctx);
        verify(ctx).status(200);

    }

    @Test
    public void PATCH_UserChangeFunFactsOff_200_Success() throws JsonProcessingException {

        NewUpdateUserRequest userRequest = new NewUpdateUserRequest();
        userRequest.funFactsActivated = false;
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonUserRequest = objectMapper.writeValueAsString(userRequest);

        doReturn("22").when(ctx).pathParam("id");
        when(ctx.body()).thenReturn(jsonUserRequest);


        Javalin.updateUser(ctx);
        verify(ctx).status(200);

    }
}
