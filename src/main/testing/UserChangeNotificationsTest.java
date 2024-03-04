import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import se.myhappyplants.javalin.Javalin;
import se.myhappyplants.javalin.login.NewLoginRequest;
import se.myhappyplants.javalin.user.NewUpdateUserRequest;
import se.myhappyplants.javalin.user.NewUserRequest;
import se.myhappyplants.javalin.utils.DbConnection;

import java.sql.SQLException;
import java.util.Random;

import static org.mockito.Mockito.*;

/**
 * Requirement: F.DP.14
 */

public class UserChangeNotificationsTest {
    private final Context ctx = mock(Context.class);
    private final Context ctxSetUp = mock(Context.class);
    private String email;
    private String username;
    private String password;

    private String getUserId;

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        DbConnection.path = "myHappyPlantsDBTEST.db";

        email = generateRandomString(8) + "@example.com";
        username = generateRandomString(10);
        password = generateRandomString(12);

        NewUserRequest testUser = new NewUserRequest(email, username, password);
        when(ctxSetUp.bodyAsClass(NewUserRequest.class)).thenReturn(testUser);
        Javalin.createUser(ctxSetUp);

        NewLoginRequest login = new NewLoginRequest(email, password);
        when(ctxSetUp.bodyAsClass(NewLoginRequest.class)).thenReturn(login);

        StringBuilder capturedResult = new StringBuilder();
        doAnswer((Answer<Void>) invocation -> {
            Object[] args = invocation.getArguments();
            String jsonResult = (String) args[0];
            capturedResult.append(jsonResult);
            return null;
        }).when(ctxSetUp).result(anyString());

        Javalin.login(ctxSetUp);

        verify(ctxSetUp).status(201);
        verify(ctxSetUp).status(200);

        String capturedResultString = capturedResult.toString();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(capturedResultString);
        getUserId = String.valueOf(jsonNode.get("id"));
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
    public void PATCH_userChangeNotificationsOn_200_Success() {
        String jsonUserRequest = "{\"notificationsActivated\":true}";

        doReturn(getUserId).when(ctx).pathParam("id");
        when(ctx.body()).thenReturn(jsonUserRequest);

        Javalin.updateUser(ctx);
        verify(ctx).status(200);
    }

    @Test
    public void PATCH_userChangeNotificationsOff_200_Success() {
        String jsonUserRequest = "{\"notificationsActivated\":false}";

        doReturn(getUserId).when(ctx).pathParam("id");
        when(ctx.body()).thenReturn(jsonUserRequest);

        Javalin.updateUser(ctx);
        verify(ctx).status(200);
    }
}
