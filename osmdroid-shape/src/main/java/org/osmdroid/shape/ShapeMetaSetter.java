package org.osmdroid.shape;

import android.graphics.Paint;

import net.iryndin.jdbf.core.DbfRecord;

import org.osmdroid.views.overlay.Overlay;

import java.text.ParseException;

public interface ShapeMetaSetter {
    void set(DbfRecord metadata, Overlay overlay) throws ParseException;
    String getSnippet();
    String getTitle();
    String getSubDescription();
    Paint getFillPaint();
    Paint getStrokePaint();
}
