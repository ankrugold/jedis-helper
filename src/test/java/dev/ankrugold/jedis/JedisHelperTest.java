package dev.ankrugold.jedis;

import dev.ankrugold.jedis.future.FuturePipeLine;
import dev.ankrugold.jedis.future.FutureResponse;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JedisHelperTest {

    @Test
    public void testMGet() {
        try (Jedis jedis = new JedisPool("localhost").getResource()) {
            jedis.mset("1", "2", "3", "1", "2", "3");
            Map<String, String> result = new JedisHelper(jedis).mget("2", "3", "4");
            assertTrue("key absent", result.containsKey("2"));
            assertTrue("key absent", result.containsKey("3"));
            assertFalse("wrong key", result.containsKey("4"));
        }
    }


    @Test
    public void testPubSub(){
        CountDownLatch cl = new CountDownLatch(1);
        publish(cl);
        cl.countDown();
        try (Jedis jedis = new JedisPool("localhost").getResource()) {
            new JedisHelper(jedis)
                    .subscribe((c, m) -> assertTrue("wrong message" + m, m.equals("world")),
                            "hello");
        }
    }

    public void publish(CountDownLatch cl) {
        new Thread(() ->
        {
            try {
                cl.await();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try (Jedis jedis = new JedisPool("localhost").getResource()) {
                jedis.publish("hello", "world");
            }
        }
        ).start();
    }

    @Test
    public void testBasicGet() throws ExecutionException, InterruptedException {
        try (Jedis jedis = new JedisPool("localhost").getResource()) {
            jedis.mset("Hello", "Hello", "World", "World");
            FuturePipeLine pipelined = new JedisHelper(jedis).pipelinedfuture();
            CompletableFuture<String> hello = toUpperCase(pipelined, "Hello");
            CompletableFuture<String> world = toLowerCase(pipelined, "World");
            pipelined.sync();
            assertTrue(hello.get().equals("HELLO"));
            assertTrue(world.get().equals("world"));

        }
    }


    public CompletableFuture<String> toUpperCase(FuturePipeLine pipelined, String hello) {
        FutureResponse<String> response = (FutureResponse<String>) pipelined.get(hello);
        return response.getFuture().thenApply(r -> r.toUpperCase());
    }

    public CompletableFuture<String> toLowerCase(FuturePipeLine pipelined, String hello) {
        FutureResponse<String> response = (FutureResponse<String>) pipelined.get(hello);
        return response.getFuture().thenApply(r -> r.toLowerCase());
    }

}
