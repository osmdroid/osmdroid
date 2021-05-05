package org.nocrala.tools.gis.data.esri.shapefile.shape;

import org.nocrala.tools.gis.data.esri.shapefile.ValidationPreferences;
import org.nocrala.tools.gis.data.esri.shapefile.exception.DataStreamEOFException;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;
import org.nocrala.tools.gis.data.esri.shapefile.util.ISUtil;

import java.io.IOException;
import java.io.InputStream;

public class ShapeHeader {

    private int recordNumber;
    private int contentLength;

    public ShapeHeader(final InputStream is, final ValidationPreferences rules)
            throws DataStreamEOFException, IOException, InvalidShapeFileException {

        this.recordNumber = ISUtil.readBeIntMaybeEOF(is);
        if (!rules.isAllowBadRecordNumbers()) {
            if (this.recordNumber != rules.getExpectedRecordNumber()) {
                throw new InvalidShapeFileException("Invalid record number. Expected "
                        + rules.getExpectedRecordNumber() + " but found "
                        + this.recordNumber + ".");
            }
        }

        this.contentLength = ISUtil.readBeInt(is);
    }

    public int getRecordNumber() {
        return recordNumber;
    }

    public int getContentLength() {
        return contentLength;
    }

}
