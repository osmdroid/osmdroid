package org.nocrala.tools.gis.data.esri.shapefile.shape.shapes;

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.shape.Const;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;
import org.nocrala.tools.gis.data.esri.shapefile.util.ISUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public abstract class AbstractPolyZShape extends AbstractPolyShape {

    private static final int BASE_CONTENT_LENGTH = (4 + 8 * 4 + 4 + 4 + 8 * 2 + 8 * 2) / 2;

    private double minZ;
    private double maxZ;
    private double[] z;

    private double minM;
    private double maxM;
    private double[] measures;

    public AbstractPolyZShape(final ShapeHeader shapeHeader,
                              final ShapeType shapeType, final InputStream is,
                              final ValidationPreferences rules) throws IOException,
            InvalidShapeFileException {

        super(shapeHeader, shapeType, is, rules);

        if (!rules.isAllowBadContentLength()) {
            int expectedLength = BASE_CONTENT_LENGTH //
                    + (this.numberOfParts * (4)) / 2 //
                    + (this.numberOfPoints * (8 * 2 + 8 + 8)) / 2;
            if (this.header.getContentLength() != expectedLength) {
                throw new InvalidShapeFileException("Invalid " + getShapeTypeName()
                        + " shape header's content length. " + "Expected " + expectedLength
                        + " 16-bit words (for " + this.numberOfParts + " parts and "
                        + this.numberOfPoints + " points)" + " but found "
                        + this.header.getContentLength() + ". " + Const.PREFERENCES);
            }
        }

        this.minZ = ISUtil.readLeDouble(is);
        this.maxZ = ISUtil.readLeDouble(is);

        this.z = new double[this.numberOfPoints];
        for (int i = 0; i < this.numberOfPoints; i++) {
            this.z[i] = ISUtil.readLeDouble(is);
        }

        this.minM = ISUtil.readLeDouble(is);
        this.maxM = ISUtil.readLeDouble(is);

        this.measures = new double[this.numberOfPoints];
        for (int i = 0; i < this.numberOfPoints; i++) {
            this.measures[i] = ISUtil.readLeDouble(is);
        }

    }

    public double[] getMOfPart(final int i) {
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

        return Arrays.copyOfRange(this.measures, from, to);
    }

    public double[] getZOfPart(final int i) {
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

        return Arrays.copyOfRange(this.z, from, to);
    }

    // Accessors

    public double getMinZ() {
        return minZ;
    }

    public double getMaxZ() {
        return maxZ;
    }

    public double[] getZ() {
        return z;
    }

    public double getMinM() {
        return minM;
    }

    public double getMaxM() {
        return maxM;
    }

    public double[] getMeasures() {
        return measures;
    }

}
