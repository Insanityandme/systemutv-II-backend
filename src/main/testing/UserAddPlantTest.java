import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.myhappyplants.javalin.Javalin;
import se.myhappyplants.javalin.plant.NewPlantRequest;
import se.myhappyplants.javalin.user.NewUserRequest;
import se.myhappyplants.javalin.utils.DbConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class UserAddPlantTest {
    private final Context ctx = mock(Context.class);
    private JsonNode jsonNode;
    private int id;
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

        when(ctx.queryParam("plant")).thenReturn("sunflower");
        Javalin.getPlants(ctx);

        verify(ctx).status(200);
        verify(ctx).result(argThat((String result) -> {

            ObjectMapper objectMapper = new ObjectMapper();
            try {
                jsonNode = objectMapper.readTree(result);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            JsonNode dataNode = jsonNode.get("data");
            if (dataNode.isArray() && !dataNode.isEmpty()) {
                JsonNode firstItem = dataNode.get(0);
                id = firstItem.get("id").asInt();
                commonName = firstItem.get("common_name").asText();
                scientificName = firstItem.get("scientific_name").asText();
                imageURL = firstItem.get("image_url").asText();
                waterFrequency = 0L;
                lastWatered = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                light = 0;
                genus = firstItem.get("genus").asText();
                family = firstItem.get("family").asText();
            }

            return true;
        }));
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
    public void POST_userAddPlant_201()  {
        nickname = UUID.randomUUID().toString();
        NewPlantRequest plant = new NewPlantRequest(id, commonName, scientificName, imageURL,nickname,lastWatered, waterFrequency, light, genus, family);
        when(ctx.bodyAsClass(NewPlantRequest.class)).thenReturn(plant);
        doReturn(getUserId.toString()).when(ctx).pathParam("id");
        Javalin.savePlant(ctx);
        verify(ctx).status(201);
    }

    @Test
    public void POST_userAddPlant_400()  {
        nickname = "test"; //Change this to another plant's nickname that already exists in the db

        NewPlantRequest plant = new NewPlantRequest(id, commonName, scientificName, imageURL,nickname,lastWatered, waterFrequency, light, genus, family);

        when(ctx.bodyAsClass(NewPlantRequest.class)).thenReturn(plant);

        doReturn(getUserId.toString()).when(ctx).pathParam("id");

        Javalin.savePlant(ctx);

        verify(ctx).status(400);
    }
}
