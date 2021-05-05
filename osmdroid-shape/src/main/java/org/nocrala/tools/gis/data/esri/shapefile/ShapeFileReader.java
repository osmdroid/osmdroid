package org.nocrala.tools.gis.data.esri.shapefile;

import org.nocrala.tools.gis.data.esri.shapefile.exception.DataStreamEOFException;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.header.ShapeFileHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.MultiPatchShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.MultiPointMShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.MultiPointPlainShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.MultiPointZShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.NullShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PointMShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PointShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PointZShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonMShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolygonZShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolylineMShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolylineShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolylineZShape;
import org.nocrala.tools.gis.data.esri.shapefile.util.ISUtil;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads an ESRI Shape File from an InputStream and provides its contents as
 * simple Java objects.
 */
public class ShapeFileReader {

    private BufferedInputStream is;
    private ValidationPreferences rules;

    private ShapeFileHeader header;
    private boolean eofReached;

    // Constructors

    /**
     * <p>
     * Reads a Shape File from an InputStream using the default validation
     * preferences. The default validation preferences conforms strictly to the
     * ESRI ShapeFile specification.
     * </p>
     *
     * <p>
     * The constructor will automatically read the header of the file. Thereafter,
     * use the method next() to read all shapes.
     * </p>
     *
     * @param is the InputStream to be read.
     * @throws InvalidShapeFileException if the data is malformed, according to the ESRI ShapeFile
     *                                   specification.
     * @throws IOException               if it's not possible to read from the InputStream.
     */
    public ShapeFileReader(final InputStream is)
            throws InvalidShapeFileException, IOException {
        ValidationPreferences rules = new ValidationPreferences();
        initialize(is, rules);
    }

    /**
     * <p>
     * Reads a Shape File from an InputStream using the specified validation
     * preferences. Use this constructor when you want to relax or change the
     * validation preferences.
     * </p>
     *
     * <p>
     * The constructor will automatically read the header of the file. Thereafter,
     * use the method next() to read all shapes.
     * </p>
     *
     * @param is          the InputStream to be read.
     * @param preferences Customized validation preferences.
     * @throws InvalidShapeFileException if the data is malformed, according to the specified preferences.
     * @throws IOException               if it's not possible to read from the InputStream.
     */
    public ShapeFileReader(final InputStream is,
                           final ValidationPreferences preferences)
            throws InvalidShapeFileException, IOException {
        initialize(is, preferences);
    }

    private void initialize(final InputStream is,
                            final ValidationPreferences preferences) throws IOException,
            InvalidShapeFileException {
        if (is == null) {
            throw new RuntimeException(
                    "Must specify a non-null input stream to read from.");
        }
        if (preferences == null) {
            throw new RuntimeException("Must specify non-null rules.");
        }
        this.is = new BufferedInputStream(is);
        this.rules = preferences;
        this.eofReached = false;
        this.header = new ShapeFileHeader(this.is, this.rules);
    }

    // Methods

    /**
     * Reads one shape from the InputStream.
     *
     * @return a shape object, or null when the end of the stream is reached. The
     * returned shape object will be of one of the following classes:
     * <ul>
     * <li>NullShape,</li>
     * <li>PointShape,</li>
     * <li>PolylineShape,</li>
     * <li>PolygonShape,</li>
     * <li>MultiPointPlainShape,</li>
     * <li>PointZShape,</li>
     * <li>PolylineZShape,</li>
     * <li>PolygonZShape,</li>
     * <li>MultiPointZShape,</li>
     * <li>PointMShape,</li>
     * <li>PolylineMShape,</li>
     * <li>PolygonMShape,</li>
     * <li>MultiPointMShape,</li>
     * <li>or MultiPatchShape.</li>
     * </ul>
     * The method getShapeType() of the AbstractShape object provides the
     * shape type, in order to to cast the object to the appropriate
     * class.
     * @throws InvalidShapeFileException if the data is malformed.
     * @throws IOException               if it's not possible to read from the InputStream.
     */
    public AbstractShape next() throws IOException, InvalidShapeFileException {

        if (this.eofReached) {
            return null;
        }

        this.rules.advanceOneRecordNumber();

        // Shape header

        ShapeHeader shapeHeader = null;
        ShapeType shapeType = null;

        try {
            shapeHeader = new ShapeHeader(this.is, this.rules);
        } catch (DataStreamEOFException e) {
            this.eofReached = true;
            return null;
        }

        // Shape body

        try {
            int typeId = ISUtil.readLeInt(this.is);
            if (this.rules.getForceShapeType() != null) {
                shapeType = this.rules.getForceShapeType();
            } else {
                shapeType = ShapeType.parse(typeId);
                if (shapeType == null) {
                    throw new InvalidShapeFileException("Invalid shape type '" + typeId
                            + "'. " + "The shape type can be forced using "
                            + "the additional constructor with " + "ValidationRules.");
                }
                if (!this.rules.isAllowMultipleShapeTypes()
                        && !this.header.getShapeType().equals(shapeType)) {
                    throw new InvalidShapeFileException("Invalid shape type '"
                            + shapeType + "'. All included shapes must have the same "
                            + "type as the one specified on the file header ("
                            + this.header.getShapeType()
                            + "). This validation can be disabled using the "
                            + "additional constructor with ValidationRules.");
                }
            }

        } catch (EOFException e) {
            throw new InvalidShapeFileException("Unexpected end of stream. "
                    + "The data is too short for the shape that was being read.");
        }

        try {
            switch (shapeType) {
                case NULL:
                    return new NullShape(shapeHeader, shapeType, this.is, this.rules);

                case POINT:
                    return new PointShape(shapeHeader, shapeType, this.is, this.rules);
                case POLYLINE:
                    return new PolylineShape(shapeHeader, shapeType, this.is, this.rules);
                case POLYGON:
                    return new PolygonShape(shapeHeader, shapeType, this.is, this.rules);
                case MULTIPOINT:
                    return new MultiPointPlainShape(shapeHeader, shapeType, this.is,
                            this.rules);

                case POINT_Z:
                    return new PointZShape(shapeHeader, shapeType, this.is, this.rules);
                case POLYLINE_Z:
                    return new PolylineZShape(shapeHeader, shapeType, this.is, this.rules);
                case POLYGON_Z:
                    return new PolygonZShape(shapeHeader, shapeType, this.is, this.rules);
                case MULTIPOINT_Z:
                    return new MultiPointZShape(shapeHeader, shapeType, this.is, this.rules);

                case POINT_M:
                    return new PointMShape(shapeHeader, shapeType, this.is, this.rules);
                case POLYLINE_M:
                    return new PolylineMShape(shapeHeader, shapeType, this.is, this.rules);
                case POLYGON_M:
                    return new PolygonMShape(shapeHeader, shapeType, this.is, this.rules);
                case MULTIPOINT_M:
                    return new MultiPointMShape(shapeHeader, shapeType, this.is, this.rules);

                case MULTIPATCH:
                    return new MultiPatchShape(shapeHeader, shapeType, this.is, this.rules);

                default:
                    throw new InvalidShapeFileException("Unexpected shape type '"
                            + shapeType + "'");
            }

        } catch (EOFException e) {
            throw new InvalidShapeFileException("Unexpected end of stream. "
                    + "The data is too short for the last shape (" + shapeType
                    + ") that was being read.");
        }
    }

    // Getters

    /**
     * Returns the shape's header.
     *
     * @return shape's header.
     */
    public ShapeFileHeader getHeader() {
        return header;
    }

}
