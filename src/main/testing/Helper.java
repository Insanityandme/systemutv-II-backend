import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import se.myhappyplants.javalin.Javalin;
import se.myhappyplants.javalin.login.NewLoginRequest;

import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Helper {
    public static String getUserIdForTest(Context ctx) {
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
}
