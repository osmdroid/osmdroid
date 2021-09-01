package org.nocrala.tools.gis.data.esri.shapefile.shape.shapes;

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.shape.Const;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a Point Shape object, as defined by the ESRI Shape file
 * specification.
 */
public class PointShape extends AbstractPointShape {

    private static final int FIXED_CONTENT_LENGTH = (4 + 8 + 8) / 2;

    public PointShape(final ShapeHeader shapeHeader, final ShapeType shapeType,
                      final InputStream is, final ValidationPreferences rules)
            throws IOException, InvalidShapeFileException {
        super(shapeHeader, shapeType, is, rules);

        if (!rules.isAllowBadContentLength()
                && this.header.getContentLength() != FIXED_CONTENT_LENGTH) {
            throw new InvalidShapeFileException(
                    "Invalid Point shape header's content length. " + "Expected "
                            + FIXED_CONTENT_LENGTH + " 16-bit words but found "
                            + this.header.getContentLength() + ". " + Const.PREFERENCES);
        }
    }

}
