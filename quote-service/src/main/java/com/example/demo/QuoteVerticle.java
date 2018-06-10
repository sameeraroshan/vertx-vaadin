package com.example.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class QuoteVerticle extends AbstractVerticle {
    private Map<String, JsonObject> quotes = new HashMap<>();

    @Override
    public void start() {
        vertx.eventBus().<JsonObject>consumer(Endpoints.MARKET_DATA)
                .handler(message -> {
                    JsonObject quote = message.body();
                    quotes.put(quote.getString("name"), quote);
                    System.out.println("Quote received : " + quote);
                });
    }


}
