package com.alexco.simplevertxservice.user;

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
    public static final String CONFIG_HTTP_PORT = "http.server.port";
    private static final Logger LOGGER = LoggerFactory.getLogger(UserHttpServerVerticle.class);

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Router router = setUserRoutes(Router.router(vertx));

        int port = config().getInteger(CONFIG_HTTP_PORT, 8080);
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, asyncResult -> {
                    if (asyncResult.succeeded()) {
                        LOGGER.info("Running HTTP Server on port 8080");
                        startFuture.complete();
                    } else {
                        LOGGER.error("Could not start HTTP Server", asyncResult.cause());
                        startFuture.fail(asyncResult.cause());
                    }
                });
    }

    private Router setUserRoutes(Router router) {
        router.get("/user/:id").handler(GetUserHandler.getInstance());
        router.put().handler(BodyHandler.create());
        router.put("/user/:id").handler(PutUserHandler.getInstance());
        return router;
    }
}
