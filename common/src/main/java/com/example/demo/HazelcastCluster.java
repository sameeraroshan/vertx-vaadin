package com.example.demo;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.MessageSource;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public interface HazelcastCluster {

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
                createServiceDiscovery(vertx);
                sendMetrics(vertx);
            } else {
                System.out.println("Failed: " + res.cause());
            }
        });
    }

    default void sendMetrics(Vertx vertx){
        MetricsService service = MetricsService.create(vertx);
        vertx.setPeriodic(2000, t -> {
            JsonObject metrics = service.getMetricsSnapshot(vertx);
            vertx.eventBus().publish(Endpoints.METRICS_SERICE, metrics);
        });
    }

  /*  void createCircuitBreaker(){
        // init circuit breaker instance
        JsonObject cbOptions = config().getJsonObject("circuit-breaker") != null ?
                config().getJsonObject("circuit-breaker") : new JsonObject();
        circuitBreaker = CircuitBreaker.create(cbOptions.getString("name", "circuit-breaker"), vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(cbOptions.getInteger("max-failures", 5))
                        .setTimeout(cbOptions.getLong("timeout", 10000L))
                        .setFallbackOnFailure(true)
                        .setResetTimeout(cbOptions.getLong("reset-timeout", 30000L))
        );
    }*/



    void onclustredVerticle(Vertx vertx);

    void createServiceDiscovery(Vertx vertx);

}
