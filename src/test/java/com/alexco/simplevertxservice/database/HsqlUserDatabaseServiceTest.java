package com.alexco.simplevertxservice.database;

import com.alexco.simplevertxservice.TestUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.Is.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@ExtendWith(MockitoExtension.class)
class HsqlUserDatabaseServiceTest {

    @Mock
    JDBCClient mockJdbcClient;

    @Mock
    SQLConnection mockSqlConnection;

    private Future<UserDatabaseService> handler;
    private HsqlUserDatabaseService hsqlUserDatabaseService;

    @BeforeEach
    void setup() {
        mockJdbcClientGetConnectionCallback(Future.succeededFuture(mockSqlConnection));
        mockSqlConnectionExecuteCallback(Future.succeededFuture());
        handler = Future.future();
        hsqlUserDatabaseService = new HsqlUserDatabaseService(mockJdbcClient, handler);
    }

    @Test
    void constructor_happyPath_getsConnectionAndCreatesTable() {
        verify(mockJdbcClient).getConnection(any(Handler.class));
        verify(mockSqlConnection).execute(eq(HsqlUserDatabaseService.SQL_CREATE_USER_TABLE), any(Handler.class));
    }

    @Test
    void constructor_happyPath_closesTheConnection() {
        verify(mockSqlConnection).close();
    }

    @Test
    void constructor_happyPath_theGivenHandlerSucceedsWithTheService() {
        assertThat(handler.succeeded(), is(true));
        assertThat(handler.result(), is(hsqlUserDatabaseService));
    }

    @Test
    void constructor_getSqlConnectionFails_theGivenHandlerFailsWithCause() {
        reset(mockJdbcClient, mockSqlConnection); // b/c the setup() function sets up the happy path constructor
        mockJdbcClientGetConnectionCallback(Future.failedFuture(TestUtils.createThrowable("test-exception")));

        handler = Future.future();
        hsqlUserDatabaseService = new HsqlUserDatabaseService(mockJdbcClient, handler);

        assertThat(handler.failed(), is(true));
        assertThat(handler.cause(), isA(RuntimeException.class));
        verifyZeroInteractions(mockSqlConnection);
    }

    @Test
    void constructor_executeCreateTableFails_theGivenHandlerFailsWithCause() {
        mockSqlConnectionExecuteCallback(Future.failedFuture(TestUtils.createThrowable("test-exception")));

        handler = Future.future();
        hsqlUserDatabaseService = new HsqlUserDatabaseService(mockJdbcClient, handler);

        assertThat(handler.failed(), is(true));
        assertThat(handler.cause(), isA(RuntimeException.class));
    }

    @Test
    void constructor_executeCreateTableFails_closesTheSqlConnection() {
        reset(mockSqlConnection); // b/c the setup() function sets up the happy path constructor
        mockSqlConnectionExecuteCallback(Future.failedFuture(TestUtils.createThrowable("test-exception")));

        handler = Future.future();
        hsqlUserDatabaseService = new HsqlUserDatabaseService(mockJdbcClient, handler);

        verify(mockSqlConnection).close();
    }


    private void mockJdbcClientGetConnectionCallback(Future<SQLConnection> future) {
        doAnswer(invocationOnMock -> {
            Handler<AsyncResult<SQLConnection>> callback = invocationOnMock.getArgument(0);
            callback.handle(future);
            return null;
        }).when(mockJdbcClient).getConnection(any(Handler.class));
    }

    private void mockSqlConnectionExecuteCallback(Future<Void> future) {
        doAnswer(invocationOnMock -> {
            Handler<AsyncResult<Void>> callback = invocationOnMock.getArgument(1);
            callback.handle(future);
            return null;
        }).when(mockSqlConnection).execute(anyString(), any(Handler.class));
    }

}