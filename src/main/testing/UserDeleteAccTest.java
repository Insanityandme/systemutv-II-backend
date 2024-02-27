import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.myhappyplants.javalin.Javalin;
import se.myhappyplants.javalin.user.NewDeleteRequest;

import io.javalin.http.Context;

import org.junit.jupiter.api.*;
import se.myhappyplants.javalin.utils.DbConnection;

import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.*;



public class UserDeleteAccTest {

    private final Context ctx = mock(Context.class);




    @BeforeEach
    public void setUp() throws SQLException {

    }


    @Test
    public void DELETE_userDeleteAcc_204_Success(){

        NewDeleteRequest del = new NewDeleteRequest();
        del.password="123"; //Might have to be changed depending on the actual user id you wanna test

        when(ctx.bodyAsClass(NewDeleteRequest.class)).thenReturn(del);
        doReturn("22").when(ctx).pathParam("id"); //Might have to be changed depending on the actual user id you wanna test



        Javalin.deleteUser(ctx);

        verify(ctx).status(204);

    }

    @Test
    public void DELETE_userDeleteAcc_400_Fail(){
        NewDeleteRequest del = new NewDeleteRequest();
        del.password="wrong_password";

        when(ctx.bodyAsClass(NewDeleteRequest.class)).thenReturn(del);
        doReturn("22").when(ctx).pathParam("id"); //Might have to be changed depending on the actual user id you wanna test

        Javalin.deleteUser(ctx);

        verify(ctx).status(400);

    }


}
