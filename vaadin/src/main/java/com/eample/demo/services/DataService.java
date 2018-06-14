package com.eample.demo.services;

import com.eample.demo.dao.Stock;
import com.example.demo.constant.Endpoints;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataService {

    private static final DataService service = new DataService();


    List<Stock> stockList = new ArrayList<>();

    HashMap<String, ArrayList<Handler>> handlers = new HashMap();

    Handler<Message> messageHandler;
    Handler<Record> recordHandler;

    private DataService() {

         messageHandler = message -> {
            JsonObject jsonObject = (JsonObject) message.body();
            Stock stock = new Stock();
            stock.setExchange(jsonObject.getString("exchange"));
            stock.setSymbol(jsonObject.getString("symbol"));
            stock.setName(jsonObject.getString("name"));
            stock.setBid(jsonObject.getDouble("bid"));
            stock.setAsk(jsonObject.getDouble("ask"));
            stock.setOpen(jsonObject.getDouble("open"));
            stock.setShares(jsonObject.getDouble("shares"));
            stockList.add(stock);
            System.out.println("updated stock:" + jsonObject.encodePrettily());
            broadcastMarketData(stock);
        };
        recordHandler = record-> this.broadcastRecordData(record);
        MicroServicesListener.getListener().subscribe(Endpoints.QUOTE_SERICE, messageHandler);
        MicroServicesListener.getListener().subscribe(Endpoints.RECORD_SERVICE, recordHandler);
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Running Shutdown Hook");
                DataService.getService().destroy();
            }
        });
    }

    public static DataService getService() {
        return service;
    }

    public void destroy() {
        MicroServicesListener.getListener().unSubscribe(Endpoints.QUOTE_SERICE, messageHandler);
        MicroServicesListener.getListener().unSubscribe(Endpoints.RECORD_SERVICE, recordHandler);
    }

    public void subscribe(String address, Handler handler) {
        if (this.handlers.get(address) == null) {
            this.handlers.put(address, new ArrayList<>());
        }
        this.handlers.get(address).add(handler);
        sendInitialData(address, handler);
    }

    protected void sendInitialData(String address, Handler handler) {
        switch (address) {
            case Endpoints.QUOTE_SERICE:
                stockList.forEach(handler::handle);
                break;
            case Endpoints.METRICS_SERICE:
            default:
                break;
        }
    }

    //not thread safe
    public void unSubscribe(String address, Handler handler) {
        if (this.handlers.get(address) == null) {
            this.handlers.get(address).remove(handler);
        }
    }

    public void broadcastMarketData(Stock stock) {
        handlers.get(Endpoints.QUOTE_SERICE).forEach(stockHandler -> stockHandler.handle(stock));
    }

    public void broadcastRecordData(Record record) {
        handlers.get(Endpoints.RECORD_SERVICE).forEach(recordHandler -> recordHandler.handle(record));
    }

}
