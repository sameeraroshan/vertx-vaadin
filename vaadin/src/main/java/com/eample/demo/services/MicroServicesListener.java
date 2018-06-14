package com.eample.demo.services;

import com.example.demo.verticle.DepoymenetVerticle;
import com.example.demo.constant.Endpoints;
import com.example.demo.verticlemanager.HealthManager;
import io.vertx.circuitbreaker.HystrixMetricHandler;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MicroServicesListener extends DepoymenetVerticle implements HealthManager {

    private static final MicroServicesListener listener = new MicroServicesListener();
    private static final HashMap<String, ArrayList<Handler>> handlers = new HashMap();
    HealthChecks healthChecks;
    Router router;
    Map<String, Record> recordMap = new HashMap<>();

    private MicroServicesListener() {
        initHazelcastCluster();
    }

    @Override
    protected Class getVerticleClass() {
        return this.getClass();
    }

    @Override
    public void onclustredVerticle(Vertx vertx) {
        consumeQuoteService(vertx);
        consumeMetricService(vertx);
        createMetrixMonitoring(vertx);
        createHealthEndpoint(vertx);
    }

    private void createMetrixMonitoring(Vertx vertx) {
        MetricsService service = MetricsService.create(vertx);
        HealthCheckHandler hc = HealthCheckHandler.create(vertx);
        hc.register("metrics-handler", future -> future.complete(Status.OK(service.getMetricsSnapshot(vertx))));

        // Register the metric handler
        Router router = Router.router(vertx);
        router.get("/hystrix_metrics").handler(HystrixMetricHandler.create(vertx));
        router.get("/dropwizard-metrics").handler(hc);
        vertx.createHttpServer().requestHandler(router::accept).listen(8090);
    }

    private void consumeQuoteService(Vertx vertx) {
        vertx.eventBus().consumer(Endpoints.QUOTE_SERICE, message -> {
            List<Handler> handlers = this.handlers.get(Endpoints.QUOTE_SERICE);
            if (handlers != null) {
                handlers.forEach(m -> m.handle(message));
            }
        });
    }

    private void consumeMetricService(Vertx vertx) {
        vertx.eventBus().consumer(Endpoints.METRICS_SERICE, message -> {
            List<Handler> handlers = this.handlers.get(Endpoints.METRICS_SERICE);
            if (handlers != null) {
                handlers.forEach(m -> m.handle(message));
            }
        });
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

    @Override
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
                    if("UP".equals(status)){
                        record.setStatus(io.vertx.servicediscovery.Status.UP);
                    }else {
                        record.setStatus(io.vertx.servicediscovery.Status.DOWN);
                    }

                    handlers.forEach(m -> m.handle(record));
                });
            });
        });
    }

    private void createHealthCheckProcedure(Vertx vertx, ServiceReference serviceReference, Record record, List<Handler> handlers) {

        healthChecks.register(record.getLocation().getString("endpoint"), response -> {
            /* EventBus eb = serviceReference.get();*/
            vertx.eventBus().send(record.getLocation().getString("endpoint") + ".health", "", event -> {
                if (event.failed()) {
                    response.complete(Status.KO());
                } else {
                    response.complete(Status.OK());
                }
            });
        });


    }
}
