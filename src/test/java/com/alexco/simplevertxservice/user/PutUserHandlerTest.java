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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PutUserHandlerTest {
    private static final String TEST_UUID = "test-uuid";
    private static final String TEST_NAME = "test-name";
    private static final int TEST_AGE = 42;
    private static final User TEST_USER = new User(TEST_UUID, TEST_NAME, TEST_AGE);
    private static final JsonObject TEST_USER_JSON = JsonObject.mapFrom(TEST_USER);
    private static final String TEST_ERROR_MSG = "something went wrong";

    @Mock
    UserDatabaseService mockUserDatabaseService;

    @Mock
    RoutingContext mockRoutingContext;

    @Mock
    HttpServerRequest mockHttpServerRequest;

    @Mock(answer = Answers.RETURNS_SELF )
    HttpServerResponse mockHttpServerResponse;

    PutUserHandler putUserHandler;

    @BeforeEach
    void setup() {
        putUserHandler = PutUserHandler.getInstance(mockUserDatabaseService);
        when(mockRoutingContext.request()).thenReturn(mockHttpServerRequest);
        when(mockRoutingContext.response()).thenReturn(mockHttpServerResponse);
    }

    @Test
    void handle_callsDatabaseService_andSetsApplicationJsonContentType() {
        when(mockHttpServerRequest.getParam("id")).thenReturn(TEST_UUID);
        when(mockRoutingContext.getBodyAsJson()).thenReturn(TEST_USER_JSON);
        mockCreateUserCall(TEST_USER_JSON, true);

        putUserHandler.handle(mockRoutingContext);

        verify(mockUserDatabaseService).createUser(eq(TEST_USER_JSON), any(Handler.class));
        verify(mockHttpServerResponse).putHeader("Content-Type", "application/json");
    }

    @Test
    void handle_whenDatabaseCallSuccessful_setsStatusCode200() {
        when(mockHttpServerRequest.getParam("id")).thenReturn(TEST_UUID);
        when(mockRoutingContext.getBodyAsJson()).thenReturn(TEST_USER_JSON);
        mockCreateUserCall(TEST_USER_JSON, true);

        putUserHandler.handle(mockRoutingContext);

        verify(mockHttpServerResponse).setStatusCode(200);
    }

    @Test
    void handle_whenDatabaseCallSuccessful_setsSuccessToTrue() {
        when(mockHttpServerRequest.getParam("id")).thenReturn(TEST_UUID);
        when(mockRoutingContext.getBodyAsJson()).thenReturn(TEST_USER_JSON);
        mockCreateUserCall(TEST_USER_JSON, true);

        putUserHandler.handle(mockRoutingContext);

        String expectedJson = new JsonObject().put("success", true).encode();
        verify(mockHttpServerResponse).end(expectedJson);
    }

    @Test
    void handle_whenDatabaseCallFails_setsStatusCode500() {
        when(mockHttpServerRequest.getParam("id")).thenReturn(TEST_UUID);
        when(mockRoutingContext.getBodyAsJson()).thenReturn(TEST_USER_JSON);
        mockCreateUserCall(TEST_USER_JSON, false);

        putUserHandler.handle(mockRoutingContext);

        verify(mockHttpServerResponse).setStatusCode(500);
    }

    @Test
    void handle_whenDatabaseCallFails_setsSuccessToFalseAndReturnsOpaqueErrorMessage() {
        when(mockHttpServerRequest.getParam("id")).thenReturn(TEST_UUID);
        when(mockRoutingContext.getBodyAsJson()).thenReturn(TEST_USER_JSON);
        mockCreateUserCall(TEST_USER_JSON, false);

        putUserHandler.handle(mockRoutingContext);

        String expectedJson = new JsonObject()
                .put("success", false)
                .put("error", "Internal server error")
                .encode();
        verify(mockHttpServerResponse).end(expectedJson);
    }

    public void mockCreateUserCall(JsonObject userJson, boolean isSuccess) {
        Future<Void> future;
        if (isSuccess){
            future = Future.succeededFuture();
        } else {
            future = Future.failedFuture(TestUtils.createThrowable(TEST_ERROR_MSG));
        }
        doAnswer(invocationOnMock -> {
            Handler<AsyncResult<Void>> handler = invocationOnMock.getArgument(1);
            handler.handle(future);
            return null;
        }).when(mockUserDatabaseService).createUser(eq(userJson), any(Handler.class));
    }

}