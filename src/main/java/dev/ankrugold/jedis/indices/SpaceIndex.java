package dev.ankrugold.jedis.indices;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SpaceIndex {

    private int precision = 64;

    private int dims;

    private String spaceKey;

    private Jedis jedis;


    public void index(String id, int[] data) {
        assert data.length == dims;
        String encodingkey = encode(data);
        StringJoiner joiner = new StringJoiner(":");
        for (int dat : data) {
            joiner.add(String.valueOf(dat));
        }
        joiner.add(id);
        String indexStr = joiner.toString();
        Transaction multi = jedis.multi();
        multi.zadd(spaceKey, 0d, indexStr);
        multi.set(id, indexStr);
        multi.exec();
    }

    private String encode(int[] dim) {

        char[] key = new char[precision * dim.length];
        for (int i = 0; i < dim.length; i++) {
            int x = dim[i];
            String bi = Integer.toBinaryString(x);
            String jbi = padLeft(bi, precision);
            int j = 0;
            for (char c : jbi.toCharArray()) {
                key[j * dim.length + i] = c;
                j++;
            }
        }
        return String.valueOf(key);
    }

    private static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", s).replace(' ', '0');
    }

    public Set<String> query(int[] start, int[] end) {
        assert start.length == dims;
        assert end.length == dims;
        int[] delta = new int[dims];
        for (int i = 0; i < dims; i++) {
            assert start[i] < end[i];
            delta[i] = end[i] - start[i];
        }
        int dmin = Arrays.stream(delta).min().getAsInt();
        int mask = 1;
        while (dmin > 2) {
            dmin = dmin / 2;
            mask++;
        }
        return query(start, end, mask);
    }

    private Set<String> query(int[] start, int[] end, int mask) {

        HashSet<String[]> keys = new HashSet<>();

        int[] starts = Arrays.stream(start).map(x -> x / 2 ^ mask).toArray();
        int[] ends = Arrays.stream(end).map(x -> x / 2 ^ mask).toArray();
        int[] cur = Arrays.copyOf(starts, starts.length);
        boolean allVisited = false;
        while (!allVisited) {

            keys.add(getSubZoneKey(mask, cur));

            for (int i = 0; i < cur.length; i++) {
                if (cur[i] < ends[i]) {
                    cur[i] = cur[i] + 1;
                    break;
                } else if (i == cur.length - 1) {
                    allVisited = true;
                } else {
                    cur[i] = start[i];
                }
            }
        }
        Pipeline pipelined = jedis.pipelined();
        Stream<Response<Set<String>>> resp = keys.stream().map(
                k -> pipelined.zrangeByLex(spaceKey, k[0], k[1]));
        pipelined.sync();
        Set<String> points = resp.map(r -> r.get()).flatMap(x -> x.stream())
                .map(x -> x.split(":"))
                .filter(x -> check(start,end,x))
                .map(x -> x[dims+1])
                .collect(Collectors.toSet())
                ;
        return points;

    }

    private String[] getSubZoneKey(int mask, int[] cur) {
        IntStream rstarts = Arrays.stream(cur).map(x -> x * 2 ^ mask);
        IntStream rends = rstarts.map(x -> x | 2 ^ mask - 1);
        String startKey = "[" + encode(rstarts.toArray()) + ":";
        String endKey = "[" + encode(rends.toArray()) + ":\\xff\"";
        return new String[]{startKey, endKey};
    }

    private boolean check(int[] start, int[] end, String[] val){

        for (int i = 0; i < dims; i++) {
            String s = val[i + 1];
            int v = Integer.parseInt(s, 2);
            if (v < start[i] || v > end[i]){
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        int[] ints = {2, 1};
        System.out.println(new SpaceIndex().encode(ints));
    }

}
