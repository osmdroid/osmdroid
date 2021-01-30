package org.osmdroid.views.overlay.simplefastpoint;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import org.osmdroid.util.GeoPoint;

/**
 * A {@link GeoPoint} with a label.
 * Created by Miguel Porto on 12-11-2016.
 */

public class LabelledGeoPoint extends GeoPoint {
    String mLabel;

    public LabelledGeoPoint(double aLatitude, double aLongitude) {
        super(aLatitude, aLongitude);
    }

    public LabelledGeoPoint(double aLatitude, double aLongitude, double aAltitude) {
        super(aLatitude, aLongitude, aAltitude);
    }

    public LabelledGeoPoint(double aLatitude, double aLongitude, double aAltitude, String aLabel) {
        super(aLatitude, aLongitude, aAltitude);
        this.mLabel = aLabel;
    }

    public LabelledGeoPoint(Location aLocation) {
        super(aLocation);
    }

    public LabelledGeoPoint(GeoPoint aGeopoint) {
        super(aGeopoint);
    }

    public LabelledGeoPoint(double aLatitude, double aLongitude, String aLabel) {
        super(aLatitude, aLongitude);
        this.mLabel = aLabel;
    }

    public LabelledGeoPoint(LabelledGeoPoint aLabelledGeopoint) {
        this(aLabelledGeopoint.getLatitude(), aLabelledGeopoint.getLongitude()
                , aLabelledGeopoint.getAltitude(), aLabelledGeopoint.getLabel());
    }

    public String getLabel() {
        return this.mLabel;
    }

    public void setLabel(String label) {
        this.mLabel = label;
    }

    @Override
    public LabelledGeoPoint clone() {
        return new LabelledGeoPoint(this.getLatitude(), this.getLongitude(), this.getAltitude()
                , this.mLabel);
    }

    // ===========================================================
    // Parcelable
    // ===========================================================
    private LabelledGeoPoint(final Parcel in) {
        super(in.readDouble(), in.readDouble(), in.readDouble());
        this.setLabel(in.readString());
    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {
        super.writeToParcel(out, flags);
        out.writeString(mLabel);
    }

    public static final Parcelable.Creator<LabelledGeoPoint> CREATOR =
            new Parcelable.Creator<LabelledGeoPoint>() {
                @Override
                public LabelledGeoPoint createFromParcel(final Parcel in) {
                    return new LabelledGeoPoint(in);
                }

                @Override
                public LabelledGeoPoint[] newArray(final int size) {
                    return new LabelledGeoPoint[size];
                }
            };

}
