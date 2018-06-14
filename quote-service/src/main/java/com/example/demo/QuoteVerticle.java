package com.example.demo;

import com.example.demo.constant.Endpoints;
import com.example.demo.verticle.BaseVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class QuoteVerticle extends BaseVerticle {
    private Map<String, JsonObject> quotes = new HashMap<>();

    @Override
    public void start() {
        MessageConsumer consumer = vertx.eventBus().<JsonObject>consumer(Endpoints.MARKET_DATA).handler(message -> {
            JsonObject quote = message.body();
            quotes.put(quote.getString("name"), quote);
            broadcast(quote);
            message.reply("OK");
        });

        consumer.completionHandler(complete -> {
            System.out.println("Quote verticle registred to " + Endpoints.MARKET_DATA);
            super.start();
        });
    }

    @Override
    public void createCircuitBreaker(Vertx vertx) {

    }

    /**
     * Sends the market data on the event bus.
     */
    private void broadcast(JsonObject quote) {
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        vertx.eventBus().publish(getEventBusAddress(),quote, deliveryOptions);
    }

    public Map<String, JsonObject> getQuotes() {
        return quotes;
    }

    @Override
    public String getEventBusAddress() {
        return Endpoints.QUOTE_SERICE;
    }
}
