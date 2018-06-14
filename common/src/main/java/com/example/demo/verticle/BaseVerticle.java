package com.example.demo.verticle;

import com.example.demo.verticlemanager.HealthManager;
import com.example.demo.verticlemanager.ServiceDiscoveryManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

public abstract class BaseVerticle extends AbstractVerticle implements HealthManager {

    @Override
    public void start() {
        createCircuitBreaker(vertx);
        createHealthEndpoint(vertx);
    }

    public abstract void createCircuitBreaker(Vertx vertx);

}
