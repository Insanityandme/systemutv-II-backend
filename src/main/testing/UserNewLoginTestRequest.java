import se.myhappyplants.javalin.Javalin;
import se.myhappyplants.server.services.UserRepository;

import io.javalin.http.Context;

import org.junit.jupiter.api.*;

import static org.mockito.Mockito.*;

public class UserNewLoginTestRequest {
    //Föreslår att när vi byter till Javalin som backend att vi har en UserController klass som
    //hanterar alla requests som berör användare (register, login osv.).

    private Context ctx;
    private UserRepository userRepository;
    //private UserController userController;

    @BeforeEach
    public void setUp() {
        ctx = mock(Context.class);
        Javalin.createUser(ctx);
        // userRepository = mock(UserRepository.class);
        //userController = new UserController(userRepository);
    }

    @Test
    public void postLoginUserWithValidEmailAndPassword() {
        when(ctx.queryParam("email")).thenReturn("plantlover@gmail.com");
        when(ctx.queryParam("password")).thenReturn("test");

        when(userRepository.checkLogin("plantlover@gmail.com", "test")).thenReturn(true);

        //userController.loginUser(ctx);

        verify(userRepository).checkLogin("plantlover@gmail.com", "test");
    }

    @Test
    public void postLoginUserWithInvalidEmailAndPassword() {
        when(ctx.queryParam("email")).thenReturn("plantlovergmail.com");
        when(ctx.queryParam("password")).thenReturn("wrong");

        when(userRepository.checkLogin("plantlovergmail.com", "wrong")).thenReturn(false);

        //userController.loginUser(ctx);
        Javalin.login(ctx);

        verify(userRepository).checkLogin("plantlovergmail.com", "wrong");
    }

    @Test
    public void postLoginUserWithInvalidEmail() {
        when(ctx.queryParam("email")).thenReturn("plantlovergmail.com");
        when(ctx.queryParam("password")).thenReturn("correct");

        when(userRepository.checkLogin("plantlovergmail.com", "correct")).thenReturn(false);

        //userController.loginUser(ctx);

        verify(userRepository).checkLogin("plantlovergmail.com", "correct");
    }

    @Test
    public void postLoginUserWithInvalidPassword() {
        when(ctx.queryParam("email")).thenReturn("plantlover@gmail.com");
        when(ctx.queryParam("password")).thenReturn("wrong");

        when(userRepository.checkLogin("plantlover@gmail.com", "wrong")).thenReturn(false);

        //userController.loginUser(ctx);

        verify(userRepository).checkLogin("plantlover@gmail.com", "wrong");
    }

    @Test
    public void postLoginUserWithEmptyEmail() {
        when(ctx.queryParam("email")).thenReturn("");
        when(ctx.queryParam("password")).thenReturn("correct");

        when(userRepository.checkLogin("", "correct")).thenReturn(false);

        //userController.loginUser(ctx);

        verify(userRepository).checkLogin("", "correct");
    }

    @Test
    public void postLoginUserWithEmptyPassword() {
        when(ctx.queryParam("email")).thenReturn("plantlover@gmail.com");
        when(ctx.queryParam("password")).thenReturn("");

        when(userRepository.checkLogin("plantlover@gmail.com", "")).thenReturn(false);

        //userController.loginUser(ctx);

        verify(userRepository).checkLogin("plantlover@gmail.com", "");
    }
}

