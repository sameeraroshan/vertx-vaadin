package com.example.demo;

import io.vertx.core.Vertx;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;

public interface ServiceDiscoveryManager {
    default void unPublish(){
        getDiscovery().unpublish(getRecord().getRegistration(), result -> {
            if (result.failed()) {
                System.out.println("unblushing service failed " + getRecord().getName());
            } else {
                System.out.println("unblushing service success " + getRecord().getName());
            }
        });

        getDiscovery().close();
    }

    default ServiceRecordTuple<Record,ServiceDiscovery>  createServiceDiscovery(Vertx vertx) {

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

        ServiceRecordTuple tuple = new ServiceRecordTuple() {
            @Override
            public Object getRecoed() {
                return record;
            }

            @Override
            public Object getDiscovery() {
                return discovery;
            }
        };

        return tuple;
    }

    Record getRecord();

    ServiceDiscovery getDiscovery();

    String getServiceName();

    String getEventBusAddress();

    interface ServiceRecordTuple<T,V>{
        T getRecoed();
        V getDiscovery();
    }
}
