package org.osmdroid.shape;

import net.iryndin.jdbf.core.DbfRecord;

import org.osmdroid.util.BoundingBox;

import java.nio.charset.Charset;
import java.text.ParseException;

public abstract class ShapeMetadataReader {
    String metaString;
    String boxString;

    public void read(DbfRecord metadata) throws ParseException {
        metadata.setStringCharset(Charset.defaultCharset());
        metaString = metadata.toMap().toString();
    }

    public void setBoundingBox(BoundingBox boundingBox){
        boxString = boundingBox.toString();
    }

    public abstract String getSnippet();
    public abstract String getTitle();
    public abstract String getSubDescription();
}
