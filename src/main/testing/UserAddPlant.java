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
import se.myhappyplants.javalin.utils.DbConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.*;

public class UserAddPlant {

    private final Context ctx1 = mock(Context.class);
    private final Context ctx2 = mock(Context.class);


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

    @Mock
    private Connection mockConnection;

    @InjectMocks
    private DbConnection dbConnection;


    @BeforeEach
    public void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        DbConnection spyDbConnection = spy(dbConnection);
        when(spyDbConnection.getConnection()).thenReturn(mockConnection);
        spyDbConnection.setPath("myHappyPlantsDBTEST.db");
        when(ctx1.queryParam("plant")).thenReturn("sunflower");
        Javalin.getPlants(ctx1);

        verify(ctx1).status(200);


        verify(ctx1).result(argThat((String result) -> {

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
                nickname = "";
                lastWatered = "1970-01-01";
                waterFrequency = 0L;
                light = 0;
                genus = firstItem.get("genus").asText();
                family = firstItem.get("family").asText();

            }

            return true;
        }));



    }

    @Test
    public void POST_userAddPlant_201()  {
        NewPlantRequest plant = new NewPlantRequest(id, commonName, scientificName, imageURL,nickname,lastWatered, waterFrequency, light, genus, family);
        when(ctx2.bodyAsClass(NewPlantRequest.class)).thenReturn(plant);
        doReturn("23").when(ctx2).pathParam("id");
        Javalin.savePlant(ctx2);
        verify(ctx2).status(201);
    }
}
