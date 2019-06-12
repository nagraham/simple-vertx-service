package com.alexco.simplevertxservice;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(VertxExtension.class)
public class UserServiceSystemTest {
    private static WebClient webClient;

    @BeforeAll
    static void setup(Vertx vertx, VertxTestContext vertxTestContext) {
        vertx.deployVerticle(new MainVerticle(), vertxTestContext.completing());
        webClient = WebClient.create(vertx);
    }

    @Test
    void createAndGetNewUser(Vertx vertx, VertxTestContext vertxTestContext) {
        String id = UUID.randomUUID().toString();
        JsonObject newUser = new JsonObject()
                .put("uuid", id)
                .put("name", "test-name")
                .put("age", 42);

        // TODO: Maybe bring in JavaRx for these system tests as the first place to play with it
        webClient.put(8080, "localhost", "/user/" + id)
                .sendJsonObject(newUser, putResult -> {
                    if (putResult.succeeded()) {
                        webClient.get(8080, "localhost", "/user/" + id).send(getResult -> {
                            if (getResult.succeeded()) {
                                vertxTestContext.verify(() -> {
                                    JsonObject resultJson = getResult.result().bodyAsJsonObject();
                                    assertThat(resultJson.getBoolean("success"), is(true));
                                    JsonObject getUserJson = resultJson.getJsonObject("user");
                                    assertThat(newUser, equalTo(getUserJson));
                                    vertxTestContext.completeNow();
                                });
                            } else {
                                vertxTestContext.failNow(getResult.cause());
                            }
                        });
                    } else {
                        vertxTestContext.failNow(putResult.cause());
                    }
                });
    }

    @Test
    void getNonExistentUser(Vertx vertx, VertxTestContext vertxTestContext) {
        String id = "non_existent_user_uuid";
        webClient.get(8080, "localhost", "/user/" + id).send(getResult -> {
            if (getResult.succeeded()) {
                vertxTestContext.verify(() -> {
                    JsonObject resultJson = getResult.result().bodyAsJsonObject();
                    assertThat(resultJson.getBoolean("success"), is(false));
                    vertxTestContext.completeNow();
                });
            } else {
                vertxTestContext.failNow(getResult.cause());
            }
        });
    }
}
