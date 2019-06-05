package com.alexco.simplevertxservice;

import com.alexco.simplevertxservice.user.UserHttpServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * Deploys all verticles within this application
 */
public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {
        deployHttpServerVerticle().setHandler(asyncResult -> {
            if (asyncResult.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(asyncResult.cause());
            }
        });
    }

    private Future<String> deployHttpServerVerticle() {
        Future<String> future = Future.future();
        // TODO: refactor config to be read in from JSON file
        JsonObject httpServerConfig = new JsonObject()
                .put(UserHttpServerVerticle.CONFIG_HTTP_PORT, 8080);

        vertx.deployVerticle(
                "com.alexco.simplevertxservice.user.UserHttpServerVerticle",
                new DeploymentOptions().setConfig(httpServerConfig),
                future);

        return future;
    }
}
