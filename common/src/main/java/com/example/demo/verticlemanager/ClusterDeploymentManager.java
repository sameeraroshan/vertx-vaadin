package com.example.demo.verticlemanager;

import com.example.demo.constant.Endpoints;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public interface ClusterDeploymentManager extends ServiceDiscoveryManager {

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

    default void sendMetrics(Vertx vertx) {
        MetricsService service = MetricsService.create(vertx);
        vertx.setPeriodic(2000, t -> {
            JsonObject metrics = service.getMetricsSnapshot(vertx);
            vertx.eventBus().publish(Endpoints.METRICS_SERICE, metrics);
        });
    }

    void onclustredVerticle(Vertx vertx);

}
