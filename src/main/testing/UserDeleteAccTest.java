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

    @BeforeEach
    public void setUp() throws SQLException {
        email = generateRandomString(8) + "@example.com";
        username = generateRandomString(10);
        password = generateRandomString(12);
        NewUserRequest testUser = new NewUserRequest();

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
        del.password = "123"; //Might have to be changed depending on the actual user id you wanna test

        when(ctx.bodyAsClass(NewDeleteRequest.class)).thenReturn(del);
        doReturn("1").when(ctx).pathParam("id"); //Might have to be changed depending on the actual user id you wanna test

        Javalin.deleteUser(ctx);

        verify(ctx).status(204);
    }

    @Test
    public void DELETE_userDeleteAcc_400_Fail() {
        NewDeleteRequest del = new NewDeleteRequest();
        del.password = "wrong_password";

        when(ctx.bodyAsClass(NewDeleteRequest.class)).thenReturn(del);
        doReturn("2").when(ctx).pathParam("id"); //Might have to be changed depending on the actual user id you wanna test

        Javalin.deleteUser(ctx);

        verify(ctx).status(400);
    }
}
