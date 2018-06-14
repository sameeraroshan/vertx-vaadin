package com.example.demo;

import com.example.demo.constant.Endpoints;
import com.example.demo.verticle.BaseVerticle;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.Objects;
import java.util.Random;

public class MarketDataVerticle extends BaseVerticle {
    private String name;
    private int variation;
    private long period;
    private String symbol;
    private int stocks;
    private double price;
    private double bid;
    private double ask;
    private int share;
    private double value;

    private final Random random = new Random();
    CircuitBreaker breaker;

    /**
     * Method called when the verticle is deployed.
     */
    @Override
    public void start() {
        // Retrieve the configuration, and initialize the verticle.
        super.start();
        JsonObject config = config();
        init(config);

        // Every `period` ms, the given Handler is called.
        vertx.setPeriodic(period, l -> {
            compute();
            send();
        });
    }

    @Override
   public void createCircuitBreaker(Vertx vertx) {
        breaker = CircuitBreaker.create("vertx.circuit-breaker", vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(5)
                        .setFallbackOnFailure(true)
        );
    }

    /**
     * Read the configuration and set the initial values.
     * @param config the configuration
     */
    void init(JsonObject config) {
        period = config.getLong("period", 1000L);
        variation = config.getInteger("variation", 100);
        name = config.getString("name");
        Objects.requireNonNull(name);
        symbol = config.getString("symbol", name);
        stocks = config.getInteger("volume", 10000);
        price = config.getDouble("price", 100.0);

        value = price;
        ask = price + random.nextInt(variation / 2);
        bid = price + random.nextInt(variation / 2);

        share = stocks / 2;

        System.out.println("Initialized " + name);
    }

    /**
     * Sends the market data on the event bus.
     */
    private void send() {
        JsonObject data = toJson();



        breaker.<String>execute(future -> {
            vertx.eventBus().send(getEventBusAddress(),data, reply->{
                if(reply.failed()){
                    System.out.println("data send failed to event bus:" + data);
                    future.fail("data send failed to event bus:" + data);
                }else {
                    System.out.println("data sent to event bus:" + data);
                    future.complete("data sent to event bus:" + data);
                }
            });
        }).setHandler(ar -> {
            // Do something with the result
        });
    }

    /**
     * Compute the new evaluation...
     */
    void compute() {

        if (random.nextBoolean()) {
            value = value + random.nextInt(variation);
            ask = value + random.nextInt(variation / 2);
            bid = value + random.nextInt(variation / 2);
        } else {
            value = value - random.nextInt(variation);
            ask = value - random.nextInt(variation / 2);
            bid = value - random.nextInt(variation / 2);
        }

        if (value <= 0) {
            value = 1.0;
        }
        if (ask <= 0) {
            ask = 1.0;
        }
        if (bid <= 0) {
            bid = 1.0;
        }

        if (random.nextBoolean()) {
            // Adjust share
            int shareVariation = random.nextInt(100);
            if (shareVariation > 0 && share + shareVariation < stocks) {
                share += shareVariation;
            } else if (shareVariation < 0 && share + shareVariation > 0) {
                share += shareVariation;
            }
        }
    }

    /**
     * @return a json representation of the market data (quote). The structure is close to
     * <a href="https://en.wikipedia.org/wiki/Market_data">https://en.wikipedia.org/wiki/Market_data</a>.
     */
    private JsonObject toJson() {
        return new JsonObject()
                .put("exchange", "Vert.x stock exchange")
                .put("symbol", symbol)
                .put("name", name)
                .put("bid", bid)
                .put("ask", ask)
                .put("volume", stocks)
                .put("open", price)
                .put("shares", share);

    }


    @Override
    public String getEventBusAddress() {
        return Endpoints.MARKET_DATA;
    }
}
