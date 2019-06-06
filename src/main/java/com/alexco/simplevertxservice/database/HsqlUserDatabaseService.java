package com.alexco.simplevertxservice.database;

import com.alexco.simplevertxservice.user.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

public class HsqlUserDatabaseService implements UserDatabaseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HsqlUserDatabaseService.class);

    protected static final String CREATE_USER_TABLE = "CREATE TABLE if not exists Users (" +
            "uuid varchar(255), " +
            "name varchar(255), " +
            "age integer, " +
            "PRIMARY KEY(uuid)" +
            ")";

    private final JDBCClient jdbcClient;

    HsqlUserDatabaseService(JDBCClient jdbcClient, Handler<AsyncResult<UserDatabaseService>> handler) {
        this.jdbcClient = jdbcClient;

        jdbcClient.getConnection(getConnResult -> {
            if (getConnResult.failed()) {
                LOGGER.error("Failed to get connection to database", getConnResult.cause());
                handler.handle(Future.failedFuture(getConnResult.cause()));
            } else {
                SQLConnection sqlConnection = getConnResult.result();
                sqlConnection.execute(CREATE_USER_TABLE, createResult -> {
                    sqlConnection.close();
                    if (createResult.failed()) {
                        LOGGER.error("Failed to create the user table", createResult.cause());
                        handler.handle(Future.failedFuture(createResult.cause()));
                    } else {
                        handler.handle(Future.succeededFuture(this));
                    }
                });
            }
        });
    }

    // TODO: actually call DB
    @Override
    public UserDatabaseService getUserById(String uuid, Handler<AsyncResult<JsonObject>> handler) {
        LOGGER.info("Getting user by id");
        User u = new User("uuid-123", "alex",  32);
        JsonObject userJson = JsonObject.mapFrom(u);
        handler.handle(Future.succeededFuture(userJson));
        return this;
    }

    @Override
    public UserDatabaseService putUser(JsonObject user, Handler<AsyncResult<Void>> handler) {
        return this;
    }
}
