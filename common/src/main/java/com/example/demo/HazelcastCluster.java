package com.example.demo;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
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
        return options;
    }


    default void clusterEventBus() {

        Vertx.clusteredVertx(getOptions(), res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                System.out.println("We now have a clustered event bus: " + vertx.eventBus());
                onclustredVerticle(vertx);
                createServiceDiscovery(vertx);
            } else {
                System.out.println("Failed: " + res.cause());
            }
        });
    }

    void createServiceDiscovery(Vertx vertx);

    void onclustredVerticle(Vertx vertx);

}
