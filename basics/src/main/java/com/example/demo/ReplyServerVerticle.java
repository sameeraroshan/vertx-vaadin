package com.example.demo;

import io.vertx.core.AbstractVerticle;

import java.util.concurrent.ThreadLocalRandom;

public class ReplyServerVerticle extends AbstractVerticle implements ServiceDiscover,HealthManager {

    @Override
    public void start() throws Exception {
        createServiceDiscovery(vertx,"Reply Service");
        createHealthEndpoint(vertx);
        vertx.eventBus().<String>consumer(getEventBusAddress(),event -> {
            int randomNum = ThreadLocalRandom.current().nextInt(0, 10 + 1);
            if(randomNum>=5){
                String name = event.body();
                event.reply("Hello "+name+" from vertx.reply.server served by handler "
                        +deploymentID()+ " "+Thread.currentThread());
            }else {
                event.fail(404,"Faild to process message");
            }
        });
    }

    @Override
    public String getEventBusAddress() {
        return "vertx.reply.server";
    }
}
