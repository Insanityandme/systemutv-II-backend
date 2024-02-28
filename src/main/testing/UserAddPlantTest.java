import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import se.myhappyplants.javalin.Javalin;
import se.myhappyplants.javalin.login.NewLoginRequest;
import se.myhappyplants.javalin.plant.NewPlantRequest;
import se.myhappyplants.javalin.user.NewUserRequest;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * REQUIREMENT: F.A.5
 */
public class UserAddPlantTest {
    private final Context ctx = mock(Context.class);
    private final Context ctxSetUp = mock(Context.class);
    private final Context ctxSetUp1 = mock(Context.class);
    private int plantId;
    private String commonName;
    private String scientificName;
    private String imageURL;
    private String nickname;
    private String lastWatered;
    private long waterFrequency;
    private int light;
    private String genus;
    private String family;
    private String email;
    private String username;
    private String password;

    private String getUserId;

    private boolean verifyResult(String result) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(result);

            JsonNode dataNode = jsonNode.get("data");
            if (dataNode.isArray() && !dataNode.isEmpty()) {
                JsonNode firstItem = dataNode.get(0);
                plantId = firstItem.get("id").asInt();
                commonName = firstItem.get("common_name").asText();
                scientificName = firstItem.get("scientific_name").asText();
                imageURL = firstItem.get("image_url").asText();
                waterFrequency = 0L;
                lastWatered = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                light = 0;
                genus = firstItem.get("genus").asText();
                family = firstItem.get("family").asText();
                nickname = UUID.randomUUID().toString();
            }

            return true;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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

    @BeforeEach
    public void setUp() throws SQLException, IllegalAccessException, NoSuchFieldException, JsonProcessingException {
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

        ObjectMapper objectMapper = new ObjectMapper();
        String capturedResultString = capturedResult.toString();
        JsonNode jsonNode = objectMapper.readTree(capturedResultString);
        getUserId = String.valueOf(jsonNode.get("id"));

        when(ctxSetUp1.queryParam("plant")).thenReturn("sunflower");
        Javalin.getPlants(ctxSetUp1);

        verify(ctxSetUp).status(201);
        verify(ctxSetUp).status(200);
        verify(ctxSetUp1).status(200);
        verify(ctxSetUp1).result(argThat(this::verifyResult));
    }

    // Requirement: F.A.5
    @Test
    public void POST_userAddPlant_201_Success()  {
        NewPlantRequest plant = new NewPlantRequest(plantId, commonName, scientificName, imageURL,nickname,lastWatered, waterFrequency, light, genus, family);
        when(ctx.bodyAsClass(NewPlantRequest.class)).thenReturn(plant);
        doReturn(getUserId).when(ctx).pathParam("id");
        Javalin.savePlant(ctx);
        verify(ctx).status(201);
    }
}
