import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.myhappyplants.javalin.Javalin;
import se.myhappyplants.javalin.plant.Fact;
import se.myhappyplants.javalin.user.NewUserRequest;
import se.myhappyplants.javalin.utils.DbConnection;

import java.sql.SQLException;

import static org.mockito.Mockito.*;

public class JavalinTests {
    @BeforeEach
    public void setup() {
        try {
            DbConnection.getInstance().setPath("myHappyPlantsDBTEST.db");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Requirement: F.DP.13
    @Test
    public void getFactSuccess() {
        Context ctx = mock(Context.class);
        Fact fact = new Fact();

        when(ctx.pathParam("factId")).thenReturn("1");
        when(ctx.bodyAsClass(Fact.class)).thenReturn(fact);

        Javalin.getFact(ctx);

        verify(ctx).status(200);
    }

    // Requirement: F.DP.13
    @Test
    public void getFactNotFound() {
        Context ctx = mock(Context.class);
        Fact fact = new Fact();

        when(ctx.pathParam("factId")).thenReturn("5000");
        when(ctx.bodyAsClass(Fact.class)).thenReturn(fact);

        Javalin.getFact(ctx);

        verify(ctx).status(404);
    }


    // Requirement: F.DP.4
    @Test
    public void createUserSuccess() {
        Context ctx = mock(Context.class);
        NewUserRequest user = new NewUserRequest();

        when(ctx.bodyAsClass(NewUserRequest.class)).thenReturn(user);

    }


    // Requirement: F.DP.4
    @Test
    public void createUserFailed() {

    }
}
