package dev.ankrugold.jedis.future;

import redis.clients.jedis.Response;

import java.util.concurrent.CompletableFuture;

public class FutureResponse<T> extends Response<T> {

    CompletableFuture<T> future;

    public FutureResponse(CompletableFuture<T> future) {
        super(null);
        this.future = future;
    }

    public CompletableFuture<T> getFuture() {
        return future;
    }
}
