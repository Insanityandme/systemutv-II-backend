import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.myhappyplants.javalin.Javalin;
import se.myhappyplants.javalin.login.NewLoginRequest;
import se.myhappyplants.javalin.user.NewUserRequest;
import io.javalin.http.Context;
import se.myhappyplants.javalin.utils.DbConnection;

import static org.mockito.Mockito.*;

/**
 * REQUIREMENT: F.DP.3
 */
 public class UserLoginTest {
    private final Context ctx = mock(Context.class);
    private String email;

    @BeforeEach
    public void setUp(){
        DbConnection.path = "myHappyPlantsDBTEST.db";

        email = "test@example.com";
        String username = "test12";
        String password = "password";

        NewUserRequest testUser = new NewUserRequest(email, username, password);
        when(ctx.bodyAsClass(NewUserRequest.class)).thenReturn(testUser);
        Javalin.createUser(ctx);

        NewLoginRequest login = new NewLoginRequest(email, password);
        when(ctx.bodyAsClass(NewLoginRequest.class)).thenReturn(login);
    }

    @Test
    public void login_Successful(){
        Javalin.login(ctx);

        verify(ctx).status(200);
        verify(ctx, never()).status(404);
    }

    @Test
    public void login_Faliure(){
        NewLoginRequest login = new NewLoginRequest(email, "wrong_password");
        when(ctx.bodyAsClass(NewLoginRequest.class)).thenReturn(login);

        Javalin.login(ctx);

        verify(ctx).status(404);
        verify(ctx, never()).status(200);
    }
}

