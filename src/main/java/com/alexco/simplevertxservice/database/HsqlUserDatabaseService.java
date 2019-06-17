package com.alexco.simplevertxservice.database;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

public class HsqlUserDatabaseService implements UserDatabaseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HsqlUserDatabaseService.class);

    // TODO: Refactor the SQL Strings to not be janky protected static variables, lol

    protected static final String SQL_CREATE_USER_TABLE =
            " CREATE TABLE if not exists Users (" +
            " uuid varchar(255), " +
            " name varchar(255), " +
            " age integer, " +
            " PRIMARY KEY(uuid)" +
            " )";

    protected static final String SQL_CREATE_USER = "INSERT INTO Users values (?, ?, ?)";

    protected static final String SQL_GET_USER =
            " SELECT uuid as \"uuid\", name as \"name\", age as\"age\" " +
            " FROM Users " +
            " WHERE uuid = ?";

    private final JDBCClient jdbcClient;

    HsqlUserDatabaseService(JDBCClient jdbcClient, Handler<AsyncResult<UserDatabaseService>> handler) {
        this.jdbcClient = jdbcClient;

        jdbcClient.getConnection(getConnResult -> {
            if (getConnResult.failed()) {
                LOGGER.error("Failed to get connection to database", getConnResult.cause());
                handler.handle(Future.failedFuture(getConnResult.cause()));
            } else {
                SQLConnection sqlConnection = getConnResult.result();
                sqlConnection.execute(SQL_CREATE_USER_TABLE, createResult -> {
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

    @Override
    public UserDatabaseService getUserById(String uuid, Handler<AsyncResult<JsonObject>> handler) {
        JsonArray params = new JsonArray().add(uuid);
        jdbcClient.queryWithParams(SQL_GET_USER, params, result -> {
            if (result.succeeded()) {
                ResultSet resultSet = result.result();
                JsonObject response = new JsonObject();
                if (resultSet.getRows().size() > 0) {
                    response.put("found", true);
                    response.put("user", resultSet.getRows().get(0));
                } else {
                    response.put("found", false);
                }
                handler.handle(Future.succeededFuture(response));
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });

        return this;
    }

    @Override
    public UserDatabaseService createUser(JsonObject user, Handler<AsyncResult<Void>> handler) {
        JsonArray createParams = new JsonArray()
                .add(user.getString("uuid"))
                .add(user.getString("name"))
                .add(user.getInteger("age"));

        // TODO: handle SQLIntegrityConstraintViolationException (creating duplicate user) to return IllegalArgException
        jdbcClient.updateWithParams(SQL_CREATE_USER, createParams, result -> {
            if (result.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                LOGGER.error("Failed to create User", result.cause());
                handler.handle(Future.failedFuture(result.cause()));
            }
        });

        return this;
    }
}
