package com.alexco.simplevertexservice.user;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

public class GetUserHandler implements Handler<RoutingContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetUserHandler.class);

    public static GetUserHandler getInstance() {
        return new GetUserHandler();
    }

    @Override
    public void handle(RoutingContext ctx) {
        String id = ctx.request().getParam("id");

        User u = new User(id, "alex",  32);
        JsonObject userJson = JsonObject.mapFrom(u);

        LOGGER.info("GET " + u);

        JsonObject response = new JsonObject()
                .put("success", true)
                .put("user", userJson);

        ctx.response()
                .setStatusCode(200)
                .putHeader("Content-Type", "application/json")
                .end(response.encode());
    }
}
