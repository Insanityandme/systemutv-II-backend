import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.myhappyplants.javalin.Javalin;
import se.myhappyplants.javalin.user.NewUpdateUserRequest;

import java.sql.SQLException;

import static org.mockito.Mockito.*;

public class UserChangeNotificationsTest {
    private final Context ctx = mock(Context.class);
    @BeforeEach
    public void setUp() throws SQLException {
    }

    @Test
    public void PATCH_userChangeNotificationsOn_200_Success() throws JsonProcessingException {
        NewUpdateUserRequest userRequest = new NewUpdateUserRequest();
        userRequest.notificationsActivated = true;
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonUserRequest = objectMapper.writeValueAsString(userRequest);

        doReturn("1").when(ctx).pathParam("id");
        when(ctx.body()).thenReturn(jsonUserRequest);

        Javalin.updateUser(ctx);
        verify(ctx).status(200);
    }

    @Test
    public void PATCH_userChangeNotificationsOff_200_Success() throws JsonProcessingException {
        NewUpdateUserRequest userRequest = new NewUpdateUserRequest();
        userRequest.notificationsActivated = false;
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonUserRequest = objectMapper.writeValueAsString(userRequest);

        doReturn("1").when(ctx).pathParam("id");
        when(ctx.body()).thenReturn(jsonUserRequest);

        Javalin.updateUser(ctx);
        verify(ctx).status(200);
    }
}
