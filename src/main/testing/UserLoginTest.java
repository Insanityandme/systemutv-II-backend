import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.javalin.http.Context;
import static org.mockito.Mockito.*;

public class UserLoginTest {

    private Context ctx;

    @BeforeEach
    public void setUp(){
        ctx = mock(Context.class);
    }

    @Test
    public void login_Successful(){
        when(ctx.queryParam("email")).thenReturn("test@gmail.com");
        when(ctx.queryParam("password")).thenReturn("test");

        Javalin.login(ctx);

        verify(ctx).status(200);
        verify(ctx).result("Success");
    }

    @Test
    public void login_Faliure(){
        when(ctx.queryParam("email")).thenReturn("test@gmail.com");
        when(ctx.queryParam("password")).thenReturn("wrong");

        Javalin.login(ctx);

        verify(ctx).status(404);
        verify(ctx).result("Login failed");
    }
}