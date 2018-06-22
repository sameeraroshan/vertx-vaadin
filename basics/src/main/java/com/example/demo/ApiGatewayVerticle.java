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
        Router router = Router.router(vertx);
        HttpServer server = getVertx().createHttpServer();
        server.requestHandler(router::accept).listen(8080);
        createServiceDiscovery(vertx, "Api gateway");
        createCircuitBreaker();
        fetchServices(router);
        createServiceMetrices(router);
        createHealthCheckProcedure();

        router.get("/replyserver/:name").handler(routingContext -> {
            //get parameter name
            String name = routingContext.request().getParam("name");
            //send message to reply server

            breaker.executeWithFallback(
                    future -> {
                        vertx.eventBus().<String>send("vertx.reply.server", name, event -> {
                            if (event.succeeded()) {
                                future.complete(event.result().body());
                            } else {
                                //routingContext.response().setChunked(true).write().end();
                                future.fail("Sending messaged failed");
                            }
                        });
                    }, v -> {
                        // Executed when the circuit is opened
                        return "Sending messaged failed circuit open";
                    })
                    .setHandler(ar -> {
                        // Do something with the result
                        routingContext.response()
                                .setChunked(true).write(ar.result()).end();
                    });
        });
    }

    private void createServiceMetrices(Router router) {
        MetricsService service = MetricsService.create(vertx);
        HealthCheckHandler hc = HealthCheckHandler.create(vertx);
        hc.register("metrics-handler", future -> future.complete(Status.OK(service.getMetricsSnapshot(vertx))));
        // Register the metric handler
        router.get("/hystrix_metrics").handler(HystrixMetricHandler.create(vertx));
        router.get("/dropwizard-metrics").handler(hc);
    }

    private void fetchServices(Router router) {
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
        router.get("/services/").handler(event -> {
            discovery.getRecords(all -> true, true, result -> {
                if (!result.failed()) {
                    HttpServerResponse response = event.response();
                    response.putHeader("content-type", "text/html");
                    result.result().forEach(record -> {
                        response.setChunked(true).write(record.getName() + record.getRegistration());
                        response.write("</br>");
                    });
                    response.end();
                }
            });
        });
    }

    private void createCircuitBreaker() {
        breaker = CircuitBreaker.create("vertx.reply.server.breaker", vertx, new CircuitBreakerOptions()
                .setMaxFailures(10)
                .setTimeout(1000)
                .setFallbackOnFailure(true)
                .setMaxRetries(5)
                .setResetTimeout(50000));
    }

    @Override
    public String getEventBusAddress() {
        return "api.gateway";
    }

    private void createHealthCheckProcedure() {
        HealthChecks healthChecks = HealthChecks.create(vertx);
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx);

        discovery.getRecords(all -> true, true, result -> {
            if (!result.failed()) {
                result.result().forEach(record -> {
                    healthChecks.register(record.getLocation().getString("endpoint"), response -> {
                        vertx.eventBus().send(record.getLocation().getString("endpoint") + ".health", "", event -> {
                            if (event.failed()) {
                                response.complete(Status.KO());
                            } else {
                                response.complete(Status.OK());
                            }
                        });
                    });
                });
            }
        });

        vertx.setPeriodic(1000,event -> {
            healthChecks.invoke(e -> {
                e.getJsonArray("checks").forEach(reference -> {
                    String location = ((JsonObject) reference).getString("id");
                    String status = ((JsonObject) reference).getString("status");
                    System.out.println("Location:"+location);
                    System.out.println("status:"+status);
                });
            });
        });
    }
}
