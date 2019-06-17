package com.alexco.simplevertxservice;

import com.alexco.simplevertxservice.database.UserDatabaseVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * Deploys all verticles within this application
 */
public class MainVerticle extends AbstractVerticle {

    // deploy via class path so Vertx will instantiate multiple instances
    private static final String USER_HTTP_VERTICLE = "com.alexco.simplevertxservice.user.UserHttpServerVerticle";

    @Override
    public void start(Future<Void> startFuture) {
        getConfigRetriever().getConfig(configAsyncResult -> {
            if (configAsyncResult.succeeded()) {
                JsonObject config = configAsyncResult.result();
                deployUserDatabaseVerticle(config).compose(id -> deployHttpServerVerticle(config))
                        .setHandler(asyncResult -> {
                            if (asyncResult.succeeded()) {
                                startFuture.complete();
                            } else {
                                startFuture.fail(asyncResult.cause());
                            }
                        });
            } else {
                startFuture.fail(configAsyncResult.cause());
            }
        });
    }

    private ConfigRetriever getConfigRetriever() {
        ConfigStoreOptions httpConfigStore = new ConfigStoreOptions()
                .setType("file")
                .setConfig(new JsonObject().put("path", "src/main/resources/http/development.json"));

        ConfigStoreOptions userDbConfigStore = new ConfigStoreOptions()
                .setType("file")
                .setConfig(new JsonObject().put("path", "src/main/resources/userdb/development.json"));

        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
                .addStore(httpConfigStore)
                .addStore(userDbConfigStore);

        return ConfigRetriever.create(vertx, options);
    }

    private Future<String> deployUserDatabaseVerticle(JsonObject config) {
        Future<String> future = Future.future();
        vertx.deployVerticle(new UserDatabaseVerticle(),
                new DeploymentOptions().setConfig(config),
                future);
        return future;
    }

    private Future<String> deployHttpServerVerticle(JsonObject config) {
        Future<String> future = Future.future();

        // deploy 3 instances (b/c why the hell not? ... vertx round-robins requests on port 8080 to each instance)
        DeploymentOptions deploymentOptions = new DeploymentOptions()
                .setConfig(config)
                .setInstances(3);

        vertx.deployVerticle(USER_HTTP_VERTICLE, deploymentOptions, future);

        return future;
    }
}
