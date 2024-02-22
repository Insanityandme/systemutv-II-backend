import se.myhappyplants.server.services.UserRepository;

import io.javalin.http.Context;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserRegisterTest {
    //Föreslår att när vi byter till Javalin som backend att vi har en UserController klass som
    //hanterar alla requests som berör användare (register, login osv.).

    private Context ctx;
    private UserRepository userRepository;
    //private UserController userController;

    @BeforeEach
    public void setUp() {
        ctx = mock(Context.class);
        userRepository = mock(UserRepository.class);
        //userController = new UserController(userRepository);
    }

    @Test
    public void postRegisterUserWithValidUsername() {
        when(ctx.queryParam("email")).thenReturn("plantlover@gmail.com");
        when(ctx.queryParam("username")).thenReturn("PlantLover");
        when(ctx.queryParam("password")).thenReturn("test");

        //Detta simulerar att vi skickar context till en metod i userController som skapar en användare genom
        //POST request + att man där sparar användaren i databasen om allt gick bra
        //userController.createUser(ctx);

        //Kollar att saveUser i userRepository anropas med en User som har rätt username
        verify(userRepository).registerUser(argThat(user -> user.getUsername().equals("PlantLover")));
    }

    @Test
    public void postRegisterUserWithEmptyNameField() {
        when(ctx.queryParam("email")).thenReturn("plantlover@gmail.com");
        when(ctx.queryParam("username")).thenReturn("");
        when(ctx.queryParam("password")).thenReturn("test");

        //userController.register(ctx);

        //Beroende på hur vi vill kolla om användaren är tom så kan vi antingen kolla så att saveUser anropas med tomt
        //användarnamn och returnar false eller så kan vi kolla så att status sätts till 400
        //verify(userRepository).saveUser(argThat(user -> user.getUsername().equals("")));
        verify(ctx).status(400);
    }

    @Test
    public void postRegisterUserWithValidEmail() {
        when(ctx.queryParam("email")).thenReturn("plantlover@gmail.com");
        when(ctx.queryParam("username")).thenReturn("PlantLover");
        when(ctx.queryParam("password")).thenReturn("test");

        //Detta simulerar att vi skickar context till en metod i userController som skapar en användare genom
        //POST request + att man där sparar användaren i databasen om allt gick bra
        //userController.createUser(ctx);

        //Kollar att saveUser i userRepository anropas med en User som har rätt email
        verify(userRepository).registerUser(argThat(user -> user.getEmail().equals("plantlover@gmail.com")));
    }

    @Test
    public void postRegisterUserWithInvalidEmail() {
        when(ctx.queryParam("email")).thenReturn("invalidemail");
        when(ctx.queryParam("username")).thenReturn("PlantLover");
        when(ctx.queryParam("password")).thenReturn("test");

        when(userRepository.registerUser(argThat(user -> user.getEmail().equals("invalidemail")))).thenReturn(false);

        //userController.register(ctx);

        verify(userRepository).registerUser(argThat(user -> user.getEmail().equals("invalidemail")));
    }

    @Test
    public void postRegisterUserWithExistingEmail() {
        when(ctx.queryParam("email")).thenReturn("existinguser@gmail.com");
        when(ctx.queryParam("username")).thenReturn("user");
        when(ctx.queryParam("password")).thenReturn("test");

        //Alternativt får vi skapa en metod i userRepository som specifikt kollar om email-adressen är upptagen istället
        //för att använda saveUser
        when(userRepository.registerUser(argThat(user -> user.getEmail().equals("existinguser@gmail.com")))).thenReturn(false);

        //userController.register(ctx);

        //Till för att kolla så att saveUser verkligen anropades med dessa värden under testet
        verify(userRepository).registerUser(argThat(user ->
                user.getUsername().equals("user") &&
                        user.getEmail().equals("nonexistinguser@gmail.com") &&
                        user.getPassword().equals("test")
        ));

        // Kollar så att userRepository.saveUser-metoden med emailen "existinguser@gmail.com" returnerar false
        assertFalse(userRepository.registerUser(argThat(user -> user.getEmail().equals("existinguser@gmail.com"))));
    }

    @Test
    public void postRegisterUserWithEmptyEmailField() {
        when(ctx.queryParam("email")).thenReturn("");
        when(ctx.queryParam("username")).thenReturn("plantlover");
        when(ctx.queryParam("password")).thenReturn("test");

        //userController.register(ctx);

        //Beroende på hur vi vill kolla om användaren är tom så kan vi antingen kolla så att saveUser anropas med tomt
        //email eller så kan vi kolla så att status sätts till 400
        //verify(userRepository).saveUser(argThat(user -> user.getEmail().equals("")));
        verify(ctx).status(400);
    }

    @Test
    public void post201ResponseAfterSuccessfulRegistration() {
        when(ctx.queryParam("email")).thenReturn("plantlover@gmail.com");
        when(ctx.queryParam("username")).thenReturn("PlantLover");
        when(ctx.queryParam("username")).thenReturn("test");

        //when(userController.createUser(ctx)).thenReturn(201);

        //userController.createUser(ctx);

        verify(ctx).status(201);
    }

    @Test
    public void post400ResponseAfterInvalidRegistration() {
        when(ctx.queryParam("email")).thenReturn("plantlover@gmail.com");
        when(ctx.queryParam("username")).thenReturn("PlantLover");
        when(ctx.queryParam("username")).thenReturn("test");

        //when(userController.createUser(ctx)).thenReturn(400);

        //userController.createUser(ctx);

        verify(ctx).status(400);
    }
}
