package com.example.demo.verticlemanager;

import io.vertx.core.Vertx;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;

public interface ServiceDiscoveryManager {
    default void unPublish(ServiceDiscovery discovery, Record record){
        discovery.unpublish(record.getRegistration(), result -> {
            if (result.failed()) {
                System.out.println("unblushing service failed " + record.getName());
            } else {
                System.out.println("unblushing service success " + record.getName());
            }
        });

        discovery.close();
    }

    default void createServiceDiscovery(Vertx vertx) {

        ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
        Record record = EventBusService.createRecord(getServiceName(), getEventBusAddress(),this.getClass());
        discovery.publish(record, ar -> {
            if (ar.succeeded()) {
                // publication succeeded
                System.out.println("Service record published successfully " + getServiceName()
                        + " on address " + getEventBusAddress());
            } else {
                // publication failed
                System.out.println("Service record publication failed " + getServiceName()
                        + " on address " + getEventBusAddress());
            }
        });
        onServiceDiscovery(vertx,discovery,record);
    }

    String getServiceName();

    String getEventBusAddress();

    void onServiceDiscovery(Vertx vertx,ServiceDiscovery discovery,Record record);
}
