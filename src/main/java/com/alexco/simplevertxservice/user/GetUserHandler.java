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
        JsonObject response = new JsonObject();

        userDatabaseService.getUserById(id, reply -> {
            if (reply.succeeded()) {
                JsonObject dbPayload = reply.result();

                if (dbPayload.getBoolean("found")) {
                    response.put("success", true).put("user", dbPayload.getJsonObject("user"));
                    ctx.response().setStatusCode(200);
                } else {
                    response.put("success", false);
                    response.put("error", "User for id [" + id + "] not found");
                    ctx.response().setStatusCode(404);
                }

            } else {
                LOGGER.error("Failed to get user");
                response.put("success", false).put("error", "Internal server error");
                ctx.response().setStatusCode(500);
            }

            ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(response.encode());
        });
    }
}
