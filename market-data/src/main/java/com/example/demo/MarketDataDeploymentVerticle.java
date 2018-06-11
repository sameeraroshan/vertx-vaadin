package com.example.demo;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

public class MarketDataDeploymentVerticle extends DepoymenetVerticle {

    public static void main(final String[] args) {
        ServiceLauncher.executeCommand("run", MarketDataDeploymentVerticle.class.getName());
    }

    @Override
    protected void deployVerticle(Vertx vertx) {
        ConfigRetriever retriever = ConfigRetriever.create(vertx, getConfigurationOptions());
        retriever.getConfig(result -> {
            if (result.failed()) {
                System.out.println("market data config creation failed");
            } else {
                JsonObject configs = result.result();
                configs.getJsonArray("companies").forEach(company -> {
                    vertx.deployVerticle(MarketDataVerticle.class.getName(), new DeploymentOptions().setConfig((JsonObject) company));
                    System.out.println("deployed market data service: " + ((JsonObject) company).getString("name"));
                });
            }
        });
    }

    /**
     * This method is called when the verticle is deployed.
     */
    private ConfigRetrieverOptions getConfigurationOptions() {
        JsonObject path = new JsonObject().put("path", "config/config.json");
        return new ConfigRetrieverOptions().addStore(new ConfigStoreOptions().setType("file").setConfig(path));
    }

    @Override
    protected String getServiceName() {
        return "Market Data service";
    }

    @Override
    protected String getEventBusAddress() {
        return Endpoints.MARKET_DATA;
    }

    @Override
    protected Class getVerticleClass() {
        return MarketDataVerticle.class;
    }
}
