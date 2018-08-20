package dev.ankrugold.jedis.lambda;

import redis.clients.jedis.JedisPubSub;

import java.util.function.BiConsumer;

public class LambdaPubSub extends JedisPubSub {

    private BiConsumer<String, String> consumer;

    public LambdaPubSub(BiConsumer<String, String> consumer) {
        this.consumer = consumer;
    }


    @Override
    public void onMessage(String channel, String message) {
        consumer.accept(channel, message);
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        consumer.accept(pattern, message);
    }


}
