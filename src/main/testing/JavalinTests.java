import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.myhappyplants.javalin.Javalin;
import se.myhappyplants.javalin.plant.Fact;
import se.myhappyplants.javalin.plant.NewPlantRequest;
import se.myhappyplants.javalin.user.NewDeleteUserRequest;
import se.myhappyplants.javalin.user.NewUserRequest;
import se.myhappyplants.javalin.utils.DbConnection;

import static org.mockito.Mockito.*;

public class JavalinTests {

    @BeforeAll
    public static void setup() {
        DbConnection.path = "myHappyPlantsDBTEST.db";
    }

    // Requirement: F.DP.4
    @Test
    public void createUserSuccess() {
        Context ctx = mock(Context.class);

        // Create user to be used in the test
        NewUserRequest user = new NewUserRequest("test@mail.com", "test", "test");
        when(ctx.bodyAsClass(NewUserRequest.class)).thenReturn(user);
        Javalin.createUser(ctx);
        verify(ctx).status(201);

        // Login to get ID for deletion
        String id = Helper.getUserIdForTest(ctx);

        // Delete user for repeatable tests
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

        // Create user to be used in the test
        NewUserRequest user = new NewUserRequest("test@mail.com", "test", "test");
        when(ctx.bodyAsClass(NewUserRequest.class)).thenReturn(user);
        Javalin.createUser(ctx);
        verify(ctx).status(201);

        // Create another user to trigger status code 409
        NewUserRequest user2 = new NewUserRequest("test@mail.com", "test", "test");
        when(ctx.bodyAsClass(NewUserRequest.class)).thenReturn(user2);
        Javalin.createUser(ctx);
        verify(ctx).status(409);

        // Login to get ID for deletion
        String id = Helper.getUserIdForTest(ctx);

        // Delete user for repeatable tests
        NewDeleteUserRequest del = new NewDeleteUserRequest();
        del.password = "test";
        when(ctx.bodyAsClass(NewDeleteUserRequest.class)).thenReturn(del);
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.deleteUser(ctx);
        verify(ctx).status(204);
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

    // Requirement: F.DP.14
    @Test
    public void updateUserByIdPassworSuccess() {
        Context ctx = mock(Context.class);

        // Create user to be used in the test
        NewUserRequest user = new NewUserRequest("test@mail.com", "test", "test");
        when(ctx.bodyAsClass(NewUserRequest.class)).thenReturn(user);
        Javalin.createUser(ctx);
        verify(ctx).status(201);

        // Get ID by logging in
        String id = Helper.getUserIdForTest(ctx);

        // Request to update user password
        String request = "{\"oldPassword\":\"test\",\"newPassword\":\"hi\"}";
        when(ctx.body()).thenReturn(request);
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.updateUser(ctx);
        // We have to verify the status code twice for some reason
        verify(ctx, times(2)).status(200);

        // Delete user for repeatable tests
        NewDeleteUserRequest del = new NewDeleteUserRequest();
        del.password = "hi";
        when(ctx.bodyAsClass(NewDeleteUserRequest.class)).thenReturn(del);
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.deleteUser(ctx);
        verify(ctx).status(204);
    }

    // Requirement: F.DP.14
    @Test
    public void updateUserByIdPasswordFail() {
        Context ctx = mock(Context.class);

        // Create user to be used in the test
        NewUserRequest user = new NewUserRequest("test@mail.com", "test", "test");
        when(ctx.bodyAsClass(NewUserRequest.class)).thenReturn(user);
        Javalin.createUser(ctx);
        verify(ctx).status(201);

        // Get ID by logging in
        String id = Helper.getUserIdForTest(ctx);

        // Request to update user password with the wrong password
        String request = "{\"oldPassword\":\"hi\",\"newPassword\":\"test\"}";
        when(ctx.body()).thenReturn(request);
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.updateUser(ctx);
        verify(ctx).status(400);

        // Delete user for repeatable tests
        NewDeleteUserRequest del = new NewDeleteUserRequest();
        del.password = "test";
        when(ctx.bodyAsClass(NewDeleteUserRequest.class)).thenReturn(del);
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.deleteUser(ctx);
        verify(ctx).status(204);
    }


    // Requirement: F.DP.16
    @Test
    public void getUserByIdSuccess() {
        Context ctx = mock(Context.class);

        // Create user to be used in the test
        NewUserRequest user = new NewUserRequest("test@mail.com", "test", "test");
        when(ctx.bodyAsClass(NewUserRequest.class)).thenReturn(user);
        Javalin.createUser(ctx);
        verify(ctx).status(201);

        // Get ID by logging in
        String id = Helper.getUserIdForTest(ctx);

        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.getUserById(ctx);
        verify(ctx, times(2)).status(200);

        // Delete user for repeatable tests
        NewDeleteUserRequest del = new NewDeleteUserRequest();
        del.password = "test";
        when(ctx.bodyAsClass(NewDeleteUserRequest.class)).thenReturn(del);
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.deleteUser(ctx);
        verify(ctx).status(204);
    }

    // Requirement: F.DP.16
    @Test
    public void getUserByIdFail() {
        Context ctx = mock(Context.class);

        // Incorrect ID
        String id = "-9999";

        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.getUserById(ctx);
        verify(ctx).status(404);
    }

    // Requirement: F.DP.4
    @Test
    public void getAllPlantsSuccesful() {
        Context ctx = mock(Context.class);

        // Create user to be used in the test
        NewUserRequest user = new NewUserRequest("test@mail.com", "test", "test");
        when(ctx.bodyAsClass(NewUserRequest.class)).thenReturn(user);
        Javalin.createUser(ctx);
        verify(ctx).status(201);

        // Get ID by logging in
        String id = Helper.getUserIdForTest(ctx);

        // Add plant to a user
        NewPlantRequest plant = new NewPlantRequest(0, "test", "test",
                "test", "test", "1970-01-01",
                1, 1, "test", "test");
        when(ctx.bodyAsClass(NewPlantRequest.class)).thenReturn(plant);
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.savePlant(ctx);
        verify(ctx, times(2)).status(201);

        // Get all plants based on user ID
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.getAllPlants(ctx);
        verify(ctx, times(2)).status(200);

        // Delete user and plants for repeatable tests
        NewDeleteUserRequest del = new NewDeleteUserRequest();
        del.password = "test";
        when(ctx.bodyAsClass(NewDeleteUserRequest.class)).thenReturn(del);
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.deleteUser(ctx);
        verify(ctx).status(204);
    }

    // Requirement: F.DP.4
    @Test
    public void getAllPlantsNotFound() {
        Context ctx = mock(Context.class);

        // Create user to be used in the test
        NewUserRequest user = new NewUserRequest("test@mail.com", "test", "test");
        when(ctx.bodyAsClass(NewUserRequest.class)).thenReturn(user);
        Javalin.createUser(ctx);
        verify(ctx).status(201);

        // Get ID by logging in
        String id = Helper.getUserIdForTest(ctx);

        // Get all plants based on user ID
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.getAllPlants(ctx);
        verify(ctx).status(404);

        // Delete user for repeatable tests
        NewDeleteUserRequest del = new NewDeleteUserRequest();
        del.password = "test";
        when(ctx.bodyAsClass(NewDeleteUserRequest.class)).thenReturn(del);
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.deleteUser(ctx);
        verify(ctx).status(204);
    }

    // Requirement: F.DP.9
    @Test
    public void updateAllPlantsWateredSucces() {
        Context ctx = mock(Context.class);

        // Create user to be used in the test
        NewUserRequest user = new NewUserRequest("test@mail.com", "test", "test");
        when(ctx.bodyAsClass(NewUserRequest.class)).thenReturn(user);
        Javalin.createUser(ctx);
        verify(ctx).status(201);

        // Get ID by logging in
        String id = Helper.getUserIdForTest(ctx);

        // Add plant to a user
        NewPlantRequest plant = new NewPlantRequest(0, "test", "test",
                "test", "test", "1970-01-01",
                1, 1, "test", "test");
        when(ctx.bodyAsClass(NewPlantRequest.class)).thenReturn(plant);
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.savePlant(ctx);
        verify(ctx, times(2)).status(201);

        // Update all plants based on user ID
        // For some reason Mockito can't handle ctx.body and ctx.bodyAsClass as intended.
        // I need to invoke both to make sure that the information is correctly transmitted to
        // the Context object.
        String request = "{\"lastWatered\":\"2024-03-04\"}";
        NewPlantRequest lastWateredRequest = new NewPlantRequest();
        lastWateredRequest.lastWatered = "2024-03-04";
        when(ctx.bodyAsClass(NewPlantRequest.class)).thenReturn(lastWateredRequest);
        when(ctx.body()).thenReturn(request);
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.updateAllPlants(ctx);
        verify(ctx, times(2)).status(200);

        // Delete user and plants for repeatable tests
        NewDeleteUserRequest del = new NewDeleteUserRequest();
        del.password = "test";
        when(ctx.bodyAsClass(NewDeleteUserRequest.class)).thenReturn(del);
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.deleteUser(ctx);
        verify(ctx).status(204);
    }

    // Requirement: F.DP.9
    @Test
    public void updateAllPlantsWateredFail() {
        Context ctx = mock(Context.class);

        // Create user to be used in the test
        NewUserRequest user = new NewUserRequest("test@mail.com", "test", "test");
        when(ctx.bodyAsClass(NewUserRequest.class)).thenReturn(user);
        Javalin.createUser(ctx);
        verify(ctx).status(201);

        // Get ID by logging in
        String id = Helper.getUserIdForTest(ctx);

        // Add plant to a user
        NewPlantRequest plant = new NewPlantRequest(0, "test", "test",
                "test", "test", "1970-01-01",
                1, 1, "test", "test");
        when(ctx.bodyAsClass(NewPlantRequest.class)).thenReturn(plant);
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.savePlant(ctx);
        verify(ctx, times(2)).status(201);

        // Update all plants based on user ID
        // For some reason Mockito can't handle ctx.body and ctx.bodyAsClass as intended.
        // I need to invoke both to make sure that the information is correctly transmitted to
        // the Context object even though Javalin handles it correctly in the actual code.
        String request = "{\"lastWatered\":\"Not a date\"}";
        NewPlantRequest lastWateredRequest = new NewPlantRequest();
        lastWateredRequest.lastWatered = "Not a date";
        when(ctx.bodyAsClass(NewPlantRequest.class)).thenReturn(lastWateredRequest);
        when(ctx.body()).thenReturn(request);
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.updateAllPlants(ctx);
        verify(ctx ).status(400);

        // Delete user and plants for repeatable tests
        NewDeleteUserRequest del = new NewDeleteUserRequest();
        del.password = "test";
        when(ctx.bodyAsClass(NewDeleteUserRequest.class)).thenReturn(del);
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.deleteUser(ctx);
        verify(ctx).status(204);
    }
}