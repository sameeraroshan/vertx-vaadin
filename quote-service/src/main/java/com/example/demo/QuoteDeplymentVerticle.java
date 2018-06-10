package com.example.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class QuoteDeplymentVerticle extends DepoymenetVerticle {

    public static void main(final String[] args) {
        Launcher.executeCommand("run", QuoteDeplymentVerticle.class.getName());
    }
    @Override
    public String getServiceName() {
        return "Quote service";
    }

    @Override
    protected String getEventBusAddress() {
        return Endpoints.QUOTE_SERICE;
    }

    @Override
    protected Class getVerticleClass() {
        return QuoteVerticle.class;
    }
}
