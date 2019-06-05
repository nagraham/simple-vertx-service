package com.alexco.simplevertxservice.user;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

public class PutUserHandler implements Handler<RoutingContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PutUserHandler.class);

    public static PutUserHandler getInstance() {
        return new PutUserHandler();
    }

    @Override
    public void handle(RoutingContext ctx) {
        String id = ctx.request().getParam("id");
        JsonObject result = new JsonObject();

        try {
            JsonObject inputJson = ctx.getBodyAsJson();
            User u = inputJson.mapTo(User.class);
            LOGGER.info("PUT for " + u);
            ctx.response().setStatusCode(200);
            result.put("success", true);
        } catch (IllegalArgumentException e) {
            ctx.response().setStatusCode(400);
            result.put("success", false).put("error", "Invalid argument");
        } catch (Exception e) {
            LOGGER.error("Exception occurred while attempting to put with id: " + id, e);
            ctx.response().setStatusCode(500);
            result.put("success", false);
        }

        ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(result.encode());
    }
}
