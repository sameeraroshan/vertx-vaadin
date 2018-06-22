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
        deployVerticle(ApiGatewayVerticle.class, "Api Gateway");
        deployVerticle(ReplyServerVerticle.class, "Reply service1");
        deployVerticle(ReplyServerVerticle.class, "Reply service2");
    }


    static void deployVerticle(Class verticleClass, String name) {
        VertxOptions vertxOptions = new VertxOptions();
        HazelcastClusterManager clusterManager = new HazelcastClusterManager();
        vertxOptions.setClusterManager(clusterManager);
        vertxOptions.setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true));
        Vertx.clusteredVertx(vertxOptions, clustred -> {
            if (clustred.succeeded()) {
                Vertx vertx = clustred.result();
                System.out.println("We now have a clustered event bus: " + vertx.eventBus());
                vertx.deployVerticle(
                        verticleClass.getName(),
                        res -> {
                            if (res.succeeded()) {
                                if (res.succeeded()) {
                                    String deploymentID = res.result();
                                    System.out.println("reply " + name + " deployed ok, deploymentID = " + deploymentID);
                                } else {
                                    res.cause().printStackTrace();
                                }
                            } else {
                                res.cause().printStackTrace();
                            }
                        });
            } else {
                System.out.println("Failed: " + clustred.cause());
            }
        });
    }

}
