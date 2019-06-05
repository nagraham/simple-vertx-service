package com.alexco.simplevertexservice.user;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/*
 * Even though User is a simple POJO, I want to have tests around serialization to understand
 * how Vert.x's JsonObject works.
 */
class UserTest {

    private static final String TEST_UUID = "test-uuid";
    private static final String TEST_NAME = "test-name";
    private static final int TEST_AGE = 42;
    private static final String FULL_JSON = "{" +
            "\"uuid\":\"" + TEST_UUID + "\"," +
            "\"name\":\"" + TEST_NAME + "\"," +
            "\"age\":" + TEST_AGE +
            "}";
    private static final String PARTIAL_JSON = "{\"uuid\":\"" + TEST_UUID + "\"}";

    User user = new User(TEST_UUID, TEST_NAME, TEST_AGE);

    @Test
    void user_canBeMappedToJson() {
        JsonObject obj = JsonObject.mapFrom(user);
        assertThat(obj.getString("uuid"), is(TEST_UUID));
    }

    @Test
    void user_canBeMappedFromFullJson() {
        User result = new JsonObject(FULL_JSON).mapTo(User.class);
        assertThat(result.getUuid(), is(TEST_UUID));
        assertThat(result.getName(), is(TEST_NAME));
        assertThat(result.getAge(), is(TEST_AGE));
    }

    @Test
    void user_canBeMappedFromPartialJson() {
        User result = new JsonObject(PARTIAL_JSON).mapTo(User.class);
        assertThat(result.getUuid(), is(TEST_UUID));
    }
}