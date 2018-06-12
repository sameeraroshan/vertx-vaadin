package com.eample.demo.services;

import com.eample.demo.Stock;
import com.example.demo.Endpoints;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataService implements Handler<Message>{

    private static final DataService service = new DataService();

    List<Stock> stockList = new ArrayList<>();
    private  HashMap<String, ArrayList<Handler<Stock>>> handlers = new HashMap();
    private DataService(){
        MicroServicesListener.getListener().subscribe(Endpoints.MARKET_DATA, this);
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Running Shutdown Hook");
                DataService.getService().destroy();
            }
        });
    }

    public static DataService getService(){
        return service;
    }

    public void destroy(){
        MicroServicesListener.getListener().unSubscribe(Endpoints.MARKET_DATA, this);
    }

    public void subscribe(String address, Handler<Stock> handler) {
        if (this.handlers.get(address) == null) {
            this.handlers.put(address, new ArrayList<>());
            stockList.forEach(handler::handle);
        }
        this.handlers.get(address).add(handler);
    }
    //not thread safe
    public void unSubscribe(String address, Handler<Stock> handler) {
        if (this.handlers.get(address) == null) {
            this.handlers.get(address).remove(handler);
        }
    }

    @Override
    public void handle(Message message) {
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
    }


    public void broadcastMarketData(Stock stock){
        handlers.get(Endpoints.MARKET_DATA).forEach(stockHandler -> stockHandler.handle(stock));
    }
}
