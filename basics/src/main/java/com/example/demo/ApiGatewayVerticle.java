package com.example.demo;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.circuitbreaker.HystrixMetricHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;

import java.util.List;

public class ApiGatewayVerticle extends AbstractVerticle implements ServiceDiscover {

    CircuitBreaker breaker;

    @Override
    public void start() throws Exception {

    }

    private void createServiceMetrices(Router router) {

    }

    private void fetchServices(Router router) {

    }

    private void createCircuitBreaker() {

    }

    @Override
    public String getEventBusAddress() {
        return "api.gateway";
    }

    private void createHealthCheckProcedure() {

    }
}
