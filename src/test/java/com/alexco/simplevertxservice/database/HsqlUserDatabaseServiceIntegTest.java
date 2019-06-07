package com.alexco.simplevertxservice.database;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.serviceproxy.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

@ExtendWith(VertxExtension.class)
public class HsqlUserDatabaseServiceIntegTest {
    private static final String TEST_UUID = "test-uuid";
    private static final String TEST_NAME = "test-name";
    private static final int TEST_AGE = 42;
    private static final JsonObject TEST_JSON_USER = userJsonObject(TEST_UUID, TEST_NAME, TEST_AGE);


    private UserDatabaseService hsqlUserDatabaseService;

    @BeforeEach
    void setup(Vertx vertx, VertxTestContext vertxTestContext) {
        JsonObject config = new JsonObject()
                .put(UserDatabaseVerticle.CONFIG_KEY_USER_DB_URL, "jdbc:hsqldb:mem:testdb;shutdown=true")
                .put(UserDatabaseVerticle.CONFIG_KEY_JDBC_MAX_POOL_SIZE, 4);

        vertx.deployVerticle(
                new UserDatabaseVerticle(),
                new DeploymentOptions().setConfig(config),
                stringAsyncResult -> {
                    hsqlUserDatabaseService = UserDatabaseService.createProxy(vertx, "user.queue");
                    if (stringAsyncResult.succeeded()) {
                        vertxTestContext.completeNow();
                    } else {
                        vertxTestContext.failNow(stringAsyncResult.cause());
                    }
                });
    }

    @Test
    void create_whenUserDoesNotExist_completesCallSuccessfully(Vertx vertx, VertxTestContext vertxTestContext) {
        hsqlUserDatabaseService.createUser(TEST_JSON_USER, result -> {
            if (result.succeeded()) {
                vertxTestContext.completeNow();
            } else {
                vertxTestContext.failNow(result.cause());
            }
        });
    }

    @Test
    void create_whenUserWithSameUuidExists_throwsError(Vertx vertx, VertxTestContext vertxTestContext) {
        hsqlUserDatabaseService.createUser(TEST_JSON_USER, vertxTestContext.succeeding(nextHandler -> {
            hsqlUserDatabaseService.createUser(TEST_JSON_USER, vertxTestContext.failing(cause -> {
                vertxTestContext.verify(() -> {
                    assertThat(cause, isA(ServiceException.class));
                    vertxTestContext.completeNow();
                });
            }));
        }));
    }

    @Test
    void get_whenUserExists_returnsJsonObjectWithUserData(Vertx vertx, VertxTestContext vertxTestContext) {
        hsqlUserDatabaseService.createUser(TEST_JSON_USER, vertxTestContext.succeeding(handler -> {
            hsqlUserDatabaseService.getUserById(TEST_UUID, vertxTestContext.succeeding(result -> {
                vertxTestContext.verify(() -> {
                    assertThat(result.getBoolean("found"), is(true));
                    assertThat(result.getJsonObject("user"), is(equalTo(TEST_JSON_USER)));
                    vertxTestContext.completeNow();
                });
            }));
        }));
    }

    @Test
    void get_whenUserDoesNotExist_returnsResponseWithFoundSetToFalse(Vertx vertx, VertxTestContext vertxTestContext) {
        hsqlUserDatabaseService.getUserById(TEST_UUID, vertxTestContext.succeeding(result -> {
            vertxTestContext.verify(() -> {
                assertThat(result.getBoolean("found"), is(false));
                vertxTestContext.completeNow();
            });
        }));
    }

    private static JsonObject userJsonObject(String uuid, String name, int age) {
        return new JsonObject()
                .put("uuid", uuid)
                .put("name", name)
                .put("age", age);
    }
}