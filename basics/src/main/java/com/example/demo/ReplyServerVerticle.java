package com.example.demo;

import io.vertx.core.AbstractVerticle;

import java.util.concurrent.ThreadLocalRandom;

public class ReplyServerVerticle extends AbstractVerticle implements ServiceDiscover,HealthManager {

    @Override
    public void start() throws Exception {

    }

    @Override
    public String getEventBusAddress() {
        return "vertx.reply.server";
    }
}
