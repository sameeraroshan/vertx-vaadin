package com.example.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

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
        });

        consumer.completionHandler(complete -> {
            System.out.println("Quote verticle registred to " + Endpoints.MARKET_DATA);
        });

        System.out.println("Quote verticle started");
    }

    /**
     * Sends the market data on the event bus.
     */
    private void broadcast(JsonObject quote) {
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        vertx.eventBus().publish(Endpoints.MARKET_DATA,quote, deliveryOptions);
    }


    @Override
    void createCircuitBreaker(Vertx vertx) {

    }

    @Override
    public String getServiceName() {
        return "Quote service";
    }

    @Override
    public String getEventBusAddress() {
        return Endpoints.QUOTE_SERICE;
    }

    public Map<String, JsonObject> getQuotes() {
        return quotes;
    }
}
