package org.osmdroid.shape;

import android.graphics.Color;
import android.graphics.Paint;

import net.iryndin.jdbf.core.DbfRecord;

import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.nio.charset.Charset;
import java.text.ParseException;

public class DefaultShapeMetaSetter implements ShapeMetaSetter {

    private String readDbf(DbfRecord metadata) throws ParseException {
        if (metadata != null) {
            metadata.setStringCharset(Charset.defaultCharset());
            return metadata.toMap().toString();
        } else {
            return "";
        }
    }

    private String getTitle(String metaString) {
        if (metaString.length() > 100) {
            return metaString.substring(0, 96) + "...";
        }
        return metaString;
    }

    @Override
    public void set(DbfRecord metadata, Marker marker) throws ParseException {
        String snippet = readDbf(metadata);
        marker.setSnippet(snippet);
        marker.setTitle(getTitle(snippet));
        marker.setSubDescription(marker.getPosition().toString());
    }

    @Override
    public void set(DbfRecord metadata, Polygon polygon) throws ParseException {
        String snippet = readDbf(metadata);
        polygon.setSnippet(snippet);
        polygon.setTitle(getTitle(snippet));
        polygon.setSubDescription(polygon.getBounds().toString());
        polygon.getFillPaint().setColor(Color.TRANSPARENT);
        Paint paint = polygon.getOutlinePaint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
    }

    @Override
    public void set(DbfRecord metadata, Polyline polyline) throws ParseException {
        String snippet = readDbf(metadata);
        polyline.setSnippet(snippet);
        polyline.setTitle(getTitle(snippet));
        polyline.setSubDescription(polyline.getBounds().toString());
        Paint paint = polyline.getOutlinePaint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
    }
}
