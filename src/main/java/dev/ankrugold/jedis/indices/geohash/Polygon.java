package dev.ankrugold.jedis.indices.geohash;

import com.github.davidmoten.geo.LatLong;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.geom.Path2D;
import java.util.List;

@Getter
@AllArgsConstructor
public class Polygon {

    Path2D path;
    List<LatLong> points;


    public Polygon(List<LatLong> points) {
        path = new Path2D.Double();
        LatLong first = points.get(0);
        path.moveTo(first.getLat(), first.getLon());
        for(int i = 1; i < points.size(); i ++) {
            LatLong pt = points.get(i);
            path.lineTo(pt.getLat(), pt.getLon());
        }
        path.closePath();
        this.points=points;
    }

    public boolean contains(LatLong location) {
        return path.contains(location.getLat(),location.getLon());
    }


}
