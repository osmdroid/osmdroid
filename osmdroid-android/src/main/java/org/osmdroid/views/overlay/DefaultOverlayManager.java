package org.osmdroid.views.overlay;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import org.osmdroid.api.IMapView;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay.Snappable;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <a href="https://github.com/osmdroid/osmdroid/issues/154">...</a>
 *
 * @author dozd
 * @since 5.0.0
 */
public class DefaultOverlayManager extends AbstractList<Overlay> implements OverlayManager {

    @Nullable
    private TilesOverlay mTilesOverlay;
    private final CopyOnWriteArrayList<Overlay> mOverlayList;
    @Nullable
    private IViewBoundingBoxChangedListener mViewBoundingBoxChangedListener = null;
    private Lifecycle mMapViewLifeCycle;
    @Nullable
    private DefaultLifecycleObserver mDefaultLifecycleObserver = null;

    public DefaultOverlayManager(final TilesOverlay tilesOverlay) {
        setTilesOverlay(tilesOverlay);
        mOverlayList = new CopyOnWriteArrayList<>();
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
        if (pElement == null) {
            //#396 fix, null check
            Exception ex = new Exception("Attempt to add a null overlay to the collection. This is probably a bug and should be reported!");
            Log.e(IMapView.LOGTAG, ex.getMessage(), ex);
        } else {
            mOverlayList.add(pIndex, pElement);
        }
    }

    @Override
    public Overlay remove(final int pIndex) {
        return mOverlayList.remove(pIndex);
    }

    @Override
    public Overlay set(final int pIndex, @NonNull final Overlay pElement) {
        //#396 fix, null check
        //noinspection ConstantValue
        if (pElement == null) {
            Exception ex = new Exception("Attempt to set a null overlay to the collection. This is probably a bug and should be reported!");
            Log.e(IMapView.LOGTAG, ex.getMessage(), ex);
            return null;
        } else {
            return mOverlayList.set(pIndex, pElement);
        }
    }

    @Nullable
    @Override
    public TilesOverlay getTilesOverlay() {
        return mTilesOverlay;
    }

    @Override
    public void setTilesOverlay(@Nullable final TilesOverlay tilesOverlay) {
        if (mTilesOverlay != null) {
            //noinspection EnhancedSwitchMigration
            switch (mMapViewLifeCycle.getCurrentState()) {
                case RESUMED: {
                    mTilesOverlay.onPause();
                    mTilesOverlay.onStop();
                    mTilesOverlay.onDestroy();
                    break;
                }
                case STARTED: {
                    mTilesOverlay.onStop();
                    mTilesOverlay.onDestroy();
                    break;
                }
                case CREATED: {
                    mTilesOverlay.onDestroy();
                    break;
                }
            }
        }
        mTilesOverlay = tilesOverlay;
    }

    @Override
    public Iterable<Overlay> overlaysReversed() {
        return new Iterable<>() {

            /**
             * @since 6.1.0
             */
            private ListIterator<Overlay> bulletProofReverseListIterator() {
                while (true) {
                    try {
                        return mOverlayList.listIterator(mOverlayList.size());
                    } catch (final IndexOutOfBoundsException e) {
                        // thread-concurrency fix - in case an item is removed in a very inappropriate time
                        // cf. https://github.com/osmdroid/osmdroid/issues/1260
                    }
                }
            }

            @NonNull
            @Override
            public Iterator<Overlay> iterator() {
                final ListIterator<Overlay> i = bulletProofReverseListIterator();

                return new Iterator<>() {
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

    @UiThread @MainThread
    @Override
    public void onDraw(final Canvas c, final MapView pMapView) {
        onDrawHelper(c, pMapView, pMapView.getProjection());
    }

    /**
     * @since 6.1.0
     */
    @UiThread @MainThread
    @Override
    public void onDraw(final Canvas c, final Projection pProjection) {
        onDrawHelper(c, null, pProjection);
    }

    /**
     * @param pMapView    may be null
     * @param pProjection may NOT be null
     * @since 6.1.0
     */
    @UiThread @MainThread
    private void onDrawHelper(@NonNull final Canvas c, @Nullable final MapView pMapView, @NonNull final Projection pProjection) {
        if (pMapView != null) setLifecycleFromMapView(pMapView);
        //fix for https://github.com/osmdroid/osmdroid/issues/904
        if (mTilesOverlay != null) {
            mTilesOverlay.protectDisplayedTilesForCache(c, pProjection);
            mTilesOverlay.setViewBoundingBoxChangedListener(mViewBoundingBoxChangedListener);
        }
        for (final Overlay overlay : mOverlayList) {
            if (overlay != null) {
                overlay.setViewBoundingBoxChangedListener(mViewBoundingBoxChangedListener);
                if (overlay.isEnabled() && (overlay instanceof TilesOverlay)) {
                    ((TilesOverlay) overlay).protectDisplayedTilesForCache(c, pProjection);
                }
            }
        }

        //always pass false, the shadow parameter will be removed in a later version of osmdroid, this change should result in the on draw being called twice
        if (mTilesOverlay != null && mTilesOverlay.isEnabled()) {
            if (pMapView != null) {
                mTilesOverlay.draw(c, pMapView, false);
            } else {
                mTilesOverlay.draw(c, pProjection);
            }
        }

        //always pass false, the shadow parameter will be removed in a later version of osmdroid, this change should result in the on draw being called twice
        for (final Overlay overlay : mOverlayList) {
            //#396 fix, null check
            if (overlay != null && overlay.isEnabled()) {
                if (pMapView != null) {
                    overlay.draw(c, pMapView, false);
                } else {
                    overlay.draw(c, pProjection);
                }
            }
        }
        //potential fix for #52 pMapView.invalidate();
    }

    @Override
    public void setMapViewLifecycle(@NonNull final MapView mapView) {
        setLifecycleFromMapView(mapView);
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
        IOverlayMenuProvider overlayMenuProvider;
        for (final Overlay overlay : this.overlaysReversed()) {
            if (overlay instanceof IOverlayMenuProvider) {
                overlayMenuProvider = (IOverlayMenuProvider) overlay;
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
        IOverlayMenuProvider overlayMenuProvider;
        for (final Overlay overlay : this.overlaysReversed()) {
            if (overlay instanceof IOverlayMenuProvider) {
                overlayMenuProvider = (IOverlayMenuProvider) overlay;
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
        IOverlayMenuProvider overlayMenuProvider;
        for (final Overlay overlay : this.overlaysReversed()) {
            if (overlay instanceof IOverlayMenuProvider) {
                overlayMenuProvider = (IOverlayMenuProvider) overlay;
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

    /** {@inheritDoc} */
    @Override
    public void setViewBoundingBoxChangedListener(@Nullable final IViewBoundingBoxChangedListener listener) {
        mViewBoundingBoxChangedListener = listener;
    }

    /** {@inheritDoc} */
    @Override
    public void onViewBoundingBoxChanged(@NonNull final Rect fromBounds, final int fromZoom, @NonNull final Rect toBounds, final int toZoom) {
        if (mViewBoundingBoxChangedListener != null) mViewBoundingBoxChangedListener.onViewBoundingBoxChanged(fromBounds, fromZoom, toBounds, toZoom);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() { return mMapViewLifeCycle; }

    private void setLifecycleFromMapView(@NonNull final MapView mapView) {
        if (mMapViewLifeCycle != null) return;
        mMapViewLifeCycle = mapView.getLifecycle();
        if (mDefaultLifecycleObserver == null) mMapViewLifeCycle.addObserver(mDefaultLifecycleObserver = new DefaultLifecycleObserver() {
            @Override
            public void onCreate(@NonNull final LifecycleOwner owner) {
                if (mTilesOverlay != null) mTilesOverlay.onCreate();
                for (final Overlay overlay : mOverlayList) { if (overlay == null) return; overlay.onCreate(); }
            }
            @Override
            public void onStart(@NonNull final LifecycleOwner owner) {
                if (mTilesOverlay != null) mTilesOverlay.onStart();
                for (final Overlay overlay : mOverlayList) { if (overlay == null) return; overlay.onStart(); }
            }
            @Override
            public void onResume(@NonNull final LifecycleOwner owner) {
                if (mTilesOverlay != null) mTilesOverlay.onResume();
                for (final Overlay overlay : mOverlayList) { if (overlay == null) return; overlay.onResume(); }
            }
            @Override
            public void onPause(@NonNull final LifecycleOwner owner) {
                for (final Overlay overlay : mOverlayList) { if (overlay == null) return; overlay.onPause(); }
                if (mTilesOverlay != null) mTilesOverlay.onPause();
            }
            @Override
            public void onStop(@NonNull final LifecycleOwner owner) {
                for (final Overlay overlay : mOverlayList) { if (overlay == null) return; overlay.onStop(); }
                if (mTilesOverlay != null) mTilesOverlay.onStop();
            }
            @Override
            public void onDestroy(@NonNull final LifecycleOwner owner) {
                for (final Overlay overlay : mOverlayList) { if (overlay == null) return; overlay.onDestroy(); }
                if (mTilesOverlay != null) mTilesOverlay.onDestroy();
                mMapViewLifeCycle.removeObserver(this);
                mDefaultLifecycleObserver = null;
                mMapViewLifeCycle = null;
                onDestroyInternal();
            }
        });
    }

    @Override
    public void onDestroyInternal() {
        this.clear();
    }

}