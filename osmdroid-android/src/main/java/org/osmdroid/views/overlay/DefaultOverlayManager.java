package org.osmdroid.views.overlay;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osmdroid.api.IMapView;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay.Snappable;

import android.graphics.Canvas;
import android.graphics.Point;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

public class DefaultOverlayManager extends AbstractList<Overlay> implements OverlayManager {

    private TilesOverlay mTilesOverlay;

    private final CopyOnWriteArrayList<Overlay> mOverlayList;

    public DefaultOverlayManager(final TilesOverlay tilesOverlay) {
        setTilesOverlay(tilesOverlay);
        mOverlayList = new CopyOnWriteArrayList<Overlay>();
    }

    @Override
    public Overlay get(final int pIndex) {
        return mOverlayList.get(pIndex);
    }

    @Override
    public int size() {
        return mOverlayList.size();
    }

    @Override
    public void add(final int pIndex, final Overlay pElement) {
        mOverlayList.add(pIndex, pElement);
    }

    @Override
    public Overlay remove(final int pIndex) {
        return mOverlayList.remove(pIndex);
    }

    @Override
    public Overlay set(final int pIndex, final Overlay pElement) {
        Overlay overlay = mOverlayList.set(pIndex, pElement);
        return overlay;
    }


    @Override
    public TilesOverlay getTilesOverlay() {
        return mTilesOverlay;
    }

    @Override
    public void setTilesOverlay(final TilesOverlay tilesOverlay) {
        mTilesOverlay = tilesOverlay;
    }

    @Override
    public Iterable<Overlay> overlaysReversed() {
        return new Iterable<Overlay>() {
            @Override
            public Iterator<Overlay> iterator() {
                final ListIterator<Overlay> i = mOverlayList.listIterator(mOverlayList.size());

                return new Iterator<Overlay>() {
                    @Override
                    public boolean hasNext() {
                        return i.hasPrevious();
                    }

                    @Override
                    public Overlay next() {
                        return i.previous();
                    }

                    @Override
                    public void remove() {
                        i.remove();
                    }
                };
            }
        };
    }

    @Override
    public List<Overlay> overlays() {
        return mOverlayList;
    }

    @Override
    public void onDraw(final Canvas c, final MapView pMapView) {
        if (mTilesOverlay != null && mTilesOverlay.isEnabled()) {
            mTilesOverlay.draw(c, pMapView, true);
        }

        if (mTilesOverlay != null && mTilesOverlay.isEnabled()) {
            mTilesOverlay.draw(c, pMapView, false);
        }

        for (final Overlay overlay : mOverlayList) {
            if (overlay.isEnabled()) {
                overlay.draw(c, pMapView, true);
            }
        }

        for (final Overlay overlay : mOverlayList) {
            if (overlay.isEnabled()) {
                overlay.draw(c, pMapView, false);
            }
        }
        //potential fix for #52 pMapView.invalidate();

    }

    @Override
    public void onDetach(final MapView pMapView) {
        if (mTilesOverlay != null) {
            mTilesOverlay.onDetach(pMapView);
        }

        for (final Overlay overlay : this.overlaysReversed()) {
            overlay.onDetach(pMapView);
        }
        this.clear();
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event, final MapView pMapView) {
        for (final Overlay overlay : this.overlaysReversed()) {
            if (overlay.onKeyDown(keyCode, event, pMapView)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event, final MapView pMapView) {
        for (final Overlay overlay : this.overlaysReversed()) {
            if (overlay.onKeyUp(keyCode, event, pMapView)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event, final MapView pMapView) {
        for (final Overlay overlay : this.overlaysReversed()) {
            if (overlay.onTouchEvent(event, pMapView)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onTrackballEvent(final MotionEvent event, final MapView pMapView) {
        for (final Overlay overlay : this.overlaysReversed()) {
            if (overlay.onTrackballEvent(event, pMapView)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onSnapToItem(final int x, final int y, final Point snapPoint, final IMapView pMapView) {
        for (final Overlay overlay : this.overlaysReversed()) {
            if (overlay instanceof Snappable) {
                if (((Snappable) overlay).onSnapToItem(x, y, snapPoint, pMapView)) {
                    return true;
                }
            }
        }

        return false;
    }

	/* GestureDetector.OnDoubleTapListener */

    @Override
    public boolean onDoubleTap(final MotionEvent e, final MapView pMapView) {
        for (final Overlay overlay : this.overlaysReversed()) {
            if (overlay.onDoubleTap(e, pMapView)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onDoubleTapEvent(final MotionEvent e, final MapView pMapView) {
        for (final Overlay overlay : this.overlaysReversed()) {
            if (overlay.onDoubleTapEvent(e, pMapView)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(final MotionEvent e, final MapView pMapView) {
        for (final Overlay overlay : this.overlaysReversed()) {
            if (overlay.onSingleTapConfirmed(e, pMapView)) {
                return true;
            }
        }

        return false;
    }

	/* OnGestureListener */

    @Override
    public boolean onDown(final MotionEvent pEvent, final MapView pMapView) {
        for (final Overlay overlay : this.overlaysReversed()) {
            if (overlay.onDown(pEvent, pMapView)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onFling(final MotionEvent pEvent1, final MotionEvent pEvent2,
                           final float pVelocityX, final float pVelocityY, final MapView pMapView) {
        for (final Overlay overlay : this.overlaysReversed()) {
            if (overlay.onFling(pEvent1, pEvent2, pVelocityX, pVelocityY, pMapView)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onLongPress(final MotionEvent pEvent, final MapView pMapView) {
        for (final Overlay overlay : this.overlaysReversed()) {
            if (overlay.onLongPress(pEvent, pMapView)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onScroll(final MotionEvent pEvent1, final MotionEvent pEvent2,
                            final float pDistanceX, final float pDistanceY, final MapView pMapView) {
        for (final Overlay overlay : this.overlaysReversed()) {
            if (overlay.onScroll(pEvent1, pEvent2, pDistanceX, pDistanceY, pMapView)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onShowPress(final MotionEvent pEvent, final MapView pMapView) {
        for (final Overlay overlay : this.overlaysReversed()) {
            overlay.onShowPress(pEvent, pMapView);
        }
    }

    @Override
    public boolean onSingleTapUp(final MotionEvent pEvent, final MapView pMapView) {
        for (final Overlay overlay : this.overlaysReversed()) {
            if (overlay.onSingleTapUp(pEvent, pMapView)) {
                return true;
            }
        }

        return false;
    }

    // ** Options Menu **//

    @Override
    public void setOptionsMenusEnabled(final boolean pEnabled) {
        for (final Overlay overlay : mOverlayList) {
            if ((overlay instanceof IOverlayMenuProvider)
                    && ((IOverlayMenuProvider) overlay).isOptionsMenuEnabled()) {
                ((IOverlayMenuProvider) overlay).setOptionsMenuEnabled(pEnabled);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu pMenu, final int menuIdOffset, final MapView mapView) {
        boolean result = true;
        for (final Overlay overlay : this.overlaysReversed()) {
            if (overlay instanceof IOverlayMenuProvider) {
                final IOverlayMenuProvider overlayMenuProvider = (IOverlayMenuProvider) overlay;
                if (overlayMenuProvider.isOptionsMenuEnabled()) {
                    result &= overlayMenuProvider.onCreateOptionsMenu(pMenu, menuIdOffset, mapView);
                }
            }
        }

        if (mTilesOverlay != null && mTilesOverlay.isOptionsMenuEnabled()) {
            result &= mTilesOverlay.onCreateOptionsMenu(pMenu, menuIdOffset, mapView);
        }

        return result;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu pMenu, final int menuIdOffset, final MapView mapView) {
        for (final Overlay overlay : this.overlaysReversed()) {
            if (overlay instanceof IOverlayMenuProvider) {
                final IOverlayMenuProvider overlayMenuProvider = (IOverlayMenuProvider) overlay;
                if (overlayMenuProvider.isOptionsMenuEnabled()) {
                    overlayMenuProvider.onPrepareOptionsMenu(pMenu, menuIdOffset, mapView);
                }
            }
        }

        if (mTilesOverlay != null && mTilesOverlay.isOptionsMenuEnabled()) {
            mTilesOverlay.onPrepareOptionsMenu(pMenu, menuIdOffset, mapView);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item, final int menuIdOffset, final MapView mapView) {
        for (final Overlay overlay : this.overlaysReversed()) {
            if (overlay instanceof IOverlayMenuProvider) {
                final IOverlayMenuProvider overlayMenuProvider = (IOverlayMenuProvider) overlay;
                if (overlayMenuProvider.isOptionsMenuEnabled() &&
                        overlayMenuProvider.onOptionsItemSelected(item, menuIdOffset, mapView)) {
                    return true;
                }
            }
        }

        if (mTilesOverlay != null &&
                mTilesOverlay.isOptionsMenuEnabled() &&
                mTilesOverlay.onOptionsItemSelected(item, menuIdOffset, mapView)) {
            return true;
        }

        return false;
    }
}