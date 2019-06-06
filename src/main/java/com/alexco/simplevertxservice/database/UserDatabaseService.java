package com.alexco.simplevertxservice.database;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

@ProxyGen
@VertxGen
public interface UserDatabaseService {

    @Fluent
    UserDatabaseService getUserById(String uuid, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    UserDatabaseService putUser(JsonObject user, Handler<AsyncResult<Void>> handler);

    @GenIgnore
    static UserDatabaseService create(JDBCClient jdbcClient, Handler<AsyncResult<UserDatabaseService>> handler) {
        return new HsqlUserDatabaseService(jdbcClient, handler);
    }

    @GenIgnore
    static UserDatabaseService createProxy(Vertx vertx, String address) {
        return new UserDatabaseServiceVertxEBProxy(vertx, address);
    }
}
