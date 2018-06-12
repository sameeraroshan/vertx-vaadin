package com.eample.demo.services;

import com.example.demo.DepoymenetVerticle;
import com.example.demo.Endpoints;
import com.example.demo.HazelcastCluster;
import com.example.demo.ServiceLauncher;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MicroServicesListener extends DepoymenetVerticle {

    private static final MicroServicesListener listener = new MicroServicesListener();
    private static final HashMap<String, ArrayList<Handler>> handlers = new HashMap();


    private MicroServicesListener(){
        initHazelcastCluster();
    }

    @Override
    protected Class getVerticleClass() {
        return null;
    }

    @Override
    public String getServiceName() {
        return "vaadin-ui";
    }

    @Override
    public String getEventBusAddress() {
        return "vaadin.ui";
    }

    @Override
    public void onclustredVerticle(Vertx vertx) {
        vertx.eventBus().consumer(Endpoints.MARKET_DATA, message -> {
            List<Handler> handlers = this.handlers.get(Endpoints.MARKET_DATA);
            if(handlers != null){
                handlers.forEach(m -> m.handle(message));
            }
        });

        vertx.eventBus().consumer(Endpoints.METRICS_SERICE,  message -> {
            List<Handler> handlers = this.handlers.get(Endpoints.METRICS_SERICE);
            if(handlers != null){
                handlers.forEach(m -> m.handle(message));
            }
        });

        vertx.setPeriodic(5000,event -> {
            List<Handler> handlers = this.handlers.get(Endpoints.RECORD_SERVICE);
            if(handlers != null){
                discovery.getRecords(record1 -> true,true, result -> {
                    System.out.println("Record"+record);
                    if(!result.failed()){
                        handlers.forEach(m -> m.handle(result.result()));
                    }
                });
            }
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
