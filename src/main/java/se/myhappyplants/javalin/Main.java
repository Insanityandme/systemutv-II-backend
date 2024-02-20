package se.myhappyplants.javalin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.openapi.*;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import org.mindrot.jbcrypt.BCrypt;

import se.myhappyplants.javalin.login.NewLoginRequest;
import se.myhappyplants.javalin.plant.NewPlantRequest;
import se.myhappyplants.javalin.plant.TreflePlant;
import se.myhappyplants.javalin.user.NewUserRequest;
import se.myhappyplants.javalin.utils.DbConnection;
import se.myhappyplants.javalin.user.User;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
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
                    path("register", () -> {
                        post(Main::createUser);
                    });
                    path("users", () -> {
                        path("{id}", () -> {
                            delete(Main::deleteUser);
                            path("plants", () -> {
                                path("{plantId}", () -> {
                                    patch(Main::updatePlant);
                                });
                                post(Main::savePlant);
                            });
                        });
                    });
                    path("plants", () -> {
                        get(Main::getPlants);
                    });
                });
            });
        }).start(7002);
    }

    /**
     * PLANT API
     */
    // Requirement: F.DP.1
    @OpenApi(
            summary = "Get plants based on search parameter",
            operationId = "getPlants",
            path = "/v1/plants",
            methods = HttpMethod.GET,
            tags = {"Plants"},
            queryParams = {@OpenApiParam(name = "plant", description = "The plant name")},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = TreflePlant[].class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public static void getPlants(Context ctx) {
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

        HttpResponse<String> result;
        try {
            result = response.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        ctx.result(result.body());
    }

    // Requirement: F.DP.2
    @OpenApi(
            summary = "Add plant to user",
            operationId = "savePlant",
            path = "/v1/users/{id}/plants",
            methods = HttpMethod.POST,
            tags = {"User"},
            pathParams = {@OpenApiParam(name = "id", type = Integer.class, description = "The user ID")},
            requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = NewPlantRequest.class)}),
            responses = {
                    @OpenApiResponse(status = "201"),
                    @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public static void savePlant(Context ctx) {
        int userId = ctx.pathParamAsClass("id", Integer.class).check(id -> id > 0, "ID must be greater than 0").get();
        NewPlantRequest plant = ctx.bodyAsClass(NewPlantRequest.class);
        boolean isCreated = false;
        Connection database = getConnection();

        String sqlSafeNickname = plant.nickname.replace("'", "''");
        String plantQuery = "INSERT INTO plant (user_id, nickname, plant_id, last_watered, image_url) VALUES (?, ?, ?, ?, ?);";
        String detailsQuery = "INSERT INTO plantdetails (scientific_name, genus, family, common_name, image_url, light, url_wikipedia_en, water_frequency, plant_id)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (PreparedStatement plantStatement = database.prepareStatement(plantQuery)) {
            plantStatement.setInt(1, userId);
            plantStatement.setString(2, sqlSafeNickname);
            plantStatement.setString(3, plant.id);
            plantStatement.setDate(4, Date.valueOf(plant.lastWatered));
            plantStatement.setString(5, plant.imageURL);
            plantStatement.executeUpdate();

            // Checks if plant details already exist in the database
            // We might want to have several of the same plants in our dashboard
            // But no duplicate details about them!
            String query = "SELECT COUNT(*) FROM plantdetails WHERE plant_id = ?";
            boolean doesPlantDetailExist = false;
            try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
                preparedStatement.setString(1, plant.id);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    doesPlantDetailExist = count > 0;
                }
            }

            if (!doesPlantDetailExist) {
                try (PreparedStatement detailsStatement = database.prepareStatement(detailsQuery)) {
                    detailsStatement.setString(1, plant.scientificName);
                    detailsStatement.setString(2, plant.genus);
                    detailsStatement.setString(3, plant.family);
                    detailsStatement.setString(4, plant.commonName);
                    detailsStatement.setString(5, plant.imageURL);
                    detailsStatement.setString(6, "0");
                    detailsStatement.setString(7, "0");
                    detailsStatement.setString(8, "0");
                    detailsStatement.setString(9, plant.id);
                    detailsStatement.executeUpdate();

                    isCreated = true;
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        if (isCreated) {
            String json = objecToJson(plant);
            ctx.result(json);
            ctx.status(200);
        } else {
            ctx.status(404);
            ctx.result("You already have a plant with that nickname");
        }
    }

    // Requirement: needs a new requirement
    @OpenApi(
            summary = "Update plant by ID",
            operationId = "updatePlantById",
            path = "/v1/users/{id}/plants/{plantId}",
            methods = HttpMethod.PATCH,
            pathParams = {
                    @OpenApiParam(name = "id", type = Integer.class, description = "The user ID"),
                    @OpenApiParam(name = "plantId", type = Integer.class, description = "The plant ID"),
            },
            tags = {"User"},
            requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = NewPlantRequest.class)}),
            responses = {
                    @OpenApiResponse(status = "200"),
                    @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public static void updatePlant(Context ctx) {
        int userId = ctx.pathParamAsClass("id", Integer.class).check(id -> id > 0, "ID must be greater than 0").get();
        int plantId = ctx.pathParamAsClass("plantId", Integer.class).check(id -> id > 0, "ID must be greater than 0").get();

        Connection database = getConnection();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(ctx.body());

            if (jsonNode.get("nickname") != null) {
                String nickname = jsonNode.get("nickname").asText();
                String sqlSafeNewNickname = nickname.replace("'", "''");
                String query = "UPDATE plant SET nickname = ? WHERE user_id = ? AND id = ?;";
                try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
                    preparedStatement.setString(1, sqlSafeNewNickname);
                    preparedStatement.setInt(2, userId);
                    preparedStatement.setInt(3, plantId);
                    preparedStatement.executeUpdate();

                    String json = String.format("{\"nickname\": \"%s\"}", nickname);
                    ctx.result(json);
                    ctx.status(200);
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }
            } else if (jsonNode.get("lastWatered") != null) {
                String lastWatered = jsonNode.get("lastWatered").asText();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // TODO send back the updated plant data to the client
    }

    /**
     * USER API
     */
    // Requirement: F.DP.4
    @OpenApi(
            summary = "Create user",
            operationId = "createUser",
            path = "/v1/register",
            methods = HttpMethod.POST,
            tags = {"Authentication"},
            requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = NewUserRequest.class)}),
            responses = {
                    @OpenApiResponse(status = "201"),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public static void createUser(Context ctx) {
        NewUserRequest user = ctx.bodyAsClass(NewUserRequest.class);
        Connection database = getConnection();

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
            tags = {"Authentication"},
            requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = NewLoginRequest.class)}),
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = User.class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public static void login(Context ctx) {
        NewLoginRequest newLoginRequest = ctx.bodyAsClass(NewLoginRequest.class);
        Connection database = getConnection();

        boolean isVerified = false;
        User user = null;
        int id = 0;
        String username = null;
        boolean notificationActivated = false;
        boolean funFactsActivated = false;

        String query = "SELECT id, username, password, notification_activated, fun_facts_activated FROM user WHERE email = ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setString(1, newLoginRequest.email);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String hashedPassword = resultSet.getString("password");
                isVerified = BCrypt.checkpw(newLoginRequest.password, hashedPassword);

                id = resultSet.getInt("id");
                username = resultSet.getString("username");
                notificationActivated = resultSet.getBoolean("notification_activated");
                funFactsActivated = resultSet.getBoolean("fun_facts_activated");
            }
            user = new User(id, newLoginRequest.email, username, notificationActivated, funFactsActivated);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        if (isVerified) {
            String json = objecToJson(user);
            ctx.result(json);
            ctx.status(200);
        } else {
            ctx.status(404);
            ctx.result("Login failed");
        }
    }

    // Requirement: F.DP.6
    @OpenApi(
            summary = "Delete user by ID",
            operationId = "deleteUserByID",
            path = "/v1/users/{id}",
            methods = HttpMethod.DELETE,
            pathParams = {@OpenApiParam(name = "id", type = Integer.class, description = "The user ID")},
            tags = {"User"},
            responses = {
                    @OpenApiResponse(status = "204"),
                    @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public static void deleteUser(Context ctx) {
        int userId = ctx.pathParamAsClass("id", Integer.class).check(id -> id > 0, "ID must be greater than 0").get();

        Connection database = getConnection();

        String queryDeletePlants = "DELETE FROM Plant WHERE user_id = ?;";
        String queryDeleteUser = "DELETE FROM User WHERE id = ?;";

        try (PreparedStatement preparedStatementDeletePlants = database.prepareStatement(queryDeletePlants);
             PreparedStatement preparedStatementDeleteUser = database.prepareStatement(queryDeleteUser)) {
            preparedStatementDeletePlants.setInt(1, userId);
            preparedStatementDeleteUser.setInt(1, userId);

            database.setAutoCommit(false);
            int deleteUser = preparedStatementDeleteUser.executeUpdate();
            if (deleteUser == 0) {
                throw new NotFoundResponse("User not found");
            } else {
                preparedStatementDeletePlants.executeUpdate();
                database.commit();
            }
        } catch (SQLException sqlException) {
            try {
                database.rollback();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        ctx.status(204);
    }

    /**
     * HELPER METHODS
     */
    public static Connection getConnection() {
        Connection database;

        try {
            database = DbConnection.getInstance().getConnection();
        } catch (SQLException e) {
            throw new InternalServerErrorResponse("Unable to establish connection to database");
        }
        return database;
    }

    public static String objecToJson(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        String json;

        try {
            json = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return json;
    }

}