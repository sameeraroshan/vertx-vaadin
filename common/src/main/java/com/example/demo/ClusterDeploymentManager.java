package com.example.demo;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.circuitbreaker.HystrixMetricHandler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.web.Router;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.MessageSource;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public interface ClusterDeploymentManager {

    default void initHazelcastCluster() {
        setDefaultProperties();
        clusterEventBus();
    }

    default void setDefaultProperties() {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("vertx.hazelcast.config", "classpath:hazelcast-configurations.xml");
    }

    default VertxOptions getOptions() {

        HazelcastClusterManager clusterManager = new HazelcastClusterManager();
        VertxOptions options = new VertxOptions();
        options.setClusterManager(clusterManager);
        options.setClustered(true);
        options.setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true));
        return options;
    }


    default void clusterEventBus() {

        Vertx.clusteredVertx(getOptions(), res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                System.out.println("We now have a clustered event bus: " + vertx.eventBus());
                onclustredVerticle(vertx);
                sendMetrics(vertx);
            } else {
                System.out.println("Failed: " + res.cause());
            }
        });
    }

    default void sendMetrics(Vertx vertx) {
        MetricsService service = MetricsService.create(vertx);
        vertx.setPeriodic(2000, t -> {
            JsonObject metrics = service.getMetricsSnapshot(vertx);
            vertx.eventBus().publish(Endpoints.METRICS_SERICE, metrics);
        });
      /*  // Register the metric handler
        Router router = Router.router(vertx);
        router.get("/hystrix-metrics").handler(HystrixMetricHandler.create(vertx));
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8090);*/
    }

    void onclustredVerticle(Vertx vertx);

}
