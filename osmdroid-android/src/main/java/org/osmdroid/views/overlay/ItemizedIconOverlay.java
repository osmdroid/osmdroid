package org.osmdroid.views.overlay;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import org.osmdroid.api.IMapView;
import org.osmdroid.library.R;
import org.osmdroid.views.MapView;

import java.util.List;

public class ItemizedIconOverlay<Item extends OverlayItem> extends ItemizedOverlay<Item> {

    protected List<Item> mItemList;
    protected OnItemGestureListener<Item> mOnItemGestureListener;

    public ItemizedIconOverlay(
            final List<Item> pList,
            final Drawable pDefaultMarker,
            final org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener<Item> pOnItemGestureListener,
            final Context pContext) {
        super(pDefaultMarker);

        this.mItemList = pList;
        this.mOnItemGestureListener = pOnItemGestureListener;
        populate();
    }

    public ItemizedIconOverlay(
            final List<Item> pList,
            final org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener<Item> pOnItemGestureListener,
            final Context pContext) {
        this(pList, pContext.getResources().getDrawable(R.drawable.marker_default), pOnItemGestureListener,
                pContext);
    }

    public ItemizedIconOverlay(
            final Context pContext,
            final List<Item> pList,
            final org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener<Item> pOnItemGestureListener) {
        this(pList, pContext.getResources().getDrawable(R.drawable.marker_default),
                pOnItemGestureListener, pContext);
    }

    @Override
    public void onDetach(MapView mapView) {
        if (mItemList != null)
            mItemList.clear();
        mItemList = null;
        mOnItemGestureListener = null;
    }

    @Override
    public boolean onSnapToItem(final int pX, final int pY, final Point pSnapPoint, final IMapView pMapView) {
        // TODO Implement this!
        return false;
    }

    @Override
    protected Item createItem(final int index) {
        return mItemList.get(index);
    }

    @Override
    public int size() {
        return Math.min(mItemList.size(), mDrawnItemsLimit);
    }

    public boolean addItem(final Item item) {
        final boolean result = mItemList.add(item);
        populate();
        return result;
    }

    public void addItem(final int location, final Item item) {
        mItemList.add(location, item);
        populate();
    }

    public boolean addItems(final List<Item> items) {
        final boolean result = mItemList.addAll(items);
        populate();
        return result;
    }

    public void removeAllItems() {
        removeAllItems(true);
    }

    public void removeAllItems(final boolean withPopulate) {
        mItemList.clear();
        if (withPopulate) {
            populate();
        }
    }

    public boolean removeItem(final Item item) {
        final boolean result = mItemList.remove(item);
        populate();
        return result;
    }

    public Item removeItem(final int position) {
        final Item result = mItemList.remove(position);
        populate();
        return result;
    }

    /**
     * Each of these methods performs a item sensitive check. If the item is located its
     * corresponding method is called. The result of the call is returned.
     * <p>
     * Helper methods are provided so that child classes may more easily override behavior without
     * resorting to overriding the ItemGestureListener methods.
     */
    @Override
    public boolean onSingleTapConfirmed(final MotionEvent event, final MapView mapView) {
        return (activateSelectedItems(event, mapView, new ActiveItem() {
            @Override
            public boolean run(final int index) {
                final ItemizedIconOverlay<Item> that = ItemizedIconOverlay.this;
                if (that.mOnItemGestureListener == null) {
                    return false;
                }
                return onSingleTapUpHelper(index, that.mItemList.get(index), mapView);
            }
        })) ? true : super.onSingleTapConfirmed(event, mapView);
    }

    protected boolean onSingleTapUpHelper(final int index, final Item item, final MapView mapView) {
        return this.mOnItemGestureListener.onItemSingleTapUp(index, item);
    }

    @Override
    public boolean onLongPress(final MotionEvent event, final MapView mapView) {
        return (activateSelectedItems(event, mapView, new ActiveItem() {
            @Override
            public boolean run(final int index) {
                final ItemizedIconOverlay<Item> that = ItemizedIconOverlay.this;
                if (that.mOnItemGestureListener == null) {
                    return false;
                }
                return onLongPressHelper(index, getItem(index));
            }
        })) ? true : super.onLongPress(event, mapView);
    }

    protected boolean onLongPressHelper(final int index, final Item item) {
        return this.mOnItemGestureListener.onItemLongPress(index, item);
    }

    /**
     * When a content sensitive action is performed the content item needs to be identified. This
     * method does that and then performs the assigned task on that item.
     *
     * @param event
     * @param mapView
     * @param task
     * @return true if event is handled false otherwise
     */
    private boolean activateSelectedItems(final MotionEvent event, final MapView mapView,
                                          final ActiveItem task) {
        final int eventX = Math.round(event.getX());
        final int eventY = Math.round(event.getY());
        for (int i = 0; i < this.mItemList.size(); ++i) {
            if (isEventOnItem(getItem(i), eventX, eventY, mapView)) {
                if (task.run(i)) {
                    return true;
                }
            }
        }
        return false;
    }


    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    /**
     * When the item is touched one of these methods may be invoked depending on the type of touch.
     * <p>
     * Each of them returns true if the event was completely handled.
     */
    public static interface OnItemGestureListener<T> {
        public boolean onItemSingleTapUp(final int index, final T item);

        public boolean onItemLongPress(final int index, final T item);
    }

    public static interface ActiveItem {
        public boolean run(final int aIndex);
    }
}
