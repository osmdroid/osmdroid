// Created by plusminus on 00:02:58 - 03.10.2008
package org.osmdroid.views.overlay;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

import org.osmdroid.api.IGeoPoint;

/**
 * An Item that can be displayed in a {@link ItemizedOverlay} or {@link ItemizedIconOverlay}.
 * <p>
 * Immutable class describing a GeoPoint with a Title and a Description.
 *
 * @author Nicolas Gramlich
 * @author Theodore Hong
 * @author Fred Eisele
 */
public class OverlayItem {

    // ===========================================================
    // Constants
    // ===========================================================
    public static final int ITEM_STATE_FOCUSED_MASK = 4;
    public static final int ITEM_STATE_PRESSED_MASK = 1;
    public static final int ITEM_STATE_SELECTED_MASK = 2;

    protected static final Point DEFAULT_MARKER_SIZE = new Point(26, 94);

    /**
     * Indicates a hotspot for an area. This is where the origin (0,0) of a point will be located
     * relative to the area. In otherwords this acts as an offset. NONE indicates that no adjustment
     * should be made.
     */
    public enum HotspotPlace {
        NONE, CENTER, BOTTOM_CENTER, TOP_CENTER, RIGHT_CENTER, LEFT_CENTER, UPPER_RIGHT_CORNER, LOWER_RIGHT_CORNER, UPPER_LEFT_CORNER, LOWER_LEFT_CORNER
    }

    // ===========================================================
    // Fields
    // ===========================================================

    protected final String mUid;
    protected final String mTitle;
    protected final String mSnippet;
    protected final IGeoPoint mGeoPoint;
    protected Drawable mMarker;
    protected HotspotPlace mHotspotPlace;

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * @param aTitle    this should be <b>singleLine</b> (no <code>'\n'</code> )
     * @param aSnippet  a <b>multiLine</b> description ( <code>'\n'</code> possible)
     * @param aGeoPoint
     */
    public OverlayItem(final String aTitle, final String aSnippet, final IGeoPoint aGeoPoint) {
        this(null, aTitle, aSnippet, aGeoPoint);
    }

    public OverlayItem(final String aUid, final String aTitle, final String aDescription,
                       final IGeoPoint aGeoPoint) {
        this.mTitle = aTitle;
        this.mSnippet = aDescription;
        this.mGeoPoint = aGeoPoint;
        this.mUid = aUid;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    public String getUid() {
        return mUid;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSnippet() {
        return mSnippet;
    }

    public IGeoPoint getPoint() {
        return mGeoPoint;
    }

    /*
     * (copied from Google API docs) Returns the marker that should be used when drawing this item
     * on the map. A null value means that the default marker should be drawn. Different markers can
     * be returned for different states. The different markers can have different bounds. The
     * default behavior is to call {@link setState(android.graphics.drawable.Drawable, int)} on the
     * overlay item's marker, if it exists, and then return it.
     *
     * @param stateBitset The current state.
     *
     * @return The marker for the current state, or null if the default marker for the overlay
     * should be used.
     */
    public Drawable getMarker(final int stateBitset) {
        // marker not specified
        if (mMarker == null) {
            return null;
        }

        // set marker state appropriately
        setState(mMarker, stateBitset);
        return mMarker;
    }

    public void setMarker(final Drawable marker) {
        this.mMarker = marker;
    }

    public void setMarkerHotspot(final HotspotPlace place) {
        this.mHotspotPlace = (place == null) ? HotspotPlace.BOTTOM_CENTER : place;
    }

    public HotspotPlace getMarkerHotspot() {
        return this.mHotspotPlace;
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================
    /*
     * (copied from the Google API docs) Sets the state of a drawable to match a given state bitset.
     * This is done by converting the state bitset bits into a state set of R.attr.state_pressed,
     * R.attr.state_selected and R.attr.state_focused attributes, and then calling {@link
     * Drawable.setState(int[])}.
     */
    public static void setState(final Drawable drawable, final int stateBitset) {
        final int[] states = new int[3];
        int index = 0;
        if ((stateBitset & ITEM_STATE_PRESSED_MASK) > 0)
            states[index++] = android.R.attr.state_pressed;
        if ((stateBitset & ITEM_STATE_SELECTED_MASK) > 0)
            states[index++] = android.R.attr.state_selected;
        if ((stateBitset & ITEM_STATE_FOCUSED_MASK) > 0)
            states[index++] = android.R.attr.state_focused;

        drawable.setState(states);
    }

    public Drawable getDrawable() {
        return this.mMarker;
    }

    public int getWidth() {
        return this.mMarker.getIntrinsicWidth();
    }

    public int getHeight() {
        return this.mMarker.getIntrinsicHeight();
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

}
