package com.eample.demo.services;

import com.example.demo.Endpoints;
import com.example.demo.HazelcastCluster;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MicroServicesListener implements HazelcastCluster {

    private static final MicroServicesListener listener = new MicroServicesListener();
    private static final HashMap<String, ArrayList<Handler<Message>>> handlers = new HashMap();

    private MicroServicesListener(){
        initHazelcastCluster();
    }

    @Override
    public void createServiceDiscovery(Vertx vertx) {
        //not implemented
    }

    @Override
    public void onclustredVerticle(Vertx vertx) {
        vertx.eventBus().consumer(Endpoints.MARKET_DATA, message -> {
            handlers.get(Endpoints.MARKET_DATA).forEach(m -> m.handle(message));
            JsonObject body = (JsonObject) message.body();
            System.out.println("message from vaadin " + body.encodePrettily());
        });
    }

    public void subscribe(String address, Handler<Message> handler) {
        if (this.handlers.get(address) == null) {
            this.handlers.put(address, new ArrayList<>());
        }
        this.handlers.get(address).add(handler);
    }

    public void unSubscribe(String address, Handler<Message> handler) {
        if (this.handlers.get(address) == null) {
            this.handlers.get(address).remove(handler);
        }
    }

    public static MicroServicesListener getListener() {
        return listener;
    }

}
