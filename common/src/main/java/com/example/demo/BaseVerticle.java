package com.example.demo;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;

public abstract class BaseVerticle extends AbstractVerticle implements ServiceDiscoveryManager {
    protected Record record;
    protected ServiceDiscovery discovery;

    @Override
    public void start() {
        ServiceRecordTuple<Record,ServiceDiscovery> tuple = createServiceDiscovery(vertx);
        this.record = tuple.getRecoed();
        this.discovery = tuple.getDiscovery();
        createHealthCheck(vertx);
    }

    private void createHealthCheck(Vertx vertx) {
        HealthChecks hc = HealthChecks.create(vertx);
        hc.register("health-check-procesure", 1000, future -> future.complete(Status.OK()));
       // vertx.eventBus().consumer(getEventBusAddress()+"health",hc);
    }

    @Override
    public void stop() throws Exception {
        unPublish();
    }

    @Override
    public Record getRecord() {
        return record;
    }

    @Override
    public ServiceDiscovery getDiscovery() {
        return discovery;
    }

    abstract void createCircuitBreaker(Vertx vertx);


}
