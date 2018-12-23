package dev.ankrugold.jedis.helpers.future;

import redis.clients.jedis.Builder;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class FuturePipeLine extends Pipeline {

    CountDownLatch cl = new CountDownLatch(1);

    @Override
    protected <T> FutureResponse<T> getResponse(Builder<T> builder) {
        Response<T> response = super.getResponse(builder);
        CompletableFuture<T> cf = CompletableFuture.supplyAsync(() -> {
                    try {
                        cl.await();
                        return response.get();
                    } catch (Exception e) {
                        return null;
                    }
                }
        );
        FutureResponse<T> promise = new FutureResponse<>(cf);
        return promise;
    }

    @Override
    public void sync() {
        super.sync();
        cl.countDown();
    }
}
