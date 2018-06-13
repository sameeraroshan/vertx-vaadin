package com.example.demo.verticle;

import com.example.demo.verticlemanager.ServiceDiscoveryManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

public abstract class BaseVerticle extends AbstractVerticle  {

    @Override
    public void start() {
        createCircuitBreaker(vertx);
        createHealthCheck(vertx);
    }

    private void createHealthCheck(Vertx vertx) {
        HealthChecks hc = HealthChecks.create(vertx);
        hc.register("health-check-procesure", 1000, future -> future.complete(Status.OK()));
       // vertx.eventBus().consumer(getEventBusAddress()+"health",hc);
    }


    public abstract void createCircuitBreaker(Vertx vertx);


}
