package com.example.demo;

import com.example.demo.constant.Endpoints;
import com.example.demo.verticle.DepoymenetVerticle;

public class QuoteDeplymentVerticle extends DepoymenetVerticle {

    public static void main(final String[] args) {
        ServiceLauncher.executeCommand("run", QuoteDeplymentVerticle.class.getName());
    }

    @Override
    protected Class getVerticleClass() {
        return QuoteVerticle.class;
    }

    @Override
    public String getServiceName() {
        return "Quote service";
    }

    @Override
    public String getEventBusAddress() {
        return Endpoints.QUOTE_SERICE;
    }
}
