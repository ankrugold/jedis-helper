package dev.ankrugold.jedis;

import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import dev.ankrugold.jedis.indices.geohash.GeoHashIndex;
import dev.ankrugold.jedis.indices.JavaPolygon;
import dev.ankrugold.jedis.indices.RedisIndices;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JedisIndicesTest {

    @Test
    public void TestPolygon() {
        LatLong c1 = new LatLong(40.842226, 14.211753);
        LatLong c2 = new LatLong(40.829498, 14.229262);
        LatLong c3 = new LatLong(40.833394, 14.26617);
        LatLong c4 = new LatLong(40.84768, 14.27870);
        LatLong c5 = new LatLong(40.858716, 14.27715);
        Arrays.asList(c1,c2,c3,c4,c5).forEach(x ->  System.out.println(x.getLon() + "," + x.getLat()));
        JavaPolygon polygon = new JavaPolygon(Arrays.asList(c1, c2, c3, c4, c5));
        HashSet<String> hashes = GeoHashIndex.getHashes(polygon, 6);
        for (String hash : hashes) {
            LatLong x = GeoHash.decodeHash(hash);
            System.out.println(x.getLon() + "," + x.getLat());
            System.out.println(hash);
        }
        System.out.println(GeoHash.encodeHash(new LatLong(40.842226,14.211753),6));
    }

    @Test
    public void TestPointPolygon() {
        LatLong c1 = new LatLong(40.842226, 14.211753);
        LatLong c2 = new LatLong(40.829498, 14.229262);
        LatLong c3 = new LatLong(40.833394, 14.26617);
        LatLong c4 = new LatLong(40.84768, 14.27870);
        LatLong c5 = new LatLong(40.858716, 14.27715);
        try (Jedis jedis = new JedisPool("localhost").getResource()) {
            RedisIndices redisIndices = new RedisIndices(jedis);
            redisIndices.indexPolygon("bogota",Arrays.asList(c1,c2,c3,c4,c5),6);
            Set<String> polygons = redisIndices.getPolygons(new LatLong(40.8344991,14.2506072), 4, 7);
            System.out.println(polygons);
            polygons = redisIndices.getPolygons(new LatLong(40.842226,14.211753), 4, 7);
            System.out.println(polygons);
        }
    }




}
