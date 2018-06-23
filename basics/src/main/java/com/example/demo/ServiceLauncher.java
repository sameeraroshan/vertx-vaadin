package com.example.demo;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class ServiceLauncher {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("vertx.hazelcast.config", "classpath:hazelcast-configurations.xml");

    }

    static void deployVerticle(Class verticleClass, String name) {

    }
}
