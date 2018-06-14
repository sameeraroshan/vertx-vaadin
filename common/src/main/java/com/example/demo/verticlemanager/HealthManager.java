package com.example.demo.verticlemanager;

import io.vertx.core.Vertx;

public interface HealthManager {

   default void createHealthEndpoint(Vertx vertx) {
        vertx.eventBus().consumer(getEventBusAddress()+".health").handler(event -> {
            event.reply("OK");
        });
    }

     String getEventBusAddress();
}
