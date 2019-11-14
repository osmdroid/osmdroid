package org.nocrala.tools.gis.data.esri.shapefile.shape;

import java.io.InputStream;

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;

public abstract class AbstractShape {

  protected ShapeHeader header;
  protected ShapeType shapeType;

  public AbstractShape(final ShapeHeader shapeHeader,
      final ShapeType shapeType, final InputStream is,
      final ValidationPreferences rules) {
    this.header = shapeHeader;
    this.shapeType = shapeType;
  }

  // Getters

  public final ShapeHeader getHeader() {
    return header;
  }

  public ShapeType getShapeType() {
    return shapeType;
  }

}
