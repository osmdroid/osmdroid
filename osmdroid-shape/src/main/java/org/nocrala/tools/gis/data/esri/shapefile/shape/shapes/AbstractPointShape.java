package org.nocrala.tools.gis.data.esri.shapefile.shape.shapes;

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.shape.AbstractShape;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;
import org.nocrala.tools.gis.data.esri.shapefile.util.ISUtil;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractPointShape extends AbstractShape {

    private double x;
    private double y;

    public AbstractPointShape(final ShapeHeader shapeHeader,
                              final ShapeType shapeType, final InputStream is,
                              final ValidationPreferences rules) throws IOException {
        super(shapeHeader, shapeType, is, rules);

        this.x = ISUtil.readLeDouble(is);
        this.y = ISUtil.readLeDouble(is);
    }

    // Getters

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

}
