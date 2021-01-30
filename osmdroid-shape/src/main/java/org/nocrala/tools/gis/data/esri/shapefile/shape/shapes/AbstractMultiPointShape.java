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

public abstract class AbstractMultiPointShape extends AbstractShape {

    protected double boxMinX;
    protected double boxMinY;
    protected double boxMaxX;
    protected double boxMaxY;

    protected int numberOfPoints;
    protected PointData[] points;

    public AbstractMultiPointShape(final ShapeHeader shapeHeader,
                                   final ShapeType shapeType, final InputStream is,
                                   final ValidationPreferences rules) throws IOException,
            InvalidShapeFileException {
        super(shapeHeader, shapeType, is, rules);

        this.boxMinX = ISUtil.readLeDouble(is);
        this.boxMinY = ISUtil.readLeDouble(is);
        this.boxMaxX = ISUtil.readLeDouble(is);
        this.boxMaxY = ISUtil.readLeDouble(is);

        this.numberOfPoints = ISUtil.readLeInt(is);

        if (!rules.isAllowUnlimitedNumberOfPointsPerShape()) {
            if (this.numberOfPoints > rules.getMaxNumberOfPointsPerShape()) {
                throw new InvalidShapeFileException("Invalid " + getShapeTypeName()
                        + " shape number of points. "
                        + "The allowed maximum number of points was "
                        + rules.getMaxNumberOfPointsPerShape() + " but found "
                        + this.numberOfPoints + ". " + Const.PREFERENCES);
            }
        }

        this.points = new PointData[this.numberOfPoints];
        for (int i = 0; i < this.numberOfPoints; i++) {
            double x = ISUtil.readLeDouble(is);
            double y = ISUtil.readLeDouble(is);
            this.points[i] = new PointData(x, y);
        }

    }

    protected abstract String getShapeTypeName();

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

    public int getNumberOfPoints() {
        return numberOfPoints;
    }

    public PointData[] getPoints() {
        return points;
    }

}
