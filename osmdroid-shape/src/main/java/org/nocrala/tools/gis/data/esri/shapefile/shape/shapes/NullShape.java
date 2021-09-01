package org.nocrala.tools.gis.data.esri.shapefile.shape.shapes;

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.Const;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;

import java.io.InputStream;

/**
 * Represents a Null Shape object, as defined by the ESRI Shape file
 * specification.
 */
public class NullShape extends AbstractShape {

    private static final int FIXED_CONTENT_LENGTH = (4) / 2;

    public NullShape(final ShapeHeader shapeHeader, final ShapeType shapeType,
                     final InputStream is, final ValidationPreferences rules)
            throws InvalidShapeFileException {
        super(shapeHeader, shapeType, is, rules);

        if (!rules.isAllowBadContentLength()
                && this.header.getContentLength() != FIXED_CONTENT_LENGTH) {
            throw new InvalidShapeFileException(
                    "Invalid Null shape header's content length. " + "Expected "
                            + FIXED_CONTENT_LENGTH + " 16-bit words but found "
                            + this.header.getContentLength() + ". " + Const.PREFERENCES);
        }

    }

}
