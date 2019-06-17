package com.alexco.simplevertxservice.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.serviceproxy.ServiceBinder;

public class UserDatabaseVerticle extends AbstractVerticle {

    public static final String CONFIG_KEY_USER_DB_URL = "userdb.jdbc.url";
    public static final String CONFIG_KEY_JDBC_DRIVER_CLASS = "userdb.jdbc.driver_class";
    public static final String CONFIG_KEY_JDBC_MAX_POOL_SIZE = "userdb.jdbc.max_pool_size";

    @Override
    public void start(Future<Void> startFuture) {
        JsonObject jdbcConfig = config().getJsonObject("userdb").getJsonObject("jdbc");
        JDBCClient client = JDBCClient.createShared(vertx, jdbcConfig);
        UserDatabaseService.create(client, readyHandler -> {
           if (readyHandler.succeeded()) {
                new ServiceBinder(vertx)
                        .setAddress("user.queue")
                        .register(UserDatabaseService.class, readyHandler.result());
                startFuture.complete();
           } else {
                startFuture.fail(readyHandler.cause());
           }
        });
    }
}
