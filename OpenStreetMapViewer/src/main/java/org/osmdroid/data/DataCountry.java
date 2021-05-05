package org.osmdroid.data;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

/**
 * Data about a country, including its ISO 3166-1 alpha-3, its name and its capital (name + lan/lon)
 *
 * @author Fabrice Fontaine
 * @since 6.0.3
 */
public class DataCountry {

    private final String mISO3166_1_alpha_3;
    private final String mName;
    private final String mCapitalName;
    private final IGeoPoint mCapitalGeoPoint;

    public DataCountry(final String pISO3166_1_alpha_3, final String pName,
                       final String pCapitalName,
                       final double pCapitalLatitude, final double pCapitalLongitude) {
        mISO3166_1_alpha_3 = pISO3166_1_alpha_3;
        mName = pName;
        mCapitalName = pCapitalName;
        mCapitalGeoPoint = new GeoPoint(pCapitalLatitude, pCapitalLongitude);
    }

    public String getISO3166_1_alpha_3() {
        return mISO3166_1_alpha_3;
    }

    public String getName() {
        return mName;
    }

    public String getCapitalName() {
        return mCapitalName;
    }

    public IGeoPoint getCapitalGeoPoint() {
        return mCapitalGeoPoint;
    }
}
