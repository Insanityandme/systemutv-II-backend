import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import se.myhappyplants.javalin.Javalin;
import se.myhappyplants.javalin.login.NewLoginRequest;
import se.myhappyplants.javalin.user.NewDeleteUserRequest;

import io.javalin.http.Context;

import org.junit.jupiter.api.*;
import se.myhappyplants.javalin.user.NewUserRequest;
import se.myhappyplants.javalin.utils.DbConnection;

import java.util.Random;

import static org.mockito.Mockito.*;

/**
 * REQUIREMENT: F.DP.6
 */
public class UserDeleteAccTest {
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
    public void DELETE_userDeleteAcc_204_Success() {
        NewDeleteUserRequest del = new NewDeleteUserRequest();
        del.password = password;

        when(ctx.bodyAsClass(NewDeleteUserRequest.class)).thenReturn(del);
        doReturn(getUserId).when(ctx).pathParam("id");

        Javalin.deleteUser(ctx);

        verify(ctx).status(204);
    }

    @Test
    public void DELETE_userDeleteAcc_400_Fail() {
        NewDeleteUserRequest del = new NewDeleteUserRequest();
        del.password = "wrong_password";

        when(ctx.bodyAsClass(NewDeleteUserRequest.class)).thenReturn(del);
        doReturn(getUserId).when(ctx).pathParam("id");

        Javalin.deleteUser(ctx);

        verify(ctx).status(400);
    }
}
