package org.osmdroid.bonuspack.kml;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;
import java.io.Writer;

/**
 * Handling of KML IconStyle hotSpot
 * @author M.Kergall
 */
public class HotSpot implements Parcelable {

    public static enum Units {
        fraction, pixels, insetPixels;
        private static Units[] allValues = values();
        public static Units fromOrdinal(int n) {return allValues[n];}
    };

    public float mx, my;
    public Units mXUnits, mYUnits;

    public Units getUnits(String sUnits){
        if ("fraction".equals(sUnits))
            return Units.fraction;
        else if ("pixels".equals(sUnits))
            return Units.pixels;
        else if ("insetPixels".equals(sUnits))
            return Units.insetPixels;
        else
            return Units.fraction;
    }

    public String getUnits(Units units){
        return ""+units;
    }

    public HotSpot(){
        mx = 0.5f;
        my = 0.5f;
        mXUnits = Units.fraction;
        mYUnits = Units.fraction;
    }

    public HotSpot(float x, float y, String xunits, String yunits){
        mx = x;
        my = y;
        mXUnits = getUnits(xunits);
        mYUnits = getUnits(yunits);
    }

    public float get(float dim, Units units, float iconSize){
        switch (units){
            case fraction:
                return dim;
            case pixels:
                return dim/iconSize;
            case insetPixels:
                return (iconSize-dim)/iconSize;
            default:
                return dim;
        }
    }

    public float getX(float iconSize){
        return get(mx, mXUnits, iconSize);
    }

    public float getY(float iconSize){
        return get(my, mYUnits, iconSize);
    }

    public void writeAsKML(Writer writer){
        try {
            writer.write("<hotSpot x=\"" + mx + "\" y=\"" + my + "\" "
                    + "xunits=\"" + getUnits(mXUnits) + "\" "
                    + "yunits=\"" + getUnits(mYUnits) + "\"/>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Parcelable implementation ------------

    @Override public int describeContents() { return 0; }

    @Override public void writeToParcel(Parcel out, int flags) {
        out.writeFloat(mx);
        out.writeFloat(my);
        out.writeInt(mXUnits.ordinal());
        out.writeInt(mYUnits.ordinal());
    }

    public static final Creator<HotSpot> CREATOR = new Creator<HotSpot>() {
        @Override
        public HotSpot createFromParcel(Parcel in) {
            return new HotSpot(in);
        }

        @Override
        public HotSpot[] newArray(int size) {
            return new HotSpot[size];
        }
    };

    public HotSpot(Parcel in){
        mx = in.readFloat();
        my = in.readFloat();
        mXUnits = Units.fromOrdinal(in.readInt());
        mYUnits = Units.fromOrdinal(in.readInt());
    }
}
