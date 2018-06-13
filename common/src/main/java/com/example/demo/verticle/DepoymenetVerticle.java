package com.example.demo.verticle;

import com.example.demo.verticlemanager.ClusterDeploymentManager;
import com.example.demo.verticlemanager.ServiceDiscoveryManager;
import io.vertx.core.*;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

public abstract class DepoymenetVerticle extends AbstractVerticle implements ClusterDeploymentManager {
    ServiceDiscovery discovery;
    Record record;

    @Override
    public final void start() {
        // Customize the configuration
        initHazelcastCluster();
    }

    @Override
    public void stop() throws Exception {
        unPublish(discovery, record);
    }

    public void onclustredVerticle(Vertx vertx) {
        this.vertx = vertx;
        vertx.deployVerticle(getVerticleClass().getName(), getDeploymentOptions(), event -> {
            if (event.failed()) {
                System.out.println("failed deployment of verticle" + getVerticleClass().getName());
            } else {
                System.out.println("Verticle " + getVerticleClass().getName() + " deployed");
            }
        });

        HealthCheckHandler hc = HealthCheckHandler.create(vertx);
        MetricsService service = MetricsService.create(vertx);
        hc.register(getVerticleClass().getName(), future -> future.complete(Status.OK(service.getMetricsSnapshot(vertx))));
    }

    protected DeploymentOptions getDeploymentOptions() {
        return new DeploymentOptions();
    }

    @Override
    public void onServiceDiscovery(Vertx vertx,ServiceDiscovery discovery, Record record) {
        this.vertx = vertx;
        this.discovery = discovery;
        this.record = record;
    }

    protected abstract Class getVerticleClass();


}
