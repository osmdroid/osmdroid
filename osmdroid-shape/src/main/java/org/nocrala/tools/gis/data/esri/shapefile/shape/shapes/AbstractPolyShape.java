package org.nocrala.tools.gis.data.esri.shapefile.shape.shapes;

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.Const;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PointData;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;
import org.nocrala.tools.gis.data.esri.shapefile.util.ISUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public abstract class AbstractPolyShape extends AbstractShape {

    protected double boxMinX;
    protected double boxMinY;
    protected double boxMaxX;
    protected double boxMaxY;

    protected int numberOfParts;
    protected int numberOfPoints;

    protected int[] partFirstPoints;
    protected PointData[] points;

    public AbstractPolyShape(final ShapeHeader shapeHeader,
                             final ShapeType shapeType, final InputStream is,
                             final ValidationPreferences rules) throws IOException,
            InvalidShapeFileException {

        super(shapeHeader, shapeType, is, rules);

        this.boxMinX = ISUtil.readLeDouble(is);
        this.boxMinY = ISUtil.readLeDouble(is);
        this.boxMaxX = ISUtil.readLeDouble(is);
        this.boxMaxY = ISUtil.readLeDouble(is);

        this.numberOfParts = ISUtil.readLeInt(is);

        if (this.numberOfParts < 0) {
            throw new InvalidShapeFileException("Invalid " + getShapeTypeName()
                    + " shape number of parts. "
                    + "It should be a number greater than zero, but found "
                    + this.numberOfParts + ". " + Const.PREFERENCES);
        }

        this.numberOfPoints = ISUtil.readLeInt(is);

        if (this.numberOfPoints < 0) {
            throw new InvalidShapeFileException("Invalid " + getShapeTypeName()
                    + " shape number of points. "
                    + "It should be a number greater than zero, but found "
                    + this.numberOfPoints + ". " + Const.PREFERENCES);
        }

        if (this.numberOfParts > this.numberOfPoints) {
            throw new InvalidShapeFileException("Invalid " + getShapeTypeName()
                    + " shape number of parts. "
                    + "It should be smaller or equal to the number of points ("
                    + this.numberOfPoints + "), but found " + this.numberOfParts + ". "
                    + Const.PREFERENCES);
        }

        if (!rules.isAllowUnlimitedNumberOfPointsPerShape()) {
            if (this.numberOfPoints > rules.getMaxNumberOfPointsPerShape()) {
                throw new InvalidShapeFileException("Invalid " + getShapeTypeName()
                        + " shape number of points. "
                        + "The allowed maximum number of points was "
                        + rules.getMaxNumberOfPointsPerShape() + " but found "
                        + this.numberOfPoints + ". " + Const.PREFERENCES);
            }
        }

        this.partFirstPoints = new int[this.numberOfParts];
        for (int i = 0; i < this.numberOfParts; i++) {
            this.partFirstPoints[i] = ISUtil.readLeInt(is);
        }

        this.points = new PointData[this.numberOfPoints];
        for (int i = 0; i < this.numberOfPoints; i++) {
            double x = ISUtil.readLeDouble(is);
            double y = ISUtil.readLeDouble(is);
            this.points[i] = new PointData(x, y);
        }

    }

    protected abstract String getShapeTypeName();

    public PointData[] getPointsOfPart(final int i) {
        if (i < 0 || i >= this.numberOfParts) {
            throw new RuntimeException("Invalid part " + i + ". Available parts [0:"
                    + this.numberOfParts + "].");
        }
        int from = this.partFirstPoints[i];
        int to = i < this.numberOfParts - 1 ? this.partFirstPoints[i + 1]
                : this.points.length;

        if (from < 0 || from > this.points.length) {
            throw new RuntimeException("Malformed content. Part start (" + from
                    + ") is out of range. Valid range of points is [0:"
                    + this.points.length + "].");
        }

        if (to < 0 || to > this.points.length) {
            throw new RuntimeException("Malformed content. Part end (" + to
                    + ") is out of range. Valid range of points is [0:"
                    + this.points.length + "].");
        }

        return Arrays.copyOfRange(this.points, from, to);
    }

    // Getters

    public double getBoxMinX() {
        return boxMinX;
    }

    public double getBoxMinY() {
        return boxMinY;
    }

    public double getBoxMaxX() {
        return boxMaxX;
    }

    public double getBoxMaxY() {
        return boxMaxY;
    }

    public int getNumberOfParts() {
        return numberOfParts;
    }

    public int getNumberOfPoints() {
        return numberOfPoints;
    }

    public int[] getPartFirstPoints() {
        return partFirstPoints;
    }

    public PointData[] getPoints() {
        return points;
    }

}
