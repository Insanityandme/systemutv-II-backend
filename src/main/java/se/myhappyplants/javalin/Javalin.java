package se.myhappyplants.javalin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.openapi.*;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.javalin.plugin.bundled.CorsPluginConfig;

import org.mindrot.jbcrypt.BCrypt;
import org.sqlite.SQLiteException;
import se.myhappyplants.javalin.login.NewLoginRequest;
import se.myhappyplants.javalin.plant.Fact;
import se.myhappyplants.javalin.plant.NewPlantRequest;
import se.myhappyplants.javalin.plant.Plant;
import se.myhappyplants.javalin.utils.TreflePlantSwaggerObject;
import se.myhappyplants.javalin.user.NewDeleteRequest;
import se.myhappyplants.javalin.user.NewUserRequest;
import se.myhappyplants.javalin.user.NewUpdateUserRequest;
import se.myhappyplants.javalin.user.User;
import se.myhappyplants.javalin.utils.ErrorResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.javalin.apibuilder.ApiBuilder.*;
import static se.myhappyplants.javalin.utils.Helper.*;

public class Javalin {
    public static void main(String[] args) {
        io.javalin.Javalin.create(config -> {
            // Plugin for documentation and testing our api
            config.registerPlugin(new OpenApiPlugin(pluginConfig -> {
                pluginConfig.withDefinitionConfiguration((version, definition) -> {
                    definition.withOpenApiInfo(info -> info.setTitle("Happy Plants API"));
                });
            }));
            config.registerPlugin(new SwaggerPlugin());
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(CorsPluginConfig.CorsRule::anyHost);
            });

            // Routing
            config.router.apiBuilder(() -> {
                path("", () ->
                        // redirect to documentation page on "/"
                        get(ctx -> ctx.redirect("/swagger"))
                );
                path("v1/", () -> {
                    path("login", () -> {
                        post(Javalin::login);
                    });
                    path("register", () -> {
                        post(Javalin::createUser);
                    });
                    path("facts", () -> {
                        path("{factId}", () -> {
                            get(Javalin::getFact);
                        });
                    });
                    path("users", () -> {
                        path("{id}", () -> {
                            patch(Javalin::updateUser);
                            delete(Javalin::deleteUser);
                            path("plants", () -> {
                                get(Javalin::getAllPlants);
                                patch(Javalin::updateAllPlants);
                                path("{plantId}", () -> {
                                    get(Javalin::getPlant);
                                    patch(Javalin::updatePlant);
                                    delete(Javalin::deletePlant);
                                });
                                post(Javalin::savePlant);
                            });
                        });
                    });
                    path("plants", () -> {
                        get(Javalin::getPlants);
                    });
                });
            });
        }).start(7002);
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
                    @OpenApiResponse(status = "409", content = {@OpenApiContent(from = ErrorResponse.class)}),
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
            try {
                preparedStatement.executeUpdate();
                ctx.status(201);
            } catch (SQLiteException exception) {
                ctx.status(409);
                ctx.result("User already exists");
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    // Requirement: F.DP.13
    @OpenApi(
            summary = "Get fact based on ID",
            operationId = "getFact",
            path = "/v1/facts/{factId}",
            pathParams = {@OpenApiParam(name = "factId", type = Integer.class, description = "The fact ID")},
            methods = HttpMethod.GET,
            tags = {"Fun Facts"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Fact[].class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public static void getFact(Context ctx) {
        int factId = ctx.pathParamAsClass("factId", Integer.class).check(id -> id > 0, "ID must be greater than 0").get();
        Connection database = getConnection();
        String fact;
        String query = "SELECT fact FROM fun_facts WHERE id = ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setInt(1, factId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                fact = resultSet.getString("fact");

                if (fact.isEmpty()) {
                    throw new NotFoundResponse("Fact not found");
                } else {
                    String json = objecToJson(fact);
                    ctx.result(json);
                    ctx.status(200);
                }
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    // Requirement: F.DP.14
    @OpenApi(
            summary = "Update user information based on ID and password",
            operationId = "updateUserById",
            path = "/v1/users/{id}",
            methods = HttpMethod.PATCH,
            pathParams = {
                    @OpenApiParam(name = "id", type = Integer.class, description = "The user ID"),
            },
            tags = {"User"},
            requestBody = @OpenApiRequestBody(
                    description = "You can use one field at a time to update the user, always include the password.",
                    content = {
                            @OpenApiContent(from = NewUpdateUserRequest.class),
                    }),
            responses = {
                    @OpenApiResponse(status = "200"),
                    @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public static void updateUser(Context ctx) {
        int userId = ctx.pathParamAsClass("id", Integer.class).check(id -> id > 0, "ID must be greater than 0").get();

        Connection database = getConnection();
        String password = "";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(ctx.body());
            if (jsonNode.get("password") != null) {
                password = jsonNode.get("password").asText();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (checkPassword(userId, password)) {
            if (jsonNode.get("funFactsActivated") != null) {
                boolean funFactsActivated = jsonNode.get("funFactsActivated").asBoolean();
                String query = "UPDATE user SET fun_facts_activated = ? WHERE id = ?;";
                try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
                    preparedStatement.setBoolean(1, funFactsActivated);
                    preparedStatement.setInt(2, userId);
                    preparedStatement.executeUpdate();
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }
            } else if (jsonNode.get("notificationsActivated") != null) {
                boolean notificationActivated = jsonNode.get("notificationsActivated").asBoolean();
                String query = "UPDATE user SET notification_activated = ? WHERE id = ?;";
                try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
                    preparedStatement.setBoolean(1, notificationActivated);
                    preparedStatement.setInt(2, userId);
                    preparedStatement.executeUpdate();
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }
            }
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
            requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = NewDeleteRequest.class)}),
            responses = {
                    @OpenApiResponse(status = "204"),
                    @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public static void deleteUser(Context ctx) {
        int userId = Integer.parseInt(ctx.pathParam("id"));

        NewDeleteRequest deleteRequest = ctx.bodyAsClass(NewDeleteRequest.class);

        Connection database = getConnection();
        String query = "SELECT password FROM user WHERE id = ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String hashedPassword = resultSet.getString("password");
                boolean isVerified = BCrypt.checkpw(deleteRequest.password, hashedPassword);

                if (isVerified) {
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
                } else {
                    ctx.status(400);
                    ctx.result("Password incorrect");
                }
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    // Requirement: F.DP.4
    @OpenApi(
            summary = "Get all plants",
            operationId = "getAllPlants",
            path = "/v1/users/{id}/plants",
            pathParams = {@OpenApiParam(name = "id", type = Integer.class, description = "The user ID")},
            methods = HttpMethod.GET,
            tags = {"User Plants"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Plant[].class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public static void getAllPlants(Context context) {
        int userId = context.pathParamAsClass("id", Integer.class).check(id -> id > 0, "ID must be greater than 0").get();
        ArrayList<NewPlantRequest> plants = new ArrayList<>();

        Connection database = getConnection();
        String queryUserPlant = "SELECT * FROM plant WHERE user_id = ?;";
        String queryPlantDetails = "SELECT * FROM plantdetails WHERE id = ?";

        try (PreparedStatement preparedStatement = database.prepareStatement(queryUserPlant)) {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                NewPlantRequest plant = new NewPlantRequest();
                plant.id = resultSet.getInt("id");
                plant.nickname = resultSet.getString("nickname");
                plant.lastWatered = resultSet.getDate("last_watered").toString();
                plant.imageURL = resultSet.getString("image_url");
                System.out.println(plant.lastWatered);

                try (PreparedStatement preparedStatement2 = database.prepareStatement(queryPlantDetails)) {
                    preparedStatement2.setInt(1, plant.id);
                    ResultSet resultSet2 = preparedStatement2.executeQuery();
                    plant.commonName = resultSet2.getString("common_name");
                    plant.scientificName = resultSet2.getString("scientific_name");
                    plant.genus = resultSet2.getString("genus");
                    plant.family = resultSet2.getString("family");
                    plant.waterFrequency = getWaterFrequency(plant.id);
                    plant.light = resultSet2.getInt("light");
                }

                plants.add(plant);
            }

        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        String json = objecToJson(plants);
        context.result(json);
        context.status(200);
    }

    // Requirement: F.DP.9
    @OpenApi(
            summary = "Update all plants by user ID",
            operationId = "updateAllPlants",
            path = "/v1/users/{id}/plants",
            methods = HttpMethod.PATCH,
            pathParams = {
                    @OpenApiParam(name = "id", type = Integer.class, description = "The user ID"),
            },
            tags = {"User Plants"},
            requestBody = @OpenApiRequestBody(
                    description = "You can use one field at a time to update the plants",
                    content = {@OpenApiContent(from = NewPlantRequest.class)}),
            responses = {
                    @OpenApiResponse(status = "200"),
                    @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public static void updateAllPlants(Context ctx) {
        int userId = ctx.pathParamAsClass("id", Integer.class).check(id -> id > 0, "ID must be greater than 0").get();
        NewPlantRequest plant = ctx.bodyAsClass(NewPlantRequest.class);

        // check if date is the correct format
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.parse(plant.lastWatered);
        } catch (Exception e) {
            ctx.status(400);
            ctx.result("Date is not in the correct format, are you sure you are using yyyy-MM-dd?");
            return;
        }

        Connection database = getConnection();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(ctx.body());

            if (jsonNode.get("lastWatered") != null) {
                String query = "UPDATE plant SET last_watered = ? WHERE user_id = ?;";
                try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
                    preparedStatement.setDate(1, Date.valueOf(plant.lastWatered));
                    preparedStatement.setInt(2, userId);
                    preparedStatement.executeUpdate();

                    String json = String.format("{\"lastWatered\": \"%s\"}", plant.lastWatered);
                    ctx.result(json);
                    ctx.status(200);
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // Requirement: F.DP.10
    @OpenApi(
            summary = "Get plant based on user ID and plant ID",
            operationId = "getPlant",
            path = "/v1/users/{id}/plants/{plantId}",
            methods = HttpMethod.GET,
            tags = {"User Plant"},
            pathParams = {
                    @OpenApiParam(name = "id", type = Integer.class, description = "The user ID"),
                    @OpenApiParam(name = "plantId", description = "The plant ID")},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Plant.class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public static void getPlant(Context ctx) {
        int userId = ctx.pathParamAsClass("id", Integer.class).check(id -> id > 0, "ID must be greater than 0").get();
        int plantId = ctx.pathParamAsClass("plantId", Integer.class).check(id -> id > 0, "ID must be greater than 0").get();

        Connection database = getConnection();
        String query = "SELECT * FROM plant WHERE user_id = ? AND id = ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, plantId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String nickname = resultSet.getString("nickname");
                int plantIdDb = resultSet.getInt("id");
                Date lastWatered = resultSet.getDate("last_watered");
                String imageURL = resultSet.getString("image_url");

                long waterFrequency = getWaterFrequency(plantIdDb);
                Plant plant = new Plant(nickname, plantIdDb, lastWatered.toString(), waterFrequency, imageURL);

                String json = objecToJson(plant);
                ctx.result(json);
                ctx.status(200);
            } else {
                throw new NotFoundResponse("Plant not found");
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    // Requirement: F.DP.7
    @OpenApi(
            summary = "Update plant by ID",
            operationId = "updatePlantById",
            path = "/v1/users/{id}/plants/{plantId}",
            methods = HttpMethod.PATCH,
            pathParams = {
                    @OpenApiParam(name = "id", type = Integer.class, description = "The user ID"),
                    @OpenApiParam(name = "plantId", type = Integer.class, description = "The plant ID"),
            },
            tags = {"User Plant"},
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
        NewPlantRequest plant = ctx.bodyAsClass(NewPlantRequest.class);

        // check if date is the correct format
        if (plant.lastWatered != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.parse(plant.lastWatered);
            } catch (Exception e) {
                ctx.status(400);
                ctx.result("Date is not in the correct format, are you sure you are using yyyy-MM-dd?");
                return;
            }
        }

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
            } else if (jsonNode.get("image_url") != null) {
                String imageURL = jsonNode.get("image_url").asText();
                String query = "UPDATE plant SET image_url = ? WHERE user_id = ? AND id = ?;";

                try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
                    preparedStatement.setString(1, imageURL);
                    preparedStatement.setInt(2, userId);
                    preparedStatement.setInt(3, plantId);
                    preparedStatement.executeUpdate();

                    String json = String.format("{\"imageURL\": \"%s\"}", imageURL);
                    ctx.result(json);
                    ctx.status(200);
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }
            } else if (jsonNode.get("lastWatered") != null) {
                LocalDate date = LocalDate.parse(jsonNode.get("lastWatered").asText());
                String query = "UPDATE plant SET last_watered = ? WHERE user_id = ? AND id = ?;";

                try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
                    preparedStatement.setDate(1, Date.valueOf(date));
                    preparedStatement.setInt(2, userId);
                    preparedStatement.setInt(3, plantId);
                    preparedStatement.executeUpdate();

                    String json = String.format("{\"lastWatered\": \"%s\"}", date);
                    ctx.result(json);
                    ctx.status(200);
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // Requirement: F.DP.8
    @OpenApi(
            summary = "Delete plant by ID",
            operationId = "deletePlantById",
            path = "/v1/users/{id}/plants/{plantId}",
            methods = HttpMethod.DELETE,
            pathParams = {
                    @OpenApiParam(name = "id", type = Integer.class, description = "The user ID"),
                    @OpenApiParam(name = "plantId", type = Integer.class, description = "The plant ID"),
            },
            tags = {"User Plant"},
            responses = {
                    @OpenApiResponse(status = "204"),
                    @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public static void deletePlant(Context ctx) {
        int userId = ctx.pathParamAsClass("id", Integer.class).check(id -> id > 0, "ID must be greater than 0").get();
        int plantId = ctx.pathParamAsClass("plantId", Integer.class).check(id -> id > -1, "ID must be greater than 0").get();

        Connection database = getConnection();
        String query = "DELETE FROM plant WHERE user_id = ? AND id = ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, plantId);
            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                throw new NotFoundResponse("Plant not found");
            }

            ctx.status(204);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    // Requirement: F.DP.1
    @OpenApi(
            summary = "Add plant to user",
            operationId = "savePlant",
            path = "/v1/users/{id}/plants",
            methods = HttpMethod.POST,
            tags = {"User Plant"},
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

        // check if date is the correct format
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.parse(plant.lastWatered);
        } catch (Exception e) {
            ctx.status(400);
            ctx.result("Date is not in the correct format, are you sure you are using yyyy-MM-dd?");
            return;
        }

        String sqlSafeNickname = plant.nickname.replace("'", "''");
        String plantQuery = "INSERT INTO plant (user_id, nickname, plant_id, last_watered, image_url) VALUES (?, ?, ?, ?, ?);";
        String detailsQuery = "INSERT INTO plantdetails (scientific_name, genus, family, common_name, image_url, light, url_wikipedia_en, water_frequency, plant_id)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (PreparedStatement plantStatement = database.prepareStatement(plantQuery)) {
            plantStatement.setInt(1, userId);
            plantStatement.setString(2, sqlSafeNickname);
            plantStatement.setInt(3, plant.id);
            plantStatement.setDate(4, Date.valueOf(plant.lastWatered));
            plantStatement.setString(5, plant.imageURL);
            plantStatement.executeUpdate();

            // Checks if plant details already exist in the database
            // We might want to have several of the same plants in our dashboard
            // But no duplicate details about them!
            String query = "SELECT COUNT(*) FROM plantdetails WHERE plant_id = ?";
            boolean doesPlantDetailExist = false;
            try (PreparedStatement preparedStatement = database.prepareStatement(query)) {
                preparedStatement.setInt(1, plant.id);
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
                    detailsStatement.setInt(9, plant.id);
                    detailsStatement.executeUpdate();
                }
            }

            isCreated = true;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        String queryGetID = "SELECT id FROM plant WHERE user_id = ? AND nickname = ?;";

        try (PreparedStatement preparedStatement = database.prepareStatement(queryGetID)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, sqlSafeNickname);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                plant.id = resultSet.getInt("id");
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        if (isCreated) {
            String json = objecToJson(plant);
            ctx.result(json);
            ctx.status(200);
        } else {
            ctx.status(409);
            ctx.result("You already have a plant with that nickname");
        }
    }

    // Requirement: F.DP.1
    @OpenApi(
            summary = "Get plants based on search parameter",
            operationId = "getPlants",
            path = "/v1/plants?plant=",
            methods = HttpMethod.GET,
            tags = {"Plants"},
            queryParams = {@OpenApiParam(name = "plant", description = "The plant name")},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = TreflePlantSwaggerObject[].class)}),
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
}