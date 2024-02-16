package se.myhappyplants.javalin;

import io.javalin.Javalin;
import io.javalin.openapi.*;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import se.myhappyplants.javalin.user.User;
import se.myhappyplants.javalin.user.UserController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
            // Routing!
            config.router.apiBuilder(() -> {
                // Users api routes
                path("", () ->
                        // redirect me to the swagger page
                        get(ctx -> ctx.redirect("/swagger"))
                );
                path("v1/", () -> {
                    path("users", () -> {
                        get(UserController::getAll);
                        post(UserController::create);
                        path("{userId}", () -> {
                            get(UserController::getOne);
                            patch(UserController::update);
                            delete(UserController::delete);
                        });
                    });
                    path("plants", () -> {
                        get(ctx -> {
                            ctx.result("You requested all plants");
                        });
                        path("search", () ->
                                get(ctx -> {
                                    String plant = ctx.queryParam("plant");
                                    HttpResponse<String> result = getPlants(plant);

                                    ctx.result(result.body());
                                })
                        );
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
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = User[].class)})
            }
    )
    public static HttpResponse<String> getPlants(String plant) throws ExecutionException, InterruptedException {
        String TrefleKey = System.getenv("TREFLE_API_KEY");

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder(
                        URI.create("https://trefle.io/api/v1/plants/search?token=" + TrefleKey + "&q=" + plant))
                .header("accept", "application/json")

                .build();
        CompletableFuture<HttpResponse<String>> response =
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        HttpResponse<String> result = response.get();

        return result;
    }
}