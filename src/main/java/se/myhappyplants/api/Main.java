package se.myhappyplants.api;

import io.javalin.Javalin;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import se.myhappyplants.api.user.UserController;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Main {
    public static void main(String[] args) {
        Javalin.create(config -> {
                    // Plugin for documentation and testing our api
                    config.registerPlugin(new OpenApiPlugin(pluginConfig -> {
                        pluginConfig.withDefinitionConfiguration((version, definition) -> {
                            definition.withOpenApiInfo(info -> info.setTitle("Javalin OpenAPI example"));
                        });
                    }));
                    config.registerPlugin(new SwaggerPlugin());

                    // Routing!
                    config.router.apiBuilder(() -> {
                        // Users api routes
                        path("users", () -> {
                            get(UserController::getAll);
                            post(UserController::create);
                            path("{userId}", () -> {
                                get(UserController::getOne);
                                patch(UserController::update);
                                delete(UserController::delete);
                            });
                        });
                    });
                }).start(7002);

        System.out.println("Check out Swagger UI docs at http://localhost:7002/swagger");

        String key = System.getenv("TREFLE_API_KEY");
        System.out.println(key);
    }
}