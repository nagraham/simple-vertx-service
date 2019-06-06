package com.alexco.simplevertxservice.user;

import com.alexco.simplevertxservice.database.UserDatabaseService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

public class GetUserHandler implements Handler<RoutingContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetUserHandler.class);

    UserDatabaseService userDatabaseService;

    private GetUserHandler(UserDatabaseService userDatabaseService) {
        this.userDatabaseService = userDatabaseService;
    }

    public static GetUserHandler getInstance(UserDatabaseService userDatabaseService) {
        return new GetUserHandler(userDatabaseService);
    }

    @Override
    public void handle(RoutingContext ctx) {
        String id = ctx.request().getParam("id");

        userDatabaseService.getUserById("id", reply -> {
            if (reply.succeeded()) {
                JsonObject user = reply.result();
                LOGGER.info("User: " + user.mapTo(User.class));
                JsonObject response = new JsonObject()
                        .put("success", true)
                        .put("user", user);
                ctx.response()
                        .setStatusCode(200)
                        .putHeader("Content-Type", "application/json")
                        .end(response.encode());
            } else {
                LOGGER.error("Failed to get user");
                JsonObject response = new JsonObject()
                        .put("success", false)
                        .put("cause", reply.cause());
                ctx.response()
                        .setStatusCode(500)
                        .putHeader("Content-Type", "application/json")
                        .end(response.encode());
            }
        });
    }
}
