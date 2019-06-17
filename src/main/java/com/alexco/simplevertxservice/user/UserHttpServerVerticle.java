package com.alexco.simplevertxservice.user;

import com.alexco.simplevertxservice.database.UserDatabaseService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * A verticle encapsulating the HTTP server for the User REST API
 */
public class UserHttpServerVerticle extends AbstractVerticle {
    public static final String CONFIG_PORT = "port";
    private static final Logger LOGGER = LoggerFactory.getLogger(UserHttpServerVerticle.class);

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        UserDatabaseService userDatabaseService = UserDatabaseService.createProxy(vertx, "user.queue");
        Router router = setUserRoutes(Router.router(vertx), userDatabaseService);

        int port = config().getJsonObject("http").getInteger(CONFIG_PORT);
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(port, asyncResult -> {
                    if (asyncResult.succeeded()) {
                        LOGGER.info("Running HTTP Server on port 8080");
                        startFuture.complete();
                    } else {
                        LOGGER.error("Could not start HTTP Server", asyncResult.cause());
                        startFuture.fail(asyncResult.cause());
                    }
                });
    }

    private Router setUserRoutes(Router router, UserDatabaseService userDatabaseService) {
        router.get("/user/:id").handler(GetUserHandler.getInstance(userDatabaseService));
        router.put().handler(BodyHandler.create());
        router.put("/user/:id").handler(PutUserHandler.getInstance(userDatabaseService));
        return router;
    }
}
