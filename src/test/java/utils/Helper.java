package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import se.myhappyplants.javalin.Javalin;
import se.myhappyplants.javalin.login.NewLoginRequest;
import se.myhappyplants.javalin.plant.NewPlantRequest;
import se.myhappyplants.javalin.user.NewDeleteUserRequest;
import se.myhappyplants.javalin.user.NewUserRequest;

import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class Helper {
    public static String getUserIdForTest() {
        Context ctx = mock(Context.class);

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
        JsonNode jsonNode;

        try {
            jsonNode = objectMapper.readTree(capturedResultString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return String.valueOf(jsonNode.get("id"));
    }

    public static void createUser() {
        Context ctx = mock(Context.class);

        // Create user to be used in the test
        NewUserRequest user = new NewUserRequest("test@mail.com", "test", "test");
        when(ctx.bodyAsClass(NewUserRequest.class)).thenReturn(user);
        Javalin.createUser(ctx);
        verify(ctx).status(201);
    }

    public static String addPlantToUser(String userId) {
        Context ctx = mock(Context.class);

        NewPlantRequest plant = new NewPlantRequest(0, "test", "test",
                "test", "test", "1970-01-01",
                1, 1, "test", "test");
        when(ctx.bodyAsClass(NewPlantRequest.class)).thenReturn(plant);
        when(ctx.pathParam("id")).thenReturn(userId);

        // get plant ID from saved plant
        AtomicReference<String> json = new AtomicReference<>("");
        when(ctx.result(anyString())).thenAnswer(invocation -> {
            String result = invocation.getArgument(0, String.class);
            json.set(result);
            return null;
        });

        Javalin.savePlant(ctx);
        verify(ctx).status(201);

        // Get ID from plant json response
        ObjectMapper objectMapper = new ObjectMapper();
        String capturedResultString = json.toString();
        JsonNode jsonNode;

        try {
            jsonNode = objectMapper.readTree(capturedResultString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return String.valueOf(jsonNode.get("id"));
    }

    public static void getFilename() {

    }

    public static void deleteUser(String userId) {
        Context ctx = mock(Context.class);

        NewDeleteUserRequest del = new NewDeleteUserRequest();
        del.password = "test";
        when(ctx.bodyAsClass(NewDeleteUserRequest.class)).thenReturn(del);
        when(ctx.pathParam("id")).thenReturn(userId);
        Javalin.deleteUser(ctx);
        verify(ctx).status(204);
    }
}
