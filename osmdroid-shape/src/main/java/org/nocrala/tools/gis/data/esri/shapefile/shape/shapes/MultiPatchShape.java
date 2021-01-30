package org.nocrala.tools.gis.data.esri.shapefile.shape.shapes;

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.Const;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PartType;
import org.nocrala.tools.gis.data.esri.shapefile.shape.PointData;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;
import org.nocrala.tools.gis.data.esri.shapefile.util.ISUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Represents a MultiPatch Shape object, as defined by the ESRI Shape file
 * specification.
 */
public class MultiPatchShape extends AbstractShape {

    private static final int BASE_CONTENT_LENGTH = (4 + 8 * 4 + 4 + 4 + 8 * 2 + 8 * 2) / 2;

    protected double boxMinX;
    protected double boxMinY;
    protected double boxMaxX;
    protected double boxMaxY;

    protected int numberOfParts;
    protected int numberOfPoints;

    protected int[] partFirstPoints;
    protected PartType[] partTypes;
    protected PointData[] points;

    private double minZ;
    private double maxZ;
    private double[] z;

    private double minM;
    private double maxM;
    private double[] m;

    public MultiPatchShape(final ShapeHeader shapeHeader,
                           final ShapeType shapeType, final InputStream is,
                           final ValidationPreferences rules) throws IOException,
            InvalidShapeFileException {

        super(shapeHeader, shapeType, is, rules);

        if (!rules.isAllowBadContentLength()) {
            int expectedLength = BASE_CONTENT_LENGTH //
                    + (this.numberOfParts * (4 + 4)) / 2 //
                    + (this.numberOfPoints * (8 * 2 + 8 + 8)) / 2;
            if (this.header.getContentLength() != expectedLength) {
                throw new InvalidShapeFileException("Invalid " + getShapeTypeName()
                        + " shape header's content length. " + "Expected " + expectedLength
                        + " 16-bit words (for " + this.numberOfParts + " parts and "
                        + this.numberOfPoints + " points)" + " but found "
                        + this.header.getContentLength() + ". " + Const.PREFERENCES);
            }
        }

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

        this.partTypes = new PartType[this.numberOfParts];
        for (int i = 0; i < this.numberOfParts; i++) {
            int partTypeId = ISUtil.readLeInt(is);
            PartType partType = PartType.parse(partTypeId);
            if (rules.getForcePartType() == null) {
                if (partType == null) {
                    throw new InvalidShapeFileException("Invalid " + getShapeTypeName()
                            + " shape part type. " + "Part type code found was " + partTypeId
                            + ". " + Const.PREFERENCES);
                }
                this.partTypes[i] = partType;
            } else {
                this.partTypes[i] = rules.getForcePartType();
            }
        }

        this.points = new PointData[this.numberOfPoints];
        for (int i = 0; i < this.numberOfPoints; i++) {
            double x = ISUtil.readLeDouble(is);
            double y = ISUtil.readLeDouble(is);
            this.points[i] = new PointData(x, y);
        }

        this.minZ = ISUtil.readLeDouble(is);
        this.maxZ = ISUtil.readLeDouble(is);

        this.z = new double[this.numberOfPoints];
        for (int i = 0; i < this.numberOfPoints; i++) {
            this.z[i] = ISUtil.readLeDouble(is);
        }

        this.minM = ISUtil.readLeDouble(is);
        this.maxM = ISUtil.readLeDouble(is);

        this.m = new double[this.numberOfPoints];
        for (int i = 0; i < this.numberOfPoints; i++) {
            this.m[i] = ISUtil.readLeDouble(is);
        }

    }

    private String getShapeTypeName() {
        return "MultiPatch";
    }

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

        return Arrays.copyOfRange(this.m, from, to);
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

    public PartType[] getPartTypes() {
        return partTypes;
    }

    public PointData[] getPoints() {
        return points;
    }

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

    public double[] getM() {
        return m;
    }

}
