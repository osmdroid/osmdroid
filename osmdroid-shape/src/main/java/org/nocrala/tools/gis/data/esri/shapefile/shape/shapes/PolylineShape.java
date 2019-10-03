package org.nocrala.tools.gis.data.esri.shapefile.shape.shapes;

import java.io.IOException;
import java.io.InputStream;

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeHeader;
import org.nocrala.tools.gis.data.esri.shapefile.shape.ShapeType;

/**
 * Represents a Polyline Shape object, as defined by the ESRI Shape file
 * specification.
 * 
 */
public class PolylineShape extends AbstractPolyPlainShape {

  public PolylineShape(final ShapeHeader shapeHeader,
      final ShapeType shapeType, final InputStream is,
      final ValidationPreferences rules) throws IOException,
      InvalidShapeFileException {

    super(shapeHeader, shapeType, is, rules);

  }

  @Override
  protected String getShapeTypeName() {
    return "Polyline";
  }

}
