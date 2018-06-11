package com.example.demo;

import com.hazelcast.config.Config;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.MessageSource;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class DepoymenetVerticle extends AbstractVerticle {
    protected Record record;
    protected ServiceDiscovery discovery;

    @Override
    public final void start() {
        // Customize the configuration
        clusterEventBus();
    }

   protected void clusterEventBus() {

        //Config config = new Config();

        try {
            InetAddress ip=InetAddress.getLocalHost();
           // config.getNetworkConfig().getJoin().getMulticastConfig().addTrustedInterface(ip.getHostAddress());
            HazelcastClusterManager clusterManager = new HazelcastClusterManager();
           // ClusterManager clusterManager = new HazelcastClusterManager(config);
            VertxOptions options = new VertxOptions();
            //options.setClusterHost(ip.getHostAddress());
            options.setClusterManager(clusterManager);
            options.setClustered(true);
            Vertx.clusteredVertx(options, res -> {
                if (res.succeeded()) {
                    Vertx vertx = res.result();
                    EventBus eventBus = vertx.eventBus();
                    System.out.println("We now have a clustered event bus: " + eventBus);
                    deployVerticle(vertx);
                    createServiceDiscovery(vertx);
                } else {
                    System.out.println("Failed: " + res.cause());
                }
            });
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        discovery.unpublish(record.getRegistration(), result -> {
            if (result.failed()) {
                System.out.println("unblushing service failed " + getServiceName());
            } else {
                System.out.println("unblushing service success " + getServiceName());
            }
        });
    }

    protected void deployVerticle(Vertx vertx) {
        vertx.deployVerticle(getVerticleClass().getName(), getDeploymentOptions());
        System.out.println("Verticle " + getServiceName() + " deployed");
    }

    protected void createServiceDiscovery(Vertx vertx) {
        discovery = ServiceDiscovery.create(vertx);
        record = MessageSource.createRecord(getServiceName(), getEventBusAddress());
        discovery.publish(record, ar -> {
            if (ar.succeeded()) {
                // publication succeeded
                System.out.println("Service record published successfully " + getServiceName()
                        + " on address " + getEventBusAddress());
            } else {
                // publication failed
                System.out.println("Service record publication failed " + getServiceName()
                        + " on address " + getEventBusAddress());
            }
        });
    }

    protected abstract String getServiceName();

    protected abstract String getEventBusAddress();

    protected abstract Class getVerticleClass();

    protected DeploymentOptions getDeploymentOptions() {
        return new DeploymentOptions();
    }
}
