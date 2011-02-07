package org.osmdroid.views.overlay;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osmdroid.views.MapView;

import android.graphics.Canvas;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

public class OverlayManager {

	final CopyOnWriteArrayList<Overlay> mOverlays;
	final CopyOnWriteArrayList<Overlay> mPermanentOverlays;

	public OverlayManager() {
		mOverlays = new CopyOnWriteArrayList<Overlay>();
		mPermanentOverlays = new CopyOnWriteArrayList<Overlay>();
	}

	public void addOverlay(Overlay overlay) {
		addOverlay(overlay, false);
	}

	public void addOverlay(Overlay overlay, boolean permanentOverlay) {
		mOverlays.add(overlay);
		if (permanentOverlay)
			mPermanentOverlays.add(overlay);
	}

	public void removeOverlay(Overlay overlay) {
		mOverlays.remove(overlay);
		mPermanentOverlays.remove(overlay);
	}

	public void clearOverlays(boolean clearPermanentOverlays) {
		if (clearPermanentOverlays)
			mOverlays.clear();
		else
			mOverlays.retainAll(mPermanentOverlays);
	}

	public void setOptionsMenusEnabled(boolean pEnabled) {
		for (Overlay overlay : mOverlays)
			overlay.setOptionsMenuEnabled(pEnabled);
	}

	public Iterable<Overlay> overlays() {
		return mOverlays;
	}

	public Iterable<Overlay> overlaysReversed() {
		return new Iterable<Overlay>() {
			@Override
			public Iterator<Overlay> iterator() {
				final ListIterator<Overlay> i = mOverlays.listIterator(mOverlays.size());

				return new Iterator<Overlay>() {
					public boolean hasNext() {
						return i.hasPrevious();
					}

					public Overlay next() {
						return i.previous();
					}

					public void remove() {
						i.remove();
					}
				};
			}
		};
	}

	public int count() {
		return mOverlays.size();
	}

	public void onDraw(final Canvas c, final MapView pMapView) {
		for (Overlay overlay : mOverlays)
			overlay.onManagedDraw(c, pMapView);
	}

	public void onDetach(final MapView pMapView) {
		for (Overlay overlay : this.overlaysReversed())
			overlay.onDetach(pMapView);
	}

	public boolean onKeyDown(final int keyCode, final KeyEvent event, final MapView pMapView) {
		for (Overlay overlay : this.overlaysReversed())
			if (overlay.onKeyDown(keyCode, event, pMapView))
				return true;

		return false;
	}

	public boolean onKeyUp(final int keyCode, final KeyEvent event, final MapView pMapView) {
		for (Overlay overlay : this.overlaysReversed())
			if (overlay.onKeyUp(keyCode, event, pMapView))
				return true;

		return false;
	}

	public boolean onTouchEvent(final MotionEvent event, final MapView pMapView) {
		for (Overlay overlay : this.overlaysReversed())
			if (overlay.onTouchEvent(event, pMapView))
				return true;

		return false;
	}

	public boolean onTrackballEvent(final MotionEvent event, final MapView pMapView) {
		for (Overlay overlay : this.overlaysReversed())
			if (overlay.onTrackballEvent(event, pMapView))
				return true;

		return false;
	}

	/** GestureDetector.OnDoubleTapListener **/

	public boolean onDoubleTap(final MotionEvent e, final MapView pMapView) {
		for (Overlay overlay : this.overlaysReversed())
			if (overlay.onDoubleTap(e, pMapView))
				return true;

		return false;
	}

	public boolean onDoubleTapEvent(final MotionEvent e, final MapView pMapView) {
		for (Overlay overlay : this.overlaysReversed())
			if (overlay.onDoubleTapEvent(e, pMapView))
				return true;

		return false;
	}

	public boolean onSingleTapConfirmed(final MotionEvent e, final MapView pMapView) {
		for (Overlay overlay : this.overlaysReversed())
			if (overlay.onSingleTapConfirmed(e, pMapView))
				return true;

		return false;
	}

	/** OnGestureListener **/

	public boolean onDown(final MotionEvent pEvent, final MapView pMapView) {
		for (Overlay overlay : this.overlaysReversed())
			if (overlay.onDown(pEvent, pMapView))
				return true;

		return false;
	}

	public boolean onFling(MotionEvent pEvent1, MotionEvent pEvent2, float pVelocityX,
			float pVelocityY, final MapView pMapView) {
		for (Overlay overlay : this.overlaysReversed())
			if (overlay.onFling(pEvent1, pEvent2, pVelocityX, pVelocityY, pMapView))
				return true;

		return false;
	}

	public boolean onLongPress(final MotionEvent pEvent, final MapView pMapView) {
		for (Overlay overlay : this.overlaysReversed())
			if (overlay.onLongPress(pEvent, pMapView))
				return true;

		return false;
	}

	public boolean onScroll(final MotionEvent pEvent1, final MotionEvent pEvent2,
			final float pDistanceX, final float pDistanceY, final MapView pMapView) {
		for (Overlay overlay : this.overlaysReversed())
			if (overlay.onScroll(pEvent1, pEvent2, pDistanceX, pDistanceY, pMapView))
				return true;

		return false;
	}

	public void onShowPress(final MotionEvent pEvent, final MapView pMapView) {
		for (Overlay overlay : this.overlaysReversed())
			overlay.onShowPress(pEvent, pMapView);
	}

	public boolean onSingleTapUp(final MotionEvent pEvent, final MapView pMapView) {
		for (Overlay overlay : this.overlaysReversed())
			if (overlay.onSingleTapUp(pEvent, pMapView))
				return true;

		return false;
	}

	/** Options Menu **/

	public boolean onCreateOptionsMenu(final Menu pMenu, final int menuIdOffset,
			final MapView mapView) {
		boolean result = true;
		for (Overlay overlay : this.overlaysReversed())
			result &= overlay.onManagedCreateOptionsMenu(pMenu, menuIdOffset, mapView);

		return result;
	}

	public boolean onPrepareOptionsMenu(final Menu pMenu, final int menuIdOffset,
			final MapView mapView) {
		boolean result = true;
		for (Overlay overlay : this.overlaysReversed())
			result &= overlay.onManagedPrepareOptionsMenu(pMenu, menuIdOffset, mapView);

		return result;
	}

	public boolean onMenuItemSelected(final int featureId, final MenuItem item,
			final int menuIdOffset, final MapView mapView) {
		for (Overlay overlay : this.overlaysReversed())
			if (overlay.onManagedMenuItemSelected(featureId, item, menuIdOffset, mapView))
				return true;

		return false;
	}
}
