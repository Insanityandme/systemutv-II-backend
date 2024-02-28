package main.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.myhappyplants.javalin.Javalin;
import se.myhappyplants.javalin.login.NewLoginRequest;
import se.myhappyplants.javalin.user.NewDeleteRequest;
import se.myhappyplants.javalin.user.NewUserRequest;
import io.javalin.http.Context;
import static org.mockito.Mockito.*;

public class UserLoginTest {

    private final Context ctx = mock(Context.class);
    private final Context ctxSetUp = mock(Context.class);
    private String email;
    private String password;
    private String username;

    @BeforeEach
    public void setUp(){
        email = "test@example.com";
        username = "test12";
        password = "password";

        NewUserRequest testUser = new NewUserRequest(email, username, password);
        when(ctxSetUp.bodyAsClass(NewUserRequest.class)).thenReturn(testUser);
        Javalin.createUser(ctxSetUp);

        NewLoginRequest login = new NewLoginRequest(email, password);
        when(ctxSetUp.bodyAsClass(NewLoginRequest.class)).thenReturn(login);
    }

    @Test
    public void login_Successful(){
        Javalin.login(ctxSetUp);

        verify(ctxSetUp).status(200);
        verify(ctxSetUp, never()).status(401);
    }

    @Test
    public void login_Faliure(){
        NewLoginRequest login = new NewLoginRequest(email, "wrong_password");
        when(ctxSetUp.bodyAsClass(NewLoginRequest.class)).thenReturn(login);

        Javalin.login(ctxSetUp);

        verify(ctxSetUp).status(401);
        verify(ctxSetUp, never()).status(200);
    }
}