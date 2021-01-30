// Created by plusminus on 17:41:55 - 16.10.2008
package org.osmdroid.util.constants;

import org.osmdroid.library.R;

public interface GeoConstants {
    // ===========================================================
    // Final Fields
    // ===========================================================

    int RADIUS_EARTH_METERS = 6378137; // http://en.wikipedia.org/wiki/Earth_radius#Equatorial_radius
    double METERS_PER_STATUTE_MILE = 1609.344; // http://en.wikipedia.org/wiki/Mile
    double METERS_PER_NAUTICAL_MILE = 1852; // http://en.wikipedia.org/wiki/Nautical_mile
    double FEET_PER_METER = 3.2808399; // http://en.wikipedia.org/wiki/Feet_%28unit_of_length%29
    @Deprecated
    int EQUATORCIRCUMFENCE = (int) (2 * Math.PI * RADIUS_EARTH_METERS);

    /**
     * @since 6.1.1
     */
    enum UnitOfMeasure {
        meter(1, R.string.format_distance_only_meter),
        kilometer(1000, R.string.format_distance_only_kilometer),
        statuteMile(GeoConstants.METERS_PER_STATUTE_MILE, R.string.format_distance_only_mile),
        nauticalMile(GeoConstants.METERS_PER_NAUTICAL_MILE, R.string.format_distance_only_nautical_mile),
        foot(1 / GeoConstants.FEET_PER_METER, R.string.format_distance_only_foot);

        private final double mConversionFactorToMeters;
        private final int mStringResId;

        UnitOfMeasure(final double pConversionFactorToMeters, final int pStringResId) {
            mConversionFactorToMeters = pConversionFactorToMeters;
            mStringResId = pStringResId;
        }

        public double getConversionFactorToMeters() {
            return mConversionFactorToMeters;
        }

        public int getStringResId() {
            return mStringResId;
        }
    }

    // ===========================================================
    // Methods
    // ===========================================================
}
