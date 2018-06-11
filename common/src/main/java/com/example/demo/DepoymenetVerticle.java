package com.example.demo;

import io.vertx.core.*;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.MessageSource;

public abstract class DepoymenetVerticle extends AbstractVerticle implements HazelcastCluster {
    protected Record record;
    protected ServiceDiscovery discovery;

    @Override
    public final void start() {
        // Customize the configuration
        initHazelcastCluster();
    }

    @Override
    public void stop() throws Exception {
        discovery.unpublish(record.getRegistration(), result -> {
            if (result.failed()) {
                System.out.println("unblushing service failed " + getServiceName());
            } else {
                System.out.println("unblushing service success " + getServiceName());
            }
        });
    }

    public void deployVerticle(Vertx vertx) {
        vertx.deployVerticle(getVerticleClass().getName(), getDeploymentOptions());
        System.out.println("Verticle " + getServiceName() + " deployed");
    }

    public void createServiceDiscovery(Vertx vertx) {
        discovery = ServiceDiscovery.create(vertx);
        record = MessageSource.createRecord(getServiceName(), getEventBusAddress());
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
    }

    protected abstract String getServiceName();

    protected abstract String getEventBusAddress();

    protected abstract Class getVerticleClass();

    protected DeploymentOptions getDeploymentOptions() {
        return new DeploymentOptions();
    }
}
