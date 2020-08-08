package com.example.folio9470m.rendezvous_re;

import com.example.folio9470m.rendezvous_re.models.LatLong;
import com.example.folio9470m.rendezvous_re.models.PolygonCentroid;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PolygonCentroidTest {
    @Test
    public void centroid() {
        LatLong Kohat = new LatLong(33.61653,71.4807);
        LatLong Mardan = new LatLong(34.23641,72.03558);
        LatLong Taxila = new LatLong(33.7194,72.91998);
        LatLong Islamabad = new LatLong(33.67141,73.03259);
        LatLong Pindi = new LatLong(33.55705,73.00787);
        LatLong point6 = new LatLong(33.99263,72.28048);
        LatLong point7 = new LatLong(32.93071,72.44128);

        List<LatLong> points = new ArrayList() ;
        points.add(Kohat);
        points.add(Mardan);
        points.add(Taxila);
        points.add(Islamabad);
        points.add(Pindi);
        points.add(point6);
        points.add(point7);
        PolygonCentroid polygonCentroid = new PolygonCentroid(points);

        LatLong centroidTest = polygonCentroid.centroid();
    }
}