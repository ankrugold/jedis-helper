package dev.ankrugold.jedis.indices.geohash;

import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;

import java.util.*;

public class GeoHashIndex {

    public static HashSet<String> getHashes(Polygon poly, int length) {
        Queue<String> eligible = new LinkedList<>();
        HashSet<String> polygonHashes = new HashSet<>();
        HashSet<String> nonPolygonHashes = new HashSet<>();

        List<LatLong> coordinates = poly.getPoints();
        for(LatLong coordinate : coordinates) {
            String hash = GeoHash.encodeHash(coordinate, length);
            polygonHashes.add(hash);
            GeoHash.neighbours(hash).stream().forEach(s -> eligible.add(s));
        }

        while (!eligible.isEmpty()) {
            String test = eligible.poll();
            if (polygonHashes.contains(test) || nonPolygonHashes.contains(test)) {
                continue;
            } else {
                LatLong latLong = GeoHash.decodeHash(test);
                if (poly.contains(latLong)) {
                    polygonHashes.add(test);
                    GeoHash.neighbours(test).stream().forEach(s -> eligible.add(s));
                } else {
                    nonPolygonHashes.add(test);
                }

            }
        }
        return polygonHashes;
    }

}
