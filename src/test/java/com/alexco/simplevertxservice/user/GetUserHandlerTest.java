package com.alexco.simplevertxservice.user;

import com.alexco.simplevertxservice.TestUtils;
import com.alexco.simplevertxservice.database.UserDatabaseService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserHandlerTest {
    private static final String TEST_UUID = "test-uuid";
    private static final String TEST_NAME = "test-name";
    private static final int TEST_AGE = 42;
    private static final User TEST_USER = new User(TEST_UUID, TEST_NAME, TEST_AGE);
    private static final String TEST_ERROR_MSG = "something went wrong";

    @Mock
    UserDatabaseService mockUserDatabaseService;

    @Mock
    RoutingContext mockRoutingContext;

    @Mock
    HttpServerRequest mockHttpServerRequest;

    @Mock(answer = Answers.RETURNS_SELF )
    HttpServerResponse mockHttpServerResponse;

    GetUserHandler getUserHandler;

    @BeforeEach
    void setup() {
        getUserHandler = GetUserHandler.getInstance(mockUserDatabaseService);
        when(mockRoutingContext.request()).thenReturn(mockHttpServerRequest);
        when(mockRoutingContext.response()).thenReturn(mockHttpServerResponse);
        when(mockHttpServerRequest.getParam("id")).thenReturn(TEST_UUID);
    }

    @Test
    void handle_userFound_setsStatusCode200() {
        mockUserDatabaseToReturn(TEST_USER, TEST_UUID);

        getUserHandler.handle(mockRoutingContext);

        verify(mockHttpServerResponse).setStatusCode(200);
    }

    @Test
    void handle_userFound_setsContentTypeHeaderToApplicationJson() {
        mockUserDatabaseToReturn(TEST_USER, TEST_UUID);

        getUserHandler.handle(mockRoutingContext);

        verify(mockHttpServerResponse).putHeader("Content-Type", "application/json");
    }

    @Test
    void handle_userFound_callsEndWithEncodedResponseObject() {
        mockUserDatabaseToReturn(TEST_USER, TEST_UUID);

        getUserHandler.handle(mockRoutingContext);

        String expectedJsonEncoding = new JsonObject()
                .put("success", true)
                .put("user", JsonObject.mapFrom(TEST_USER))
                .encode();

        verify(mockHttpServerResponse).end(expectedJsonEncoding);
    }

    @Test
    void handle_userNotFound_setsStatusCode404() {
        mockUserDatabaseToReturn(null, TEST_UUID);

        getUserHandler.handle(mockRoutingContext);

        verify(mockHttpServerResponse).setStatusCode(404);
    }

    @Test
    void handle_userNotFound_encodedResponseObjectHasErrorMessage() {
        mockUserDatabaseToReturn(null, TEST_UUID);

        getUserHandler.handle(mockRoutingContext);

        String expectedJsonEncoding = new JsonObject()
                .put("success", false)
                .put("error", "User for id [" + TEST_UUID + "] not found")
                .encode();

        verify(mockHttpServerResponse).end(expectedJsonEncoding);
    }

    @Test
    void handle_failedDbCall_returnsStatusCode500() {
        mockUserDatabaseToFail(TEST_ERROR_MSG);

        getUserHandler.handle(mockRoutingContext);

        verify(mockHttpServerResponse).setStatusCode(500);
    }

    @Test
    void handle_failedDbCall_returnsOpaqueErrorMessage() {
        mockUserDatabaseToFail(TEST_ERROR_MSG);

        getUserHandler.handle(mockRoutingContext);

        String expectedJsonEncoding = new JsonObject()
                .put("success", false)
                .put("error", "Internal server error")
                .encode();

        verify(mockHttpServerResponse).end(expectedJsonEncoding);
    }

    public void mockUserDatabaseToReturn(User user, String userId) {
        JsonObject result = new JsonObject();
        if (user == null) {
            result.put("found", false);
        } else {
            result.put("found", true).put("user", JsonObject.mapFrom(user));
        }
        Future<JsonObject> userFuture = Future.succeededFuture(result);
        doAnswer(invocationOnMock -> {
            Handler<AsyncResult<JsonObject>> handler = invocationOnMock.getArgument(1);
            handler.handle(userFuture);
            return null;
        }).when(mockUserDatabaseService).getUserById(eq(userId), any(Handler.class));
    }

    public void mockUserDatabaseToFail(String failureMsg) {
        Future<JsonObject> userFuture = Future.failedFuture(TestUtils.createThrowable(failureMsg));
        doAnswer(invocationOnMock -> {
            Handler<AsyncResult<JsonObject>> handler = invocationOnMock.getArgument(1);
            handler.handle(userFuture);
            return null;
        }).when(mockUserDatabaseService).getUserById(anyString(), any(Handler.class));
    }
}