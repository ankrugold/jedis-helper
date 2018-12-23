package dev.ankrugold.jedis.indices;

import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import dev.ankrugold.jedis.indices.geohash.GeoHashIndex;
import redis.clients.jedis.Jedis;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RedisIndices {
    public static final String HASH = "HASH";
    private Jedis jedis;

    public RedisIndices(Jedis jedis) {
        this.jedis = jedis;
    }

    public void indexPolygon(String polygonId, List<LatLong> polygon, int precision){
        JavaPolygon poly = new JavaPolygon(polygon);
        HashSet<String> hashes = GeoHashIndex.getHashes(poly, precision);
        hashes.forEach(h -> jedis.sadd(getLocPrefix(h),polygonId));
    }

    public Set<String> getPolygons(LatLong loc, int minPrecision, int maxPrecision){
        List<String> hashes = IntStream.range(minPrecision, maxPrecision)
                .mapToObj(x -> GeoHash.encodeHash(loc, x)).collect(Collectors.toList());
       for(String h : hashes){
           Set<String> smembers = jedis.smembers(getLocPrefix(h));
           if(!smembers.isEmpty()){
               return smembers;
           }
       }
       return new HashSet<>();
    }

    private String getLocPrefix(String hash){
        return HASH + hash;
    }


}
