package com.example.demo.verticle;

import com.example.demo.verticlemanager.ClusterDeploymentManager;
import com.example.demo.verticlemanager.ServiceDiscoveryManager;
import io.vertx.core.*;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

public abstract class DepoymenetVerticle extends AbstractVerticle implements ClusterDeploymentManager, ServiceDiscoveryManager {
    protected Record record;
    protected ServiceDiscovery discovery;

    @Override
    public final void start() {
        // Customize the configuration
        initHazelcastCluster();
        ServiceRecordTuple<Record,ServiceDiscovery> tuple = createServiceDiscovery(vertx);
        this.record = tuple.getRecoed();
        this.discovery = tuple.getDiscovery();
    }

    @Override
    public void stop() throws Exception {
        unPublish();
    }

    @Override
    public Record getRecord() {
        return record;
    }

    @Override
    public ServiceDiscovery getDiscovery() {
        return discovery;
    }


    public void onclustredVerticle(Vertx vertx) {
        this.vertx = vertx;
        vertx.deployVerticle(getVerticleClass().getName(), getDeploymentOptions(),event -> {
            if(event.failed()){
                System.out.println("failed deployment of verticle"+getVerticleClass().getName());
            }else {
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
    protected abstract Class getVerticleClass();


}
