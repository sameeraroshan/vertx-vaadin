package com.example.demo;

import io.vertx.core.*;

public abstract class DepoymenetVerticle extends AbstractVerticle implements ClusterDeploymentManager {


    @Override
    public final void start() {
        // Customize the configuration
        initHazelcastCluster();
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

    }

    protected DeploymentOptions getDeploymentOptions() {
        return new DeploymentOptions();
    }
    protected abstract Class getVerticleClass();


}
