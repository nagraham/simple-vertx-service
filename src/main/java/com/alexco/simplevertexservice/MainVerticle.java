package com.alexco.simplevertexservice;

import com.alexco.simplevertexservice.user.GetUserHandler;
import com.alexco.simplevertexservice.user.PutUserHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        startHttpServer().setHandler(asyncResult -> {
            if (asyncResult.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(asyncResult.cause());
            }
        });
    }

    private Future<Void> startHttpServer() {
        Future<Void> future = Future.future();

        Router router = setUserRoutes(Router.router(vertx));

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, asyncResult -> {
                    if (asyncResult.succeeded()) {
                        LOGGER.info("Running HTTP Server on port 8080");
                        future.complete();
                    } else {
                        LOGGER.error("Could not start HTTP Server", asyncResult.cause());
                        future.fail(asyncResult.cause());
                    }
                });

        return future;
    }

    private Router setUserRoutes(Router router) {
        router.get("/user/:id").handler(GetUserHandler.getInstance());
        router.put().handler(BodyHandler.create());
        router.put("/user/:id").handler(PutUserHandler.getInstance());
        return router;
    }
}
