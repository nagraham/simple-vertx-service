package com.alexco.simplevertxservice.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.serviceproxy.ServiceBinder;

public class UserDatabaseVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {
        JDBCClient client = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", "jdbc:hsqldb:db/user")
                .put("driver_class", "org.hsqldb.jdbcDriver")
                .put("max_pool_size", 30));
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
