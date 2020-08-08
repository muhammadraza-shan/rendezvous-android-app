package com.example.folio9470m.rendezvous_re.models;

import com.example.folio9470m.rendezvous_re.models.LatLong;

import java.util.List;

public class PolygonCentroid {

    private List<LatLong> points;
    private int pointsSize;

    public PolygonCentroid(List<LatLong> points) {
        this.points = points;
        this.pointsSize = points.size();
    }

    protected double polygonArea() {
        double area = 0;
        for (int i = 0, j; i < pointsSize; i++) {
            j = (i + 1) % pointsSize;
            area += points.get(i).getLongitude() * points.get(j).getLatitude();
            area -= points.get(i).getLatitude() * points.get(j).getLongitude();
        }
        area /= 2.0;
        return area;
    }

    public LatLong centroid() {
        double cx = 0, cy = 0;
        double factor;
        for (int i = 0, j; i < pointsSize; i++) {
            j = (i + 1) % pointsSize;
            factor = (points.get(i).getLongitude() * points.get(j).getLatitude() - points.get(j).getLongitude() * points.get(i).getLatitude());
            cx += (points.get(i).getLongitude() + points.get(j).getLongitude()) * factor;
            cy += (points.get(i).getLatitude() + points.get(j).getLatitude()) * factor;
        }
        double A = polygonArea();
        factor = 1.0 / (6.0 * A);
        cx *= factor;
        cy *= factor;
        return new LatLong(cy, cx);
    }

}