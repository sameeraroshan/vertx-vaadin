package com.example.demo;

import io.vertx.core.Vertx;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;

public interface ServiceDiscover {

    default void createServiceDiscovery(Vertx vertx, String name) {
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
        Record record = EventBusService.createRecord(name, getEventBusAddress(), this.getClass());
        discovery.publish(record, ar -> {
            if (ar.succeeded()) {
                // publication succeeded
                System.out.println("Service record published successfully " + name
                        + " on address " + getEventBusAddress());
            } else {
                // publication failed
                System.out.println("Service record publication failed " + name
                        + " on address " + getEventBusAddress());
            }
        });
    }

    String getEventBusAddress();
}
