import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.myhappyplants.javalin.Javalin;
import se.myhappyplants.javalin.user.NewUpdateUserRequest;
import se.myhappyplants.javalin.user.NewUserRequest;

import java.sql.SQLException;
import java.util.Random;

import static org.mockito.Mockito.*;

public class UserChangeNotificationsTest {
    private final Context ctx = mock(Context.class);
    private String email;
    private String username;
    private String password;

    private Long getUserId;
    @BeforeEach
    public void setUp() throws SQLException, IllegalAccessException, NoSuchFieldException {
        email = generateRandomString(8) + "@example.com";
        username = generateRandomString(10);
        password = generateRandomString(12);
        NewUserRequest testUser = new NewUserRequest(email,username,password);

        when(ctx.bodyAsClass(NewUserRequest.class)).thenReturn(testUser);

        Javalin.createUser(ctx);

        verify(ctx).status(201);

        java.lang.reflect.Field field = Javalin.class.getDeclaredField("generatedUserId");
        field.setAccessible(true);
        getUserId = (long) field.get(null);
    }

    private String generateRandomString(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder randomString = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            randomString.append(characters.charAt(index));
        }

        return randomString.toString();
    }
    @Test
    public void PATCH_userChangeNotificationsOn_200_Success() throws JsonProcessingException {
        NewUpdateUserRequest userRequest = new NewUpdateUserRequest();
        userRequest.notificationsActivated = true;
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonUserRequest = objectMapper.writeValueAsString(userRequest);

        doReturn(getUserId.toString()).when(ctx).pathParam("id");
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

        doReturn(getUserId.toString()).when(ctx).pathParam("id");
        when(ctx.body()).thenReturn(jsonUserRequest);

        Javalin.updateUser(ctx);
        verify(ctx).status(200);
    }
}
