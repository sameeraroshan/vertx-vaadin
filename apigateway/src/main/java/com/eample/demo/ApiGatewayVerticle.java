package com.eample.demo;

import com.example.demo.verticle.BaseVerticle;
import com.example.demo.constant.Endpoints;
import io.vertx.circuitbreaker.HystrixMetricHandler;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ApiGatewayVerticle extends BaseVerticle {

    Router router;
    HealthChecks healthChecks;

    @Override
    public void start() {
        super.start();
        router = Router.router(vertx);
        createCrossHandler();
        createSockJsEndpoint();
        createStaticHandler();
        createMetrixMonitoring();
        broadcastQuoteService();
    }

    private void createStaticHandler() {

        router.route().handler(StaticHandler.create()
                .setAllowRootFileSystemAccess(false).setCachingEnabled(false)
                .setCachingEnabled(true).setWebRoot("angular" + File.separator + "dist" + File.separator + "epic"));
    }


    private void createSockJsEndpoint() {
        SockJSHandlerOptions handlerOptions = new SockJSHandlerOptions().setHeartbeatInterval(2000);
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, handlerOptions);
        BridgeOptions options = new BridgeOptions();
        options.addInboundPermitted(new PermittedOptions().setAddress("websocket.data.service"));
        options.addOutboundPermitted(new PermittedOptions().setAddress("websocket.data.service"));
        sockJSHandler.bridge(options, event -> {
            System.out.println("A websocket event occurred: " + event.type() + "; " + event.getRawMessage());
            event.complete(true);
        });
        router.route("/eventbus/*").handler(sockJSHandler);
    }

    private void createMetrixMonitoring() {
        MetricsService service = MetricsService.create(vertx);
        HealthCheckHandler hc = HealthCheckHandler.create(vertx);
        hc.register("metrics-handler", future -> future.complete(Status.OK(service.getMetricsSnapshot(vertx))));
        router.get("/hystrix_metrics").handler(HystrixMetricHandler.create(vertx));
        router.get("/dropwizard-metrics").handler(hc);
        vertx.createHttpServer().requestHandler(router::accept).listen(8081);
    }


    @Override
    public void createCircuitBreaker(Vertx vertx) {

    }

    @Override
    public String getEventBusAddress() {
        return Endpoints.API_GATEWAY;
    }


    private void broadcastQuoteService() {
        vertx.eventBus().consumer(Endpoints.QUOTE_SERICE, message -> {
            //broadcast using new address
            vertx.eventBus().send("websocket.data.service", message.body());
        });
    }


    /*@Override
    public void onServiceDiscovery(Vertx vertx, ServiceDiscovery discovery, Record r) {
        super.onServiceDiscovery(vertx, discovery, r);
        healthChecks = HealthChecks.create(vertx);
        vertx.setPeriodic(5000, event -> {
            List<Handler> handlers = this.handlers.get(Endpoints.RECORD_SERVICE);
            if (handlers != null) {
                discovery.getRecords(record1 -> true, true, result -> {
                    if (!result.failed()) {
                        result.result().forEach(record -> {
                            if (recordMap.get(record.getLocation().getString("endpoint")) == null) {
                                recordMap.put(record.getLocation().getString("endpoint"), record);
                                ServiceReference reference = discovery.getReference(record);
                                createHealthCheckProcedure(vertx, reference, record, handlers);
                            }
                        });
                    }
                });
            }

            healthChecks.invoke(e -> {
                e.getJsonArray("checks").forEach(reference -> {
                    String location = ((JsonObject) reference).getString("id");
                    String status = ((JsonObject) reference).getString("status");
                    Record record = recordMap.get(location);
                    if ("UP".equals(status)) {
                        record.setStatus(io.vertx.servicediscovery.Status.UP);
                    } else {
                        record.setStatus(io.vertx.servicediscovery.Status.DOWN);
                    }

                    handlers.forEach(m -> m.handle(record));
                });
            });
        });
    }

    private void createHealthCheckProcedure(Vertx vertx, ServiceReference serviceReference, Record record, List<Handler> handlers) {

        healthChecks.register(record.getLocation().getString("endpoint"), response -> {
            *//* EventBus eb = serviceReference.get();*//*
            vertx.eventBus().send(record.getLocation().getString("endpoint") + ".health", "", event -> {
                if (event.failed()) {
                    response.complete(Status.KO());
                } else {
                    response.complete(Status.OK());
                }
            });
        });


    }*/

    private void createCrossHandler() {
        this.router.route().handler(CorsHandler.create("*")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.PUT)
                .allowedMethod(HttpMethod.PATCH)
                .allowedMethod(HttpMethod.DELETE)
                .allowedMethod(HttpMethod.OPTIONS)
                .allowedHeader("Access-Control-Request-Methods")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Authorization")
                .allowedHeader("enctype")
                .allowedHeader("Content-Type"));
    }

}
