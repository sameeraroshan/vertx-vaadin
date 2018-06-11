package com.exmple.demo.services;

import com.example.demo.DepoymenetVerticle;
import com.example.demo.Endpoints;
import com.example.demo.ServiceLauncher;

public class QuoteDeplymentVerticle extends DepoymenetVerticle {

    public static void main(final String[] args) {
        deploy();
    }

    public static void deploy() {
        ServiceLauncher.executeCommand("run", QuoteDeplymentVerticle.class.getName());
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
