package org.osmdroid.shape;

import android.graphics.Paint;

import net.iryndin.jdbf.core.DbfRecord;

import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.PolyOverlayWithIW;
import org.osmdroid.views.overlay.Polygon;

import java.nio.charset.Charset;
import java.text.ParseException;

public class DefaultShapeMetaSetter implements ShapeMetaSetter {

    private String metaString;
    private String subString;
    private Overlay mOverlay;
    private String shapeType;

    @Override
    public void set(DbfRecord metadata, Overlay overlay) throws ParseException {
        if (metadata != null){
            metadata.setStringCharset(Charset.defaultCharset());
            metaString = metadata.toMap().toString();
        } else{
            metaString = null;
        }

        shapeType = overlay.getClass().getName();

        if (shapeType.equals("org.osmdroid.views.overlay.Marker"))
            subString = ((Marker) overlay).getPosition().toString();
        else if (shapeType.equals("org.osmdroid.views.overlay.Polygon") || shapeType.equals("org.osmdroid.views.overlay.Polyline"))
            subString = overlay.getBounds().toString();

        mOverlay = overlay;
    }

    @Override
    public String getSnippet() {
        return metaString;
    }

    @Override
    public String getTitle() {
        if (metaString.length() > 100) {
            return metaString.substring(0, 96) + "...";
        }
        return metaString;
    }

    @Override
    public String getSubDescription() {
        return subString;
    }

    @Override
    public Paint getFillPaint() {
        if (shapeType.equals("org.osmdroid.views.overlay.Polygon"))
            return ((Polygon) mOverlay).getFillPaint();
        return null;
    }

    @Override
    public Paint getStrokePaint() {
        if (shapeType.equals("org.osmdroid.views.overlay.Polygon") || shapeType.equals("org.osmdroid.views.overlay.Polyline"))
            return ((PolyOverlayWithIW) mOverlay).getOutlinePaint();
        return null;
    }
}
