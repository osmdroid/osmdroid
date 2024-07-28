package org.osmdroid.views.overlay;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.UiThread;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import org.osmdroid.api.IMapView;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay.Snappable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * <a href="https://github.com/osmdroid/osmdroid/issues/154">...</a>
 *
 * @author dozd
 * @since 5.0.0
 */
public class DefaultOverlayManager extends CopyOnWriteArrayList<Overlay> implements OverlayManager {

    @Nullable
    private TilesOverlay mTilesOverlay;
    @Nullable
    private IViewBoundingBoxChangedListener mViewBoundingBoxChangedListener = null;
    private Lifecycle mMapViewLifeCycle;
    @Nullable
    private DefaultLifecycleObserver mDefaultLifecycleObserver = null;
    private final ConcurrentLinkedQueue<Overlay> mConcurrentOverlaysToRemove = new ConcurrentLinkedQueue<>();

    public DefaultOverlayManager(@Nullable final TilesOverlay tilesOverlay) {
        setTilesOverlay(tilesOverlay);
    }

    @Override
    public boolean add(@Nullable final Overlay overlay) {
        if (overlay == null) return false;
        return super.add(overlay);
    }

    @Override
    public void add(final int pIndex, @Nullable final Overlay pElement) {
        if (pElement == null) return;
        super.add(pIndex, pElement);
    }

    @Override
    public boolean addAll(@Nullable final Collection<? extends Overlay> c) {
        if (c == null) return false;
        return super.addAll(c);
    }

    @Override
    public boolean addAll(final int index, @Nullable final Collection<? extends Overlay> c) {
        if (c == null) return false;
        return super.addAll(index, c);
    }

    @Override
    public boolean addIfAbsent(@Nullable final Overlay overlay) {
        if (overlay == null) return false;
        return super.addIfAbsent(overlay);
    }

    @Override
    public int addAllAbsent(@Nullable final Collection<? extends Overlay> c) {
        if (c == null) return 0;
        return super.addAllAbsent(c);
    }

    @Override
    public boolean remove(@Nullable final Object o) {
        if (o == null) return false;
        mConcurrentOverlaysToRemove.add((Overlay)o);
        return super.remove(o);
    }

    @Override
    public Overlay remove(final int index) {
        final Overlay cPrev = super.remove(index);
        mConcurrentOverlaysToRemove.add(cPrev);
        return cPrev;
    }

    @Override
    @RequiresApi(api = android.os.Build.VERSION_CODES.N)
    public boolean removeIf(@NonNull final Predicate<? super Overlay> filter) {
        return super.removeIf(new Predicate<>() {
            @Override
            public boolean test(@NonNull final Overlay overlay) {
                final boolean res = filter.test(overlay);
                if (res) mConcurrentOverlaysToRemove.add(overlay);
                return false;
            }
        });
    }

    @Override
    public boolean retainAll(@NonNull final Collection<?> c) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void replaceAll(@NonNull final UnaryOperator<Overlay> operator) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public boolean removeAll(@Nullable final Collection<?> c) {
        if (c == null) return false;
        mConcurrentOverlaysToRemove.addAll(this);
        return super.removeAll(c);
    }

    @Nullable
    @Override
    public Overlay set(final int pIndex, @Nullable final Overlay pElement) {
        if (pElement == null) return null;
        final Overlay cPrev = super.set(pIndex, pElement);
        mConcurrentOverlaysToRemove.add(cPrev);
        return cPrev;
    }

    @Override
    public void clear() {
        mConcurrentOverlaysToRemove.addAll(this);
        super.clear();
    }

    @Nullable
    @Override
    public TilesOverlay getTilesOverlay() {
        return mTilesOverlay;
    }

    @Override
    public void setTilesOverlay(@Nullable final TilesOverlay tilesOverlay) {
        if (mTilesOverlay != null) {
            //TODO: check if there is something needed to do in this case
        }
        mTilesOverlay = tilesOverlay;
        if ((mTilesOverlay != null) && (mMapViewLifeCycle != null)) mTilesOverlay.setLifecycleFromMapView(mMapViewLifeCycle);
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
                        return DefaultOverlayManager.this.listIterator(DefaultOverlayManager.this.size());
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

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @Override
    public final List<Overlay> overlays() {
        return this;
    }

    @UiThread @MainThread
    @Override
    public void onDraw(final Canvas c, final MapView pMapView) {
            //raise Overlays REMOVED event
        Overlay cOverlay;
        while ((cOverlay = mConcurrentOverlaysToRemove.poll()) != null) {
            if (!cOverlay.isMarkedAsRemoved()) continue;
            cOverlay.onRemovedFromOverlayManager(this);
            cOverlay.setIsAttachedToOverlayManager(Boolean.FALSE);
        }
            //raise Overlays ATTACHED event
        for (final Overlay overlay : this) {
            if (overlay.isAttachedToOverlayManager()) continue;
            overlay.onAttachedToOverlayManager(this);
            overlay.setIsAttachedToOverlayManager(Boolean.TRUE);
        }
            //normal drawing procedure
        onDrawHelper(c, pMapView, pMapView.getProjection());
    }

    /**
     * @since 6.1.0
     */
    @UiThread @MainThread
    @Override
    public void onDraw(final Canvas c, final Projection pProjection) {
            //raise Overlays REMOVED event
        Overlay cOverlay;
        while ((cOverlay = mConcurrentOverlaysToRemove.poll()) != null) {
            if (!cOverlay.isMarkedAsRemoved()) continue;
            cOverlay.onRemovedFromOverlayManager(this);
            cOverlay.setIsAttachedToOverlayManager(Boolean.FALSE);
        }
            //raise Overlays ATTACHED event
        for (final Overlay overlay : this) {
            if (overlay.isAttachedToOverlayManager()) continue;
            overlay.onAttachedToOverlayManager(this);
            overlay.setIsAttachedToOverlayManager(Boolean.TRUE);
        }
            //normal drawing procedure
        onDrawHelper(c, null, pProjection);
    }

    /**
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
        for (final Overlay overlay : this) {
            if (overlay != null) {
                overlay.setViewBoundingBoxChangedListener(mViewBoundingBoxChangedListener);
                overlay.setLifecycleFromMapView(mMapViewLifeCycle);
                if (overlay.isEnabled() && (overlay instanceof TilesOverlay)) ((TilesOverlay) overlay).protectDisplayedTilesForCache(c, pProjection);
            }
        }

        //always pass false, the shadow parameter will be removed in a later version of osmdroid, this change should result in the on draw being called twice
        if (mTilesOverlay != null && mTilesOverlay.isEnabled()) {
            if (pMapView != null) mTilesOverlay.draw(c, pMapView, false);
            else mTilesOverlay.draw(c, pProjection);
        }

        //always pass false, the shadow parameter will be removed in a later version of osmdroid, this change should result in the on draw being called twice
        for (final Overlay overlay : this) {
            //#396 fix, null check
            if ((overlay != null) && overlay.isEnabled()) {
                if (pMapView != null) overlay.draw(c, pMapView, false);
                else overlay.draw(c, pProjection);
            }
        }
        //potential fix for #52 pMapView.invalidate();
    }

    @Override
    public void setMapViewLifecycle(@Nullable final MapView mapView) {
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
    public boolean onFling(final MotionEvent pEvent1, final MotionEvent pEvent2, final float pVelocityX, final float pVelocityY, final MapView pMapView) {
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
    public boolean onScroll(final MotionEvent pEvent1, final MotionEvent pEvent2, final float pDistanceX, final float pDistanceY, final MapView pMapView) {
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
        for (final Overlay overlay : this) {
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
                if (overlayMenuProvider.isOptionsMenuEnabled() && overlayMenuProvider.onOptionsItemSelected(item, menuIdOffset, mapView)) {
                    return true;
                }
            }
        }

        if ((mTilesOverlay != null) && mTilesOverlay.isOptionsMenuEnabled() && mTilesOverlay.onOptionsItemSelected(item, menuIdOffset, mapView)) {
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

    private void setLifecycleFromMapView(@Nullable final MapView mapView) {
        if (mMapViewLifeCycle != null) {
            if (mDefaultLifecycleObserver != null) mMapViewLifeCycle.removeObserver(mDefaultLifecycleObserver);
            return;
        }
        if (mapView == null) return;
        mMapViewLifeCycle = mapView.getLifecycle();
        if (mTilesOverlay != null) mTilesOverlay.setLifecycleFromMapView(mMapViewLifeCycle);
        mMapViewLifeCycle.addObserver(mDefaultLifecycleObserver = new DefaultLifecycleObserver() {
            @Override
            public void onCreate(@NonNull final LifecycleOwner owner) {
                for (final Overlay overlay : DefaultOverlayManager.this) { if (overlay == null) return; overlay.onCreate(); }
            }
            @Override
            public void onStart(@NonNull final LifecycleOwner owner) {
                for (final Overlay overlay : DefaultOverlayManager.this) { if (overlay == null) return; overlay.onStart(); }
            }
            @Override
            public void onResume(@NonNull final LifecycleOwner owner) {
                for (final Overlay overlay : DefaultOverlayManager.this) { if (overlay == null) return; overlay.onResume(); }
            }
            @Override
            public void onPause(@NonNull final LifecycleOwner owner) {
                for (final Overlay overlay : DefaultOverlayManager.this) { if (overlay == null) return; overlay.onPause(); }
            }
            @Override
            public void onStop(@NonNull final LifecycleOwner owner) {
                for (final Overlay overlay : DefaultOverlayManager.this) { if (overlay == null) return; overlay.onStop(); }
            }
            @Override
            public void onDestroy(@NonNull final LifecycleOwner owner) {
                for (final Overlay overlay : DefaultOverlayManager.this) { if (overlay == null) return; overlay.onDestroy(mapView); }
                mMapViewLifeCycle.removeObserver(this);
                mDefaultLifecycleObserver = null;
                mMapViewLifeCycle = null;
                DefaultOverlayManager.this.onDestroyInternal();
            }
        });
    }

    @Override
    public void onDestroyInternal() {
        this.clear();
    }

}