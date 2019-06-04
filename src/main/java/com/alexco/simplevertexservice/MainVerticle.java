package com.alexco.simplevertexservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

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
        router.get("/user/:id").handler(this::getUserHandler);
        router.put().handler(BodyHandler.create());
        router.put("/user/:id").handler(this::putUserHandler);

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

    private void getUserHandler(RoutingContext ctx) {
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

    private void putUserHandler(RoutingContext ctx) {
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
