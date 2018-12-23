package dev.ankrugold.jedis.helpers;

import dev.ankrugold.jedis.helpers.future.FuturePipeLine;
import dev.ankrugold.jedis.helpers.lambda.LambdaPubSub;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JedisHelper {

    private Jedis jedis;

    public JedisHelper(Jedis jedis) {
        this.jedis = jedis;
    }

    public FuturePipeLine pipelinedfuture() {
        FuturePipeLine futurePipe = new FuturePipeLine();
        futurePipe.setClient(jedis.getClient());
        return futurePipe;
    }

    public void subscribe(BiConsumer<String, String> consumer, String... channels) {
        LambdaPubSub rxPubSub = new LambdaPubSub(consumer);
        jedis.subscribe(rxPubSub, channels);
    }

    public void psubscribe(BiConsumer<String, String> consumer, String... patterns) {
        LambdaPubSub rxPubSub = new LambdaPubSub(consumer);
        jedis.psubscribe(rxPubSub, patterns);
    }

    public Map<String, String> mget(String... keys) {
        List<String> values = jedis.mget(keys);
        return IntStream.range(0, Math.min(values.size(), keys.length)).boxed()
                .filter(i->values.get(i)!=null)
                .collect(Collectors.toMap(i -> keys[i], i -> values.get(i)));
    }

}
