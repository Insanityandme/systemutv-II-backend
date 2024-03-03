import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.myhappyplants.javalin.Javalin;
import se.myhappyplants.javalin.login.NewLoginRequest;
import se.myhappyplants.javalin.plant.Fact;
import se.myhappyplants.javalin.user.NewDeleteUserRequest;
import se.myhappyplants.javalin.user.NewUserRequest;
import se.myhappyplants.javalin.utils.DbConnection;

import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.*;

public class JavalinTests {

    @BeforeAll
    public static void setup() {
        DbConnection.path = "myHappyPlantsDBTEST.db";
    }

    // Requirement: F.DP.13
    @Test
    public void getFactSuccess() {
        Context ctx = mock(Context.class);

        when(ctx.pathParam("factId")).thenReturn("1");

        Javalin.getFact(ctx);

        verify(ctx).status(200);
    }

    // Requirement: F.DP.13
    @Test
    public void getFactNotFound() {
        Context ctx = mock(Context.class);
        Fact fact = new Fact();

        when(ctx.pathParam("factId")).thenReturn("5000");
        when(ctx.bodyAsClass(Fact.class)).thenReturn(fact);

        Javalin.getFact(ctx);

        verify(ctx).status(404);
    }


    // Requirement: F.DP.4
    @Test
    public void createUserSuccess() throws JsonProcessingException {
        Context ctx = mock(Context.class);

        // Create user to be used in the test
        NewUserRequest user = new NewUserRequest("test@mail.com", "test", "test");
        when(ctx.bodyAsClass(NewUserRequest.class)).thenReturn(user);
        Javalin.createUser(ctx);
        verify(ctx).status(201);

        // Login to get ID for deletion
        NewLoginRequest login = new NewLoginRequest("test@mail.com", "test");
        when(ctx.bodyAsClass(NewLoginRequest.class)).thenReturn(login);
        AtomicReference<String> json = new AtomicReference<>("");
        when(ctx.result(anyString())).thenAnswer(invocation -> {
            String result = invocation.getArgument(0, String.class);
            json.set(result);
            return null;
        });
        Javalin.login(ctx);
        verify(ctx).status(200);

        // Get ID from login json response
        ObjectMapper objectMapper = new ObjectMapper();
        String capturedResultString = json.toString();
        JsonNode jsonNode = objectMapper.readTree(capturedResultString);
        String id = String.valueOf(jsonNode.get("id"));

        // Delete user
        NewDeleteUserRequest del = new NewDeleteUserRequest();
        del.password = "test";
        when(ctx.bodyAsClass(NewDeleteUserRequest.class)).thenReturn(del);
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.deleteUser(ctx);
        verify(ctx).status(204);
    }


    // Requirement: F.DP.4
    @Test
    public void createUserNoUser() {
        Context ctx = mock(Context.class);

        // Empty object to show how it handles sending insufficient data
        NewUserRequest user = new NewUserRequest();

        when(ctx.bodyAsClass(NewUserRequest.class)).thenReturn(user);

        Javalin.createUser(ctx);

        verify(ctx).status(404);
    }

    // Requirement: F.DP.4
    @Test
    public void createUserAlreadyExist() {
        Context ctx = mock(Context.class);

        // Empty object to show how it handles sending insufficient data
        NewUserRequest user = new NewUserRequest();

        when(ctx.bodyAsClass(NewUserRequest.class)).thenReturn(user);

        Javalin.createUser(ctx);

        verify(ctx).status(404);
    }
}
