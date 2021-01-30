// Created by plusminus on 23:18:23 - 02.10.2008
package org.osmdroid.views.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import org.osmdroid.util.RectL;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws a list of {@link OverlayItem} as markers to a map. The item with the lowest index is drawn
 * as last and therefore the 'topmost' marker. It also gets checked for onTap first. This class is
 * generic, because you then you get your custom item-class passed back in onTap().
 *
 * @param <Item>
 * @author Marc Kurtz
 * @author Nicolas Gramlich
 * @author Theodore Hong
 * @author Fred Eisele
 */
public abstract class ItemizedOverlay<Item extends OverlayItem> extends Overlay implements
        Overlay.Snappable {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================
    protected int mDrawnItemsLimit = Integer.MAX_VALUE;
    protected final Drawable mDefaultMarker;
    private final ArrayList<Item> mInternalItemList;
    private boolean[] mInternalItemDisplayedList;
    private final Rect mRect = new Rect();
    private final Rect mMarkerRect = new Rect();
    private final Rect mOrientedMarkerRect = new Rect();
    private final Point mCurScreenCoords = new Point();
    protected boolean mDrawFocusedItem = true;
    private Item mFocusedItem;
    private boolean mPendingFocusChangedEvent = false;
    private OnFocusChangeListener mOnFocusChangeListener;

    private Rect itemRect = new Rect();
    private Rect screenRect = new Rect();

    // ===========================================================
    // Abstract methods
    // ===========================================================

    /**
     * Method by which subclasses create the actual Items. This will only be called from populate()
     * we'll cache them for later use.
     */
    protected abstract Item createItem(int i);

    /**
     * The number of items in this overlay.
     */
    public abstract int size();

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * Use {@link #ItemizedOverlay(Drawable)} instead
     */
    @Deprecated
    public ItemizedOverlay(Context ctx, final Drawable pDefaultMarker) {
        this(pDefaultMarker);
    }

    public ItemizedOverlay(final Drawable pDefaultMarker) {

        super();
        if (pDefaultMarker == null) {
            throw new IllegalArgumentException("You must pass a default marker to ItemizedOverlay.");
        }

        this.mDefaultMarker = pDefaultMarker;

        mInternalItemList = new ArrayList<Item>();
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public int getDrawnItemsLimit() {
        return this.mDrawnItemsLimit;
    }

    public void setDrawnItemsLimit(final int aLimit) {
        this.mDrawnItemsLimit = aLimit;
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces (and supporting methods)
    // ===========================================================

    @Override
    public void onDetach(MapView mapView) {
        if (mDefaultMarker != null) {
            //release the bitmap
        }
    }

    /**
     * Draw a marker on each of our items. populate() must have been called first.<br>
     * <br>
     * The marker will be drawn twice for each Item in the Overlay--once in the shadow phase, skewed
     * and darkened, then again in the non-shadow phase. The bottom-center of the marker will be
     * aligned with the geographical coordinates of the Item.<br>
     * <br>
     * The order of drawing may be changed by overriding the getIndexToDraw(int) method. An item may
     * provide an alternate marker via its OverlayItem.getMarker(int) method. If that method returns
     * null, the default marker is used.<br>
     * <br>
     * The focused item is always drawn last, which puts it visually on top of the other items.<br>
     */
    @Override
    public void draw(final Canvas canvas, final Projection pj) {
        if (mPendingFocusChangedEvent && mOnFocusChangeListener != null)
            mOnFocusChangeListener.onFocusChanged(this, mFocusedItem);
        mPendingFocusChangedEvent = false;

        final int size = Math.min(this.mInternalItemList.size(), mDrawnItemsLimit);

        if (mInternalItemDisplayedList == null || mInternalItemDisplayedList.length != size) {
            mInternalItemDisplayedList = new boolean[size];
        }

        /* Draw in backward cycle, so the items with the least index are on the front. */
        for (int i = size - 1; i >= 0; i--) {
            final Item item = getItem(i);
            if (item == null) {
                continue;
            }

            pj.toPixels(item.getPoint(), mCurScreenCoords);
            calculateItemRect(item, mCurScreenCoords, itemRect);

            mInternalItemDisplayedList[i] = onDrawItem(canvas, item, mCurScreenCoords, pj);
        }
    }

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * Utility method to perform all processing on a new ItemizedOverlay. Subclasses provide Items
     * through the createItem(int) method. The subclass should call this as soon as it has data,
     * before anything else gets called.
     */
    protected final void populate() {
        final int size = size();
        mInternalItemList.clear();
        mInternalItemList.ensureCapacity(size);
        for (int a = 0; a < size; a++) {
            mInternalItemList.add(createItem(a));
        }
        mInternalItemDisplayedList = null;
    }

    /**
     * Returns the Item at the given index.
     *
     * @param position the position of the item to return
     * @return the Item of the given index, or null if not found at position
     */
    public final Item getItem(final int position) {
        try {
            return mInternalItemList.get(position);
        } catch (final IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Draws an item located at the provided screen coordinates to the canvas.
     *
     * @param canvas          what the item is drawn upon
     * @param item            the item to be drawn
     * @param curScreenCoords
     * @param pProjection
     * @return true if the item was actually drawn
     */
    protected boolean onDrawItem(final Canvas canvas, final Item item, final Point curScreenCoords,
                                 final Projection pProjection) {


        final int state = (mDrawFocusedItem && (mFocusedItem == item) ? OverlayItem.ITEM_STATE_FOCUSED_MASK
                : 0);
        final Drawable marker = (item.getMarker(state) == null) ? getDefaultMarker(state) : item
                .getMarker(state);
        final HotspotPlace hotspot = item.getMarkerHotspot();

        boundToHotspot(marker, hotspot);

        int x = mCurScreenCoords.x;
        int y = mCurScreenCoords.y;

        marker.copyBounds(mRect);
        mMarkerRect.set(mRect);
        mRect.offset(x, y);
        RectL.getBounds(mRect, x, y, pProjection.getOrientation(), mOrientedMarkerRect);
        final boolean displayed = Rect.intersects(mOrientedMarkerRect, canvas.getClipBounds());
        if (displayed) {
            if (pProjection.getOrientation() != 0) { // optimization: step 1/2
                canvas.save();
                canvas.rotate(-pProjection.getOrientation(), x, y);
            }
            marker.setBounds(mRect);
            marker.draw(canvas);
            if (pProjection.getOrientation() != 0) { // optimization: step 2/2
                canvas.restore();
            }
            marker.setBounds(mMarkerRect);
        }

        return displayed;
    }

    /**
     * Get the list of all the items that are currently drawn on the canvas.
     * The obvious use case is a "share" or "export" button on a map, restricted to what is displayed.
     * The order of the items is kept
     *
     * @return the items that have actually been drawn
     * @since 5.6.7
     */
    public List<Item> getDisplayedItems() {
        final List<Item> result = new ArrayList<>();
        if (mInternalItemDisplayedList == null) {
            return result;
        }
        for (int i = 0; i < mInternalItemDisplayedList.length; i++) {
            if (mInternalItemDisplayedList[i]) {
                result.add(getItem(i));
            }
        }
        return result;
    }

    protected Drawable getDefaultMarker(final int state) {
        OverlayItem.setState(mDefaultMarker, state);
        return mDefaultMarker;
    }

    /**
     * See if a given hit point is within the bounds of an item's marker. Override to modify the way
     * an item is hit tested. The hit point is relative to the marker's bounds. The default
     * implementation just checks to see if the hit point is within the touchable bounds of the
     * marker.
     *
     * @param item   the item to hit test
     * @param marker the item's marker
     * @param hitX   x coordinate of point to check
     * @param hitY   y coordinate of point to check
     * @return true if the hit point is within the marker
     */
    protected boolean hitTest(final Item item, final android.graphics.drawable.Drawable marker, final int hitX,
                              final int hitY) {
        return marker.getBounds().contains(hitX, hitY);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
        final int size = this.size();
        final int eventX = Math.round(e.getX());
        final int eventY = Math.round(e.getY());
        for (int i = 0; i < size; i++) {
            if (isEventOnItem(getItem(i), eventX, eventY, mapView)) {
                if (onTap(i)) {
                    // We got a response so consume the event
                    return true;
                }
            }
        }

        return super.onSingleTapConfirmed(e, mapView);
    }

    /**
     * Override this method to handle a "tap" on an item. This could be from a touchscreen tap on an
     * onscreen Item, or from a trackball click on a centered, selected Item. By default, does
     * nothing and returns false.
     *
     * @return true if you handled the tap, false if you want the event that generated it to pass to
     * other overlays.
     */
    protected boolean onTap(int index) {
        return false;
    }

    /**
     * Set whether or not to draw the focused item. The default is to draw it, but some clients may
     * prefer to draw the focused item themselves.
     */
    public void setDrawFocusedItem(final boolean drawFocusedItem) {
        mDrawFocusedItem = drawFocusedItem;
    }

    /**
     * If the given Item is found in the overlay, force it to be the current focus-bearer. Any
     * registered {@link OnFocusChangeListener} will be notified. This does not move the map, so if
     * the Item isn't already centered, the user may get confused. If the Item is not found, this is
     * a no-op. You can also pass null to remove focus.
     */
    public void setFocus(final Item item) {
        mPendingFocusChangedEvent = item != mFocusedItem;
        mFocusedItem = item;
    }

    /**
     * @return the currently-focused item, or null if no item is currently focused.
     */
    public Item getFocus() {
        return mFocusedItem;
    }

    /**
     * Adjusts a drawable's bounds so that (0,0) is a pixel in the location described by the hotspot
     * parameter. Useful for "pin"-like graphics. For convenience, returns the same drawable that
     * was passed in.
     *
     * @param marker  the drawable to adjust
     * @param hotspot the hotspot for the drawable
     * @return the same drawable that was passed in.
     */
    protected Drawable boundToHotspot(final Drawable marker, HotspotPlace hotspot) {
        if (hotspot == null) {
            hotspot = HotspotPlace.BOTTOM_CENTER;
        }
        final int markerWidth = marker.getIntrinsicWidth();
        final int markerHeight = marker.getIntrinsicHeight();
        final int offsetX;
        final int offsetY;
        switch (hotspot) {
            default:
            case NONE:
            case LEFT_CENTER:
            case UPPER_LEFT_CORNER:
            case LOWER_LEFT_CORNER:
                offsetX = 0;
                break;
            case CENTER:
            case BOTTOM_CENTER:
            case TOP_CENTER:
                offsetX = -markerWidth / 2;
                break;
            case RIGHT_CENTER:
            case UPPER_RIGHT_CORNER:
            case LOWER_RIGHT_CORNER:
                offsetX = -markerWidth;
                break;
        }
        switch (hotspot) {
            default:
            case NONE:
            case TOP_CENTER:
            case UPPER_LEFT_CORNER:
            case UPPER_RIGHT_CORNER:
                offsetY = 0;
                break;
            case CENTER:
            case RIGHT_CENTER:
            case LEFT_CENTER:
                offsetY = -markerHeight / 2;
                break;
            case BOTTOM_CENTER:
            case LOWER_RIGHT_CORNER:
            case LOWER_LEFT_CORNER:
                offsetY = -markerHeight;
                break;
        }
        marker.setBounds(offsetX, offsetY, offsetX + markerWidth, offsetY + markerHeight);
        return marker;
    }

    /**
     * Calculates the screen rect for an item.
     *
     * @param item
     * @param coords
     * @param reuse
     * @return
     */
    protected Rect calculateItemRect(Item item, Point coords, Rect reuse) {
        final Rect out = reuse != null ? reuse : new Rect();

        HotspotPlace hotspot = item.getMarkerHotspot();
        if (hotspot == null) {
            hotspot = HotspotPlace.BOTTOM_CENTER;
        }

        final int state = (mDrawFocusedItem && (mFocusedItem == item) ? OverlayItem.ITEM_STATE_FOCUSED_MASK : 0);
        final Drawable marker = (item.getMarker(state) == null) ? getDefaultMarker(state) : item.getMarker(state);
        int itemWidth = marker.getIntrinsicWidth();
        int itemHeight = marker.getIntrinsicHeight();

        switch (hotspot) {
            case NONE:
                out.set(coords.x - itemWidth / 2,
                        coords.y - itemHeight / 2,
                        coords.x + itemWidth / 2,
                        coords.y + itemHeight / 2);
                break;
            case CENTER:
                out.set(coords.x - itemWidth / 2,
                        coords.y - itemHeight / 2,
                        coords.x + itemWidth / 2,
                        coords.y + itemHeight / 2);
                break;
            case BOTTOM_CENTER:
                out.set(coords.x - itemWidth / 2,
                        coords.y - itemHeight,
                        coords.x + itemWidth / 2,
                        coords.y);
                break;
            case TOP_CENTER:
                out.set(coords.x - itemWidth / 2,
                        coords.y,
                        coords.x + itemWidth / 2,
                        coords.y + itemHeight);
                break;
            case RIGHT_CENTER:
                out.set(coords.x - itemWidth,
                        coords.y - itemHeight / 2,
                        coords.x,
                        coords.y + itemHeight / 2);
                break;
            case LEFT_CENTER:
                out.set(coords.x,
                        coords.y - itemHeight / 2,
                        coords.x + itemWidth,
                        coords.y + itemHeight / 2);
                break;
            case UPPER_RIGHT_CORNER:
                out.set(coords.x - itemWidth,
                        coords.y,
                        coords.x,
                        coords.y + itemHeight);
                break;
            case LOWER_RIGHT_CORNER:
                out.set(coords.x - itemWidth,
                        coords.y - itemHeight,
                        coords.x,
                        coords.y);
                break;
            case UPPER_LEFT_CORNER:
                out.set(coords.x,
                        coords.y,
                        coords.x + itemWidth,
                        coords.y + itemHeight);
                break;
            case LOWER_LEFT_CORNER:
                out.set(coords.x,
                        coords.y - itemHeight,
                        coords.x + itemWidth,
                        coords.y);
                break;
        }

        return out;
    }

    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        mOnFocusChangeListener = l;
    }

    public static interface OnFocusChangeListener {
        void onFocusChanged(ItemizedOverlay<?> overlay, OverlayItem newFocus);
    }

    /**
     * @since 6.0.2
     */
    protected boolean isEventOnItem(final Item pItem, final int pEventX, final int pEventY, final MapView pMapView) {
        if (pItem == null) {
            return false;
        }
        pMapView.getProjection().toPixels(pItem.getPoint(), mCurScreenCoords);
        final int state = (mDrawFocusedItem && (mFocusedItem == pItem) ? OverlayItem.ITEM_STATE_FOCUSED_MASK : 0);
        Drawable marker = pItem.getMarker(state);
        if (marker == null) {
            marker = getDefaultMarker(state);
        }
        boundToHotspot(marker, pItem.getMarkerHotspot());
        marker.copyBounds(mRect);
        mRect.offset(mCurScreenCoords.x, mCurScreenCoords.y);
        RectL.getBounds(mRect, mCurScreenCoords.x, mCurScreenCoords.y, -pMapView.getMapOrientation(), mOrientedMarkerRect);
        return mOrientedMarkerRect.contains(pEventX, pEventY);
    }
}
