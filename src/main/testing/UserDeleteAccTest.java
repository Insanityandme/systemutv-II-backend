import org.junit.jupiter.api.Test;
import se.myhappyplants.javalin.Javalin;
import se.myhappyplants.javalin.user.NewDeleteRequest;

import io.javalin.http.Context;

import org.junit.jupiter.api.*;
import se.myhappyplants.javalin.utils.DbConnection;

import java.sql.Connection;

import static org.mockito.Mockito.*;


public class UserDeleteAccTest {

    private final Context ctx = mock(Context.class);

    @BeforeEach
    public void setUp() {
        Connection mockConnection = mock(Connection.class);
        DbConnection dbConnection = mock(DbConnection.class);
        when(dbConnection.getConnection()).thenReturn(mockConnection);
        dbConnection.setPath("");

    }


    @Test
    public void DELETE_userDeleteAcc_204_Success(){

        NewDeleteRequest del = new NewDeleteRequest();
        del.password="123";

        when(ctx.bodyAsClass(NewDeleteRequest.class)).thenReturn(del);
        doReturn("16").when(ctx).pathParam("id");

        Javalin.deleteUser(ctx);

        verify(ctx).status(204);

    }

    @Test
    public void DELETE_userDeleteAcc_400_Fail(){
        NewDeleteRequest del = new NewDeleteRequest();
        del.password="wrong_password";

        when(ctx.bodyAsClass(NewDeleteRequest.class)).thenReturn(del);
        doReturn("1").when(ctx).pathParam("id");

        Javalin.deleteUser(ctx);

        verify(ctx).status(400);

    }


}
