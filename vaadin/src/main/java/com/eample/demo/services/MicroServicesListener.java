package com.eample.demo.services;

import com.example.demo.DepoymenetVerticle;
import com.example.demo.Endpoints;
import com.example.demo.ServiceDiscoveryManager;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MicroServicesListener extends DepoymenetVerticle implements ServiceDiscoveryManager {

    private static final MicroServicesListener listener = new MicroServicesListener();
    private static final HashMap<String, ArrayList<Handler>> handlers = new HashMap();
    private ServiceDiscovery serviceDiscovery;
    private Record record;

    private MicroServicesListener(){
        initHazelcastCluster();
    }

    @Override
    protected Class getVerticleClass() {
        return this.getClass();
    }

    @Override
    public void onclustredVerticle(Vertx vertx) {
        vertx.eventBus().consumer(Endpoints.QUOTE_SERICE, message -> {
            List<Handler> handlers = this.handlers.get(Endpoints.QUOTE_SERICE);
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

        ServiceRecordTuple<Record,ServiceDiscovery> tuple =  createServiceDiscovery(vertx);
        serviceDiscovery = tuple.getDiscovery();
        record = tuple.getRecoed();

       vertx.setPeriodic(5000,event -> {
            List<Handler> handlers = this.handlers.get(Endpoints.RECORD_SERVICE);
            if(handlers != null){
                getDiscovery().getRecords(record1 -> true,true, result -> {
                    System.out.println("Record"+getRecord());
                    if(!result.failed()){
                        handlers.forEach(m -> m.handle(result.result()));
                    }
                });
            }
        });
    }

    public void subscribe(String address, Handler handler) {
        if (this.handlers.get(address) == null) {
            this.handlers.put(address, new ArrayList<>());
        }
        this.handlers.get(address).add(handler);
    }

    public void unSubscribe(String address, Handler handler) {
        if (this.handlers.get(address) == null) {
            this.handlers.get(address).remove(handler);
        }
    }

    public static MicroServicesListener getListener() {
        return listener;
    }

    @Override
    public Record getRecord() {
        return record;
    }

    @Override
    public ServiceDiscovery getDiscovery() {
        return serviceDiscovery;
    }

    @Override
    public String getServiceName() {
        return "Vaadin-UI";
    }

    @Override
    public String getEventBusAddress() {
        return Endpoints.UI_DATA_SERVICE;
    }
}
