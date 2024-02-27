import org.junit.jupiter.api.Test;
import se.myhappyplants.javalin.Javalin;
import se.myhappyplants.javalin.user.NewDeleteRequest;

import io.javalin.http.Context;

import org.junit.jupiter.api.*;
import se.myhappyplants.javalin.user.NewUserRequest;

import java.sql.SQLException;
import java.util.Random;

import static org.mockito.Mockito.*;

public class UserDeleteAccTest {
    private final Context ctx = mock(Context.class);
    private String email;
    private String username;
    private String password;

    private Long getUserId;

    @BeforeEach
    public void setUp() throws SQLException, NoSuchFieldException, IllegalAccessException {
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

        NewDeleteRequest del = new NewDeleteRequest();
        del.password = password;

        when(ctx.bodyAsClass(NewDeleteRequest.class)).thenReturn(del);
        doReturn(getUserId.toString()).when(ctx).pathParam("id");

        Javalin.deleteUser(ctx);

        verify(ctx).status(204);
    }

    @Test
    public void DELETE_userDeleteAcc_400_Fail() {
        NewDeleteRequest del = new NewDeleteRequest();
        del.password = "wrong_password";

        when(ctx.bodyAsClass(NewDeleteRequest.class)).thenReturn(del);
        doReturn(getUserId.toString()).when(ctx).pathParam("id");

        Javalin.deleteUser(ctx);

        verify(ctx).status(400);
    }
}
