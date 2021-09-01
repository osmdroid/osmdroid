package org.nocrala.tools.gis.data.esri.shapefile.shape.shapes;

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.shape.Const;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a MultiPoint Shape object, as defined by the ESRI Shape file
 * specification.
 */
public class MultiPointPlainShape extends AbstractMultiPointShape {

    private static final int BASE_CONTENT_LENGTH = (4 + 8 * 4 + 4) / 2;

    public MultiPointPlainShape(final ShapeHeader shapeHeader,
                                final ShapeType shapeType, final InputStream is,
                                final ValidationPreferences rules) throws IOException,
            InvalidShapeFileException {

        super(shapeHeader, shapeType, is, rules);

        if (!rules.isAllowBadContentLength()) {
            int expectedLength = BASE_CONTENT_LENGTH
                    + (this.numberOfPoints * (8 * 2)) / 2;
            if (this.header.getContentLength() != expectedLength) {
                throw new InvalidShapeFileException("Invalid " + getShapeTypeName()
                        + " shape header's content length. " + "Expected " + expectedLength
                        + " 16-bit words (for " + this.numberOfPoints + " points)"
                        + " but found " + this.header.getContentLength() + ". "
                        + Const.PREFERENCES);
            }
        }

    }

    @Override
    protected String getShapeTypeName() {
        return "MultiPoint";
    }

}
