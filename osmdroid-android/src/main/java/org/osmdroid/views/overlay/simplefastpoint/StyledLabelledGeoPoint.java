package org.osmdroid.views.overlay.simplefastpoint;

import android.graphics.Paint;
import android.location.Location;

import org.osmdroid.util.GeoPoint;

/**
 * Created by miguel on 07-01-2018.
 */

public class StyledLabelledGeoPoint extends LabelledGeoPoint {
    Paint mPointStyle, mTextStyle;

    public StyledLabelledGeoPoint(double aLatitude, double aLongitude) {
        super(aLatitude, aLongitude);
    }

    public StyledLabelledGeoPoint(double aLatitude, double aLongitude, double aAltitude) {
        super(aLatitude, aLongitude, aAltitude);
    }

    public StyledLabelledGeoPoint(double aLatitude, double aLongitude, double aAltitude, String aLabel) {
        super(aLatitude, aLongitude, aAltitude, aLabel);
    }

    public StyledLabelledGeoPoint(Location aLocation) {
        super(aLocation);
    }

    public StyledLabelledGeoPoint(GeoPoint aGeopoint) {
        super(aGeopoint);
    }

    public StyledLabelledGeoPoint(double aLatitude, double aLongitude, String aLabel) {
        super(aLatitude, aLongitude, aLabel);
    }

    public StyledLabelledGeoPoint(double aLatitude, double aLongitude, String aLabel, Paint pointStyle, Paint textStyle) {
        super(aLatitude, aLongitude, aLabel);
        this.mPointStyle = pointStyle;
        this.mTextStyle = textStyle;
    }

    public StyledLabelledGeoPoint(double aLatitude, double aLongitude, double aAltitude, String aLabel, Paint pointStyle, Paint textStyle) {
        super(aLatitude, aLongitude, aAltitude, aLabel);
        this.mPointStyle = pointStyle;
        this.mTextStyle = textStyle;
    }

    public StyledLabelledGeoPoint(LabelledGeoPoint aLabelledGeopoint) {
        super(aLabelledGeopoint);
    }

    public Paint getPointStyle() {
        return mPointStyle;
    }

    public void setPointStyle(Paint mPointStyle) {
        this.mPointStyle = mPointStyle;
    }

    public Paint getTextStyle() {
        return mTextStyle;
    }

    public void setTextStyle(Paint mTextStyle) {
        this.mTextStyle = mTextStyle;
    }

    @Override
    public StyledLabelledGeoPoint clone() {
        return new StyledLabelledGeoPoint(this.getLatitude(), this.getLongitude(), this.getAltitude()
                , this.mLabel, this.mPointStyle, this.mTextStyle);
    }

}
