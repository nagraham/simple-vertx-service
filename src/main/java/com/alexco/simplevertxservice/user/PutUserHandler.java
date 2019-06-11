package com.alexco.simplevertxservice.user;

import com.alexco.simplevertxservice.database.UserDatabaseService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

public class PutUserHandler implements Handler<RoutingContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PutUserHandler.class);

    private UserDatabaseService userDatabaseService;

    private PutUserHandler(UserDatabaseService userDatabaseService) {
        this.userDatabaseService = userDatabaseService;
    }

    public static PutUserHandler getInstance(UserDatabaseService userDatabaseService) {
        return new PutUserHandler(userDatabaseService);
    }

    @Override
    public void handle(RoutingContext ctx) {
        String id = ctx.request().getParam("id");
        JsonObject response = new JsonObject();
        userDatabaseService.createUser(ctx.getBodyAsJson(), result -> {
            if (result.succeeded()) {
                ctx.response().setStatusCode(200);
                response.put("success", true);
            } else {
                // for a real service, we should actually distinguish 400/500 errors
                LOGGER.error("Exception occurred while attempting to put with id: " + id, result.cause());
                ctx.response().setStatusCode(500);
                response.put("success", false).put("error", "Internal server error");
            }

            ctx.response().putHeader("Content-Type", "application/json").end(response.encode());
        });
    }
}
