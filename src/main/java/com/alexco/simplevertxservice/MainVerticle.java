package com.alexco.simplevertxservice;

import com.alexco.simplevertxservice.database.UserDatabaseVerticle;
import com.alexco.simplevertxservice.user.UserHttpServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * Deploys all verticles within this application
 */
public class MainVerticle extends AbstractVerticle {
    private static final String USER_HTTP_VERTICLE = "com.alexco.simplevertxservice.user.UserHttpServerVerticle";

    @Override
    public void start(Future<Void> startFuture) {

        deployUserDatabaseVerticle().compose(id -> deployHttpServerVerticle()).setHandler(asyncResult -> {
            if (asyncResult.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(asyncResult.cause());
            }
        });
    }

    private Future<String> deployUserDatabaseVerticle() {
        Future<String> future = Future.future();
        vertx.deployVerticle(new UserDatabaseVerticle(), future);
        return future;
    }

    private Future<String> deployHttpServerVerticle() {
        Future<String> future = Future.future();

        // TODO: refactor config to be read in from JSON file
        JsonObject httpServerConfig = new JsonObject()
                .put(UserHttpServerVerticle.CONFIG_HTTP_PORT, 8080);

        // deploy 3 instances (b/c why the hell not? ... vertx round-robins requests on port 8080 to each instance)
        DeploymentOptions deploymentOptions = new DeploymentOptions()
                .setConfig(httpServerConfig)
                .setInstances(3);

        vertx.deployVerticle(USER_HTTP_VERTICLE, deploymentOptions, future);

        return future;
    }
}
