package com.exmple.demo.services;

import com.example.demo.Endpoints;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuoteVerticle extends AbstractVerticle {
    private Map<String, JsonObject> quotes = new HashMap<>();

    @Override
    public void start() {

        MessageConsumer consumer = vertx.eventBus().<JsonObject>consumer(Endpoints.MARKET_DATA).handler(message -> {
            JsonObject quote = message.body();
            quotes.put(quote.getString("name"), quote);
            System.out.println("Quote received : " + quote);
        });
        
        consumer.completionHandler(complete -> {
            System.out.println("Quote verticle registred to " + Endpoints.MARKET_DATA);
        });

        System.out.println("Quote verticle started");
    }



}
