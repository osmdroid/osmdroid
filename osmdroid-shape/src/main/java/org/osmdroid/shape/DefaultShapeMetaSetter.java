package org.osmdroid.shape;

import net.iryndin.jdbf.core.DbfRecord;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.nio.charset.Charset;
import java.text.ParseException;

public class DefaultShapeMetaSetter implements ShapeMetaSetter {

    private static String getSensibleTitle(String snippet) {
        if (snippet.length() > 100) {
            return snippet.substring(0, 96) + "...";
        }
        return snippet;
    }

    @Override
    public void set(DbfRecord metadata, Marker marker) throws ParseException {
        if (metadata != null) {
            metadata.setStringCharset(Charset.defaultCharset());
            marker.setSnippet(metadata.toMap().toString());
            marker.setTitle(getSensibleTitle(marker.getSnippet()));
        }
    }

    @Override
    public void set(DbfRecord metadata, Polygon polygon) throws ParseException {
        if (metadata != null) {
            metadata.setStringCharset(Charset.defaultCharset());
            polygon.setSnippet(metadata.toMap().toString());
            polygon.setTitle(getSensibleTitle(polygon.getSnippet()));
        }
        final BoundingBox boundingBox = polygon.getBounds();
        polygon.setSubDescription(boundingBox.toString());
    }

    @Override
    public void set(DbfRecord metadata, Polyline polyline) throws ParseException {
        if (metadata != null) {
            metadata.setStringCharset(Charset.defaultCharset());
            polyline.setSnippet(metadata.toMap().toString());
            polyline.setTitle(getSensibleTitle(polyline.getSnippet()));
        }
    }
}
