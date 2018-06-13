package com.eample.demo.services;

import com.example.demo.verticle.DepoymenetVerticle;
import com.example.demo.constant.Endpoints;
import com.example.demo.verticlemanager.ServiceDiscoveryManager;
import io.vertx.circuitbreaker.HystrixMetricHandler;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MicroServicesListener extends DepoymenetVerticle implements ServiceDiscoveryManager {

    private static final MicroServicesListener listener = new MicroServicesListener();
    private static final HashMap<String, ArrayList<Handler>> handlers = new HashMap();

    private MicroServicesListener() {
        initHazelcastCluster();
    }

    @Override
    protected Class getVerticleClass() {
        return this.getClass();
    }

    @Override
    public void onclustredVerticle(Vertx vertx) {
        vertx.eventBus().consumer(Endpoints.QUOTE_SERICE, message -> {
            List<Handler> handlers = this.handlers.get(Endpoints.QUOTE_SERICE);
            if (handlers != null) {
                handlers.forEach(m -> m.handle(message));
            }
        });

        vertx.eventBus().consumer(Endpoints.METRICS_SERICE, message -> {
            List<Handler> handlers = this.handlers.get(Endpoints.METRICS_SERICE);
            if (handlers != null) {
                handlers.forEach(m -> m.handle(message));
            }
        });

        vertx.setPeriodic(5000, event -> {
            List<Handler> handlers = this.handlers.get(Endpoints.RECORD_SERVICE);
            if (handlers != null) {
                getDiscovery().getRecords(record1 -> true, true, result -> {
                    System.out.println("Record" + getRecord());
                    if (!result.failed()) {
                        handlers.forEach(m -> m.handle(result.result()));
                    }
                });
            }
        });

        MetricsService service = MetricsService.create(vertx);
        HealthCheckHandler hc = HealthCheckHandler.create(vertx);
        hc.register("metrics-handler", future -> future.complete(Status.OK(service.getMetricsSnapshot(vertx))));

        // Register the metric handler
        Router router = Router.router(vertx);
        router.get("/hystrix_metrics").handler(HystrixMetricHandler.create(vertx));
        router.get("/dropwizard-metrics").handler(hc);
        vertx.createHttpServer().requestHandler(router::accept).listen(8090);
    }

    public void subscribe(String address, Handler handler) {
        if (this.handlers.get(address) == null) {
            this.handlers.put(address, new ArrayList<>());
        }
        this.handlers.get(address).add(handler);
    }

    public void unSubscribe(String address, Handler handler) {
        if (this.handlers.get(address) == null) {
            this.handlers.get(address).remove(handler);
        }
    }

    public static MicroServicesListener getListener() {
        return listener;
    }

    @Override
    public String getServiceName() {
        return "Vaadin-UI";
    }

    @Override
    public String getEventBusAddress() {
        return Endpoints.UI_DATA_SERVICE;
    }
}
