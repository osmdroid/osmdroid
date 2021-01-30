package org.osmdroid.data;

import org.osmdroid.util.BoundingBox;

/**
 * Data about a geo region, including its ISO 3166, its name and its geo bounding box
 *
 * @author Fabrice Fontaine
 * @since 6.0.2
 */
public class DataRegion {

    private final String mISO3166;
    private final String mName;
    private final BoundingBox mBox;

    public DataRegion(final String pISO3166, final String pName, final BoundingBox pBox) {
        mISO3166 = pISO3166;
        mName = pName;
        mBox = pBox;
    }

    public String getISO3166() {
        return mISO3166;
    }

    public String getName() {
        return mName;
    }

    public BoundingBox getBox() {
        return mBox;
    }
}
