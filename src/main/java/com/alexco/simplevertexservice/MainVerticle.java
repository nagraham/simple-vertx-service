package com.alexco.simplevertexservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        startHttpServer().setHandler(asyncResult -> {
            if (asyncResult.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(asyncResult.cause());
            }
        });
    }

    private Future<Void> startHttpServer() {
        Future<Void> future = Future.future();

        Router router = Router.router(vertx);
        router.get("/user/:id").handler(this::indexHandler);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, asyncResult -> {
                    if (asyncResult.succeeded()) {
                        LOGGER.info("Running HTTP Server on port 8080");
                        future.complete();
                    } else {
                        LOGGER.error("Could not start HTTP Server", asyncResult.cause());
                        future.fail(asyncResult.cause());
                    }
                });

        return future;
    }

    private void indexHandler(RoutingContext ctx) {
        String id = ctx.request().getParam("id");

        JsonObject userJson = JsonObject.mapFrom(new User(
                id,
                "alex",
                "alex@alexco.com",
                32));

        JsonObject response = new JsonObject()
                .put("success", true)
                .put("user", userJson);

        ctx.response()
                .setStatusCode(200)
                .putHeader("Content-Type", "application/json")
                .end(response.encode());
    }
}
