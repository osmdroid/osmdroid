package org.osmdroid.shape;

import net.iryndin.jdbf.core.DbfRecord;

import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.text.ParseException;

public interface ShapeMetaSetter {
    void set(DbfRecord metadata, Marker marker) throws ParseException;

    void set(DbfRecord metadata, Polygon polygon) throws ParseException;

    void set(DbfRecord metadata, Polyline polyline) throws ParseException;
}
