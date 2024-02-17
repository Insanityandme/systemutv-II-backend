package se.myhappyplants.javalin;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import org.mindrot.jbcrypt.BCrypt;

import se.myhappyplants.javalin.plants.Plant;
import se.myhappyplants.javalin.user.NewUserRequest;
import se.myhappyplants.javalin.utils.DbConnection;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
                        post(ctx -> {
                            ctx.result("You requested to login");
                        });
                    });
                    path("users", () -> {
                        post(Main::createUser);

                        // path("{userId}", () -> {
                        //     get(UserController::getOne);
                        //     patch(UserController::update);
                        //     delete(UserController::delete);
                        // });
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

        ctx.status(201);
    }
}