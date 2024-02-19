package se.myhappyplants.javalin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import org.mindrot.jbcrypt.BCrypt;

import se.myhappyplants.javalin.login.NewLoginRequest;
import se.myhappyplants.javalin.plants.Plant;
import se.myhappyplants.javalin.user.NewUserRequest;
import se.myhappyplants.javalin.utils.DbConnection;
import se.myhappyplants.javalin.user.User;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Main {
    public static void main(String[] args) {
        Javalin.create(config -> {
            // Plugin for documentation and testing our api
            config.registerPlugin(new OpenApiPlugin(pluginConfig -> {
                pluginConfig.withDefinitionConfiguration((version, definition) -> {
                    definition.withOpenApiInfo(info -> info.setTitle("Happy Plants OpenAPI"));
                });
            }));
            config.registerPlugin(new SwaggerPlugin());
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(CorsPluginConfig.CorsRule::anyHost);
            });

            // Routing
            config.router.apiBuilder(() -> {
                // Users api routes
                path("", () ->
                        // redirect me to the swagger page
                        get(ctx -> ctx.redirect("/swagger"))
                );
                path("v1/", () -> {
                    path("login", () -> {
                        post(Main::login);
                    });
                    path("users", () -> {
                        post(Main::createUser);
                        path("{userid}", () -> {
                            delete(Main::deleteUser);
                        });
                        delete(Main::deleteUser);
                    });
                    path("plants", () -> {
                        get(ctx -> {
                            ctx.result("You requested all plants");
                        });
                        path("search", () -> {
                            get(Main::getPlants);
                        });
                    });
                });
            });
        }).start(7002);
    }

    // Requirement: F.DP.1
    @OpenApi(
            summary = "Get plants based on search parameter",
            operationId = "getPlants",
            path = "/v1/plants/search",
            methods = HttpMethod.GET,
            tags = {"Plants"},
            queryParams = {@OpenApiParam(name = "plant", description = "The plant name")},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Plant[].class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public static void getPlants(Context ctx) throws ExecutionException, InterruptedException {
        String TrefleKey = System.getenv("TREFLE_API_KEY");

        // for example when you want to search for a plant called "rose"
        // you type in the url: http://localhost:7002/v1/plants/search?plant=rose
        String plant = ctx.queryParam("plant");

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder(
                        URI.create("https://trefle.io/api/v1/plants/search?token=" + TrefleKey + "&q=" + plant))
                .header("accept", "application/json")
                .build();

        CompletableFuture<HttpResponse<String>> response =
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        HttpResponse<String> result = response.get();

        ctx.result(result.body());
    }

    // Requirement: F.DP.4
    @OpenApi(
            summary = "Create user",
            operationId = "createUser",
            path = "/v1/users",
            methods = HttpMethod.POST,
            tags = {"User"},
            requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = NewUserRequest.class)}),
            responses = {
                    @OpenApiResponse(status = "201"),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public static void createUser(Context ctx) {
        NewUserRequest user = ctx.bodyAsClass(NewUserRequest.class);
        Connection database;

        try {
            database = DbConnection.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt());
        String sqlSafeUsername = user.username.replace("'", "''");
        String query = "INSERT INTO user (username, email, password, notification_activated, fun_facts_activated) VALUES (?, ?, ?, 1, 1);";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, sqlSafeUsername);
            preparedStatement.setString(2, user.email);
            preparedStatement.setString(3, hashedPassword);

            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        // TODO add error handling
        ctx.status(201);
    }

    // Requirement: F.DP.3
    @OpenApi(
            summary = "Login",
            operationId = "login",
            path = "/v1/login",
            methods = HttpMethod.POST,
            tags = {"Login"},
            requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = NewLoginRequest.class)}),
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = User.class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public static void login(Context ctx) {
        NewLoginRequest login = ctx.bodyAsClass(NewLoginRequest.class);

        Connection database;

        try {
            database = DbConnection.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        boolean isVerified = false;
        User user = null;
        int id = 0;
        String username = null;
        boolean notificationActivated = false;
        boolean funFactsActivated = false;

        String query = "SELECT id, username, password, notification_activated, fun_facts_activated FROM user WHERE email = ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, login.email);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String hashedPassword = resultSet.getString("password");
                isVerified = BCrypt.checkpw(login.password, hashedPassword);

                id = resultSet.getInt("id");
                username = resultSet.getString("username");
                notificationActivated = resultSet.getBoolean("notification_activated");
                funFactsActivated = resultSet.getBoolean("fun_facts_activated");
            }
            user = new User(id, login.email, username, notificationActivated, funFactsActivated);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        if (isVerified) {
            ObjectMapper mapper = new ObjectMapper();
            String json;

            try {
                json = mapper.writeValueAsString(user);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            ctx.result(json);
            ctx.status(200);
        } else {
            ctx.status(404);
            ctx.result("Login failed");
        }
    }

    // Requirement: Need a requirement
    @OpenApi(
            summary = "Delete user by ID",
            operationId = "deleteUserByID",
            path = "/v1/users/:userid",
            methods = HttpMethod.DELETE,
            pathParams = {@OpenApiParam(name = "userId", type = Integer.class, description = "The user ID")},
            tags = {"User"},
            responses = {
                    @OpenApiResponse(status = "204"),
                    @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public static void deleteUser(Context ctx) throws SQLException {
        boolean accountDeleted = false;
        Connection database;
        database = DbConnection.getInstance().getConnection();

        String querySelect = "SELECT id FROM user WHERE email = ?;";

        try (PreparedStatement preparedStatementSelect = database.prepareStatement(querySelect)) {
            // preparedStatementSelect.setString(1, email);

            ResultSet resultSet = preparedStatementSelect.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String queryDeletePlants = "DELETE FROM Plant WHERE user_id = ?;";
                String queryDeleteUser = "DELETE FROM User WHERE id = ?;";

                try (PreparedStatement preparedStatementDeletePlants = database.prepareStatement(queryDeletePlants);
                     PreparedStatement preparedStatementDeleteUser = database.prepareStatement(queryDeleteUser)) {

                    preparedStatementDeletePlants.setInt(1, id);
                    preparedStatementDeleteUser.setInt(1, id);

                    preparedStatementDeletePlants.executeUpdate();
                    preparedStatementDeleteUser.executeUpdate();

                    accountDeleted = true;
                } catch (SQLException sqlException) {
                }
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    // TODO: when creating a new plant make sure you send all data back to the client
}