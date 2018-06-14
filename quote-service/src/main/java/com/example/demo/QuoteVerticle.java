package com.example.demo;

import com.example.demo.constant.Endpoints;
import com.example.demo.verticle.BaseVerticle;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class QuoteVerticle extends BaseVerticle {
    private Map<String, JsonObject> quotes = new HashMap<>();
    CircuitBreaker breaker;

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
        breaker = CircuitBreaker.create(getEventBusAddress()+".histrix", vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(5)
                        .setFallbackOnFailure(true)
        );
    }

    /**
     * Sends the market data on the event bus.
     */
    private void broadcast(JsonObject quote) {
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        breaker.<String>execute(future -> {
            try {
                vertx.eventBus().publish(getEventBusAddress(),quote, deliveryOptions);
                future.complete("Quote published to event bus:");
            } catch (Exception e) {
                e.printStackTrace();
                future.fail("Quote publish failed to event bus:");
            }
        }).setHandler(ar -> {
            // Do something with the result
        });

    }

    public Map<String, JsonObject> getQuotes() {
        return quotes;
    }

    @Override
    public String getEventBusAddress() {
        return Endpoints.QUOTE_SERICE;
    }
}
