package org.nocrala.tools.gis.data.esri.shapefile.shape;

public class PointData {

    private double x;
    private double y;

    public PointData(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

}
