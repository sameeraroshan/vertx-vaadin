package com.example.demo;

import io.vertx.core.Vertx;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;

public interface ServiceDiscover {

    default void createServiceDiscovery(Vertx vertx, String name) {

    }

    String getEventBusAddress();
}
