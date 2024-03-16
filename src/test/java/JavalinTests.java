import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.myhappyplants.javalin.Javalin;
import se.myhappyplants.javalin.plant.Fact;
import se.myhappyplants.javalin.plant.NewPlantRequest;
import se.myhappyplants.javalin.user.NewUserRequest;
import se.myhappyplants.javalin.utils.DbConnection;
import utils.Helper;

import java.io.*;
import java.util.concurrent.atomic.AtomicReference;

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
        String id = Helper.getUserIdForTest();

        // Delete user for repeatable tests
        Helper.deleteUser(id);
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
        String id = Helper.getUserIdForTest();

        // Delete user for repeatable tests
        Helper.deleteUser(id);
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
    public void updateUserByIdPasswordSuccess() {
        Context ctx = mock(Context.class);

        // Create user to be used in the test
        Helper.createUser();

        // Get ID by logging in
        String id = Helper.getUserIdForTest();

        // Request to update user password
        String request = "{\"oldPassword\":\"test\",\"newPassword\":\"hi\"}";
        when(ctx.body()).thenReturn(request);
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.updateUser(ctx);
        // We have to verify the status code twice for some reason
        verify(ctx).status(200);

        // Update it back to the previous password for deletion
        String request2 = "{\"oldPassword\":\"hi\",\"newPassword\":\"test\"}";
        when(ctx.body()).thenReturn(request2);
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.updateUser(ctx);
        verify(ctx, times(2)).status(200);

        // Delete user for repeatable tests
        Helper.deleteUser(id);
    }

    // Requirement: F.DP.14
    @Test
    public void updateUserByIdPasswordFail() {
        Context ctx = mock(Context.class);

        // Create user to be used in the test
        Helper.createUser();

        // Get ID by logging in
        String id = Helper.getUserIdForTest();

        // Request to update user password with the wrong password
        String request = "{\"oldPassword\":\"hi\",\"newPassword\":\"test\"}";
        when(ctx.body()).thenReturn(request);
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.updateUser(ctx);
        verify(ctx).status(400);

        // Delete user for repeatable tests
        Helper.deleteUser(id);
    }


    // Requirement: F.DP.16
    @Test
    public void getUserByIdSuccess() {
        Context ctx = mock(Context.class);

        // Create user to be used in the test
        Helper.createUser();

        // Get ID by logging in
        String id = Helper.getUserIdForTest();

        // Get user based on ID
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.getUserById(ctx);
        verify(ctx).status(200);

        // Delete user for repeatable tests
        Helper.deleteUser(id);
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

    // Requirement: F.DP.1 + F.SI.1
    @Test
    public void getAllPlantsSuccesful() {
        Context ctx = mock(Context.class);

        // Create user to be used in the test
        Helper.createUser();

        // Get ID by logging in
        String id = Helper.getUserIdForTest();

        // Add plant to a user
        Helper.addPlantToUser(id);

        // Get all plants based on user ID
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.getAllPlants(ctx);
        verify(ctx).status(200);

        // Delete user and plants for repeatable tests
        Helper.deleteUser(id);
    }

    // Requirement: F.DP.1 + F.SI.1
    @Test
    public void getAllPlantsNotFound() {
        Context ctx = mock(Context.class);

        // Create user to be used in the test
        Helper.createUser();

        // Get ID by logging in
        String id = Helper.getUserIdForTest();

        // Get all plants based on user ID
        when(ctx.pathParam("id")).thenReturn(id);
        Javalin.getAllPlants(ctx);
        verify(ctx).status(404);

        // Delete user for repeatable tests
        Helper.deleteUser(id);
    }

    // Requirement: F.DP.9
    @Test
    public void updateAllPlantsWateredSucces() {
        Context ctx = mock(Context.class);

        // Create user to be used in the test
        Helper.createUser();

        // Get ID by logging in
        String id = Helper.getUserIdForTest();

        // Add plant to a user
        Helper.addPlantToUser(id);

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
        verify(ctx).status(200);

        // Delete user and plants for repeatable tests
        Helper.deleteUser(id);
    }

    // Requirement: F.DP.9
    @Test
    public void updateAllPlantsWateredFail() {
        Context ctx = mock(Context.class);

        // Create user to be used in the test
        Helper.createUser();

        // Get ID by logging in
        String id = Helper.getUserIdForTest();

        // Add plant to a user
        Helper.addPlantToUser(id);

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
        verify(ctx).status(400);

        // Delete user and plants for repeatable tests
        Helper.deleteUser(id);
    }

    // Requirement: F.DP.10
    @Test
    public void getPlantSuccess() {
        Context ctx = mock(Context.class);

        // Create user for the test
        Helper.createUser();
        // Get ID by logging in
        String userId = Helper.getUserIdForTest();
        // Add plant to a user and get plant ID
        String plantId = Helper.addPlantToUser(userId);

        // Get plant based on user and plant ID
        when(ctx.pathParam("id")).thenReturn(userId);
        when(ctx.pathParam("plantId")).thenReturn(plantId);
        Javalin.getPlant(ctx);
        verify(ctx).status(200);

        // Delete user and plants for repeatable tests
        Helper.deleteUser(userId);
    }

    // Requirement: F.DP.10
    @Test
    public void getPlantFail() {
        Context ctx = mock(Context.class);

        // Create user for the test
        Helper.createUser();
        // Get ID by logging in
        String userId = Helper.getUserIdForTest();

        // Get plant based on user and plant ID
        when(ctx.pathParam("id")).thenReturn(userId);
        when(ctx.pathParam("plantId")).thenReturn("-999");
        Javalin.getPlant(ctx);
        verify(ctx).status(404);

        // Delete user and plants for repeatable tests
        Helper.deleteUser(userId);
    }

    // Requirement: F.DP.7
    @Test
    public void updatePlantLastWateredSuccess() {
        Context ctx = mock(Context.class);

        // Create user for the test
        Helper.createUser();
        // Get ID by logging in
        String userId = Helper.getUserIdForTest();
        // Add plant to a user and get plant ID
        String plantId = Helper.addPlantToUser(userId);

        // Update plant based on user and plant ID
        String request = "{\"lastWatered\":\"2024-03-04\"}";
        NewPlantRequest lastWateredRequest = new NewPlantRequest();
        lastWateredRequest.lastWatered = "2024-03-04";
        when(ctx.bodyAsClass(NewPlantRequest.class)).thenReturn(lastWateredRequest);
        when(ctx.body()).thenReturn(request);
        when(ctx.pathParam("id")).thenReturn(userId);
        when(ctx.pathParam("plantId")).thenReturn(plantId);
        Javalin.updatePlant(ctx);
        verify(ctx).status(200);

        // Delete user and plants for repeatable tests
        Helper.deleteUser(userId);
    }

    // Requirement: F.DP.7
    @Test
    public void updatePlantLastWateredFail() {
        Context ctx = mock(Context.class);

        // Create user for the test
        Helper.createUser();
        // Get ID by logging in
        String userId = Helper.getUserIdForTest();
        // Add plant to a user and get plant ID
        String plantId = Helper.addPlantToUser(userId);

        // Update plant based on user and plant ID
        String request = "{\"lastWatered\":\"Not a date\"}";
        NewPlantRequest lastWateredRequest = new NewPlantRequest();
        lastWateredRequest.lastWatered = "Not a date";
        when(ctx.bodyAsClass(NewPlantRequest.class)).thenReturn(lastWateredRequest);
        when(ctx.body()).thenReturn(request);
        when(ctx.pathParam("id")).thenReturn(userId);
        when(ctx.pathParam("plantId")).thenReturn(plantId);
        Javalin.updatePlant(ctx);
        verify(ctx).status(400);

        // Delete user and plants for repeatable tests
        Helper.deleteUser(userId);
    }

    // Requirement: F.DP.7
    @Test
    public void updatePlantNicknameSuccess() {
        Context ctx = mock(Context.class);

        // Create user for the test
        Helper.createUser();
        // Get ID by logging in
        String userId = Helper.getUserIdForTest();
        // Add plant to a user and get plant ID
        String plantId = Helper.addPlantToUser(userId);

        // Update plant based on user and plant ID
        String request = "{\"nickname\":\"filip\"}";
        NewPlantRequest nickname = new NewPlantRequest();
        nickname.nickname = "filip";
        when(ctx.bodyAsClass(NewPlantRequest.class)).thenReturn(nickname);
        when(ctx.body()).thenReturn(request);
        when(ctx.pathParam("id")).thenReturn(userId);
        when(ctx.pathParam("plantId")).thenReturn(plantId);
        Javalin.updatePlant(ctx);
        verify(ctx).status(200);

        // Delete user and plants for repeatable tests
        Helper.deleteUser(userId);
    }

    // Requirement: F.DP.7
    @Test
    public void updatePlantImageUrlSuccess() {
        Context ctx = mock(Context.class);

        // Create user for the test
        Helper.createUser();
        // Get ID by logging in
        String userId = Helper.getUserIdForTest();
        // Add plant to a user and get plant ID
        String plantId = Helper.addPlantToUser(userId);

        // Update plant based on user and plant ID
        String request = "{\"image_url\":\"filip.se\"}";
        NewPlantRequest imageUrl = new NewPlantRequest();
        imageUrl.imageURL = "filip.se";
        when(ctx.bodyAsClass(NewPlantRequest.class)).thenReturn(imageUrl);
        when(ctx.body()).thenReturn(request);
        when(ctx.pathParam("id")).thenReturn(userId);
        when(ctx.pathParam("plantId")).thenReturn(plantId);
        Javalin.updatePlant(ctx);
        verify(ctx).status(200);

        // Delete user and plants for repeatable tests
        Helper.deleteUser(userId);
    }

    // Requirement: F.DP.2
    @Test
    public void addPlantToUserSuccess() {
        Context ctx = mock(Context.class);

        // Create user for the test
        Helper.createUser();
        // Get ID by logging in
        String userId = Helper.getUserIdForTest();

        // Add plant to a user
        NewPlantRequest plant = new NewPlantRequest(0, "test", "test",
                "test", "test", "1970-01-01",
                1, 1, "test", "test");
        when(ctx.bodyAsClass(NewPlantRequest.class)).thenReturn(plant);
        when(ctx.pathParam("id")).thenReturn(userId);
        Javalin.savePlant(ctx);
        verify(ctx).status(201);

        // Delete user and plants for repeatable tests
        Helper.deleteUser(userId);
    }

    // Requirement: F.DP.2
    @Test
    public void addPlantToUserWrongDateFormat() {
        Context ctx = mock(Context.class);

        // Create user for the test
        Helper.createUser();
        // Get ID by logging in
        String userId = Helper.getUserIdForTest();

        // Add plant to a user
        NewPlantRequest plant = new NewPlantRequest(0, "test", "test",
                "test", "test", "1999/02/22",
                1, 1, "test", "test");
        when(ctx.bodyAsClass(NewPlantRequest.class)).thenReturn(plant);
        when(ctx.pathParam("id")).thenReturn(userId);
        Javalin.savePlant(ctx);
        verify(ctx).status(400);

        // Delete user and plants for repeatable tests
        Helper.deleteUser(userId);
    }

    // Requirement: F.DP.2
    @Test
    public void addPlantToUserAlreadyExists() {
        Context ctx = mock(Context.class);

        // Create user for the test
        Helper.createUser();
        // Get ID by logging in
        String userId = Helper.getUserIdForTest();

        // Add plant to a user
        NewPlantRequest plant = new NewPlantRequest(0, "test", "test",
                "test", "test", "1970-01-01",
                1, 1, "test", "test");
        when(ctx.bodyAsClass(NewPlantRequest.class)).thenReturn(plant);
        when(ctx.pathParam("id")).thenReturn(userId);
        Javalin.savePlant(ctx);
        verify(ctx).status(201);

        // Add plant to a user again to trigger status 409
        NewPlantRequest plant2 = new NewPlantRequest(0, "test", "test",
                "test", "test", "1970-01-01",
                1, 1, "test", "test");
        when(ctx.bodyAsClass(NewPlantRequest.class)).thenReturn(plant2);
        when(ctx.pathParam("id")).thenReturn(userId);
        Javalin.savePlant(ctx);
        verify(ctx).status(409);

        // Delete user and plants for repeatable tests
        Helper.deleteUser(userId);
    }

    // Requirement: F.DP.1
    @Test
    public void getPlantsSuccess() {
        Context ctx = mock(Context.class);
        when(ctx.queryParam("plant")).thenReturn("monstera");
        Javalin.getPlants(ctx);
        verify(ctx).status(200);
    }

    // Requirement: F.DP.1
    @Test
    public void getPlantsFail() {
        Context ctx = mock(Context.class);
        when(ctx.queryParam("plant")).thenReturn("asdasdasdjasdlkjadlad");
        Javalin.getPlants(ctx);
        verify(ctx).status(404);
    }

    // Requirement: F.DP.5
    @Test
    public void uploadImageSuccess() {
        Context ctx = mock(Context.class);

        // Create user for the test
        Helper.createUser();
        // Get ID by logging in
        String userId = Helper.getUserIdForTest();

        // Upload profile picture
        File file = new File("src/test/java/utils/test.jpg");
        AtomicReference<String> filePath = new AtomicReference<>("");
        UploadedFile uploadedFile = mock(UploadedFile.class);

        try (FileInputStream fis = new FileInputStream(file)) {
            when(uploadedFile.content()).thenReturn(fis);
            when(uploadedFile.contentType()).thenReturn("image/jpeg");
            when(uploadedFile.filename()).thenReturn("test.jpg");
            when(uploadedFile.extension()).thenReturn(".jpg");
            when(uploadedFile.size()).thenReturn(file.getTotalSpace());

            when(ctx.pathParam("id")).thenReturn(userId);
            when(ctx.uploadedFile("file")).thenReturn(uploadedFile);

            // Get the filepath back from the response
            when(ctx.result(anyString())).thenAnswer(invocation -> {
                filePath.set(invocation.getArgument(0, String.class).replace("\"", ""));
                return null;
            });

            Javalin.uploadImage(ctx);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        verify(ctx).status(200);

        // Delete user and image for repeatable tests
        Helper.deleteUser(userId);
        File fileToBeDeleted = new File("src/main/resources/uploads/" + filePath);
        if (fileToBeDeleted.exists()) {
            fileToBeDeleted.delete();
        }
    }
}
