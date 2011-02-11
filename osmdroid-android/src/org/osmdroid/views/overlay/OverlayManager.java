package org.osmdroid.views.overlay;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osmdroid.views.MapView;

import android.graphics.Canvas;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

public class OverlayManager extends AbstractList<Overlay> {

	private TilesOverlay mTilesOverlay;

	private final CopyOnWriteArrayList<Overlay> mOverlayList;

	public OverlayManager(TilesOverlay tilesOverlay) {
		setTilesOverlay(tilesOverlay);
		mOverlayList = new CopyOnWriteArrayList<Overlay>();
	}

	@Override
	public Overlay get(int pIndex) {
		return mOverlayList.get(pIndex);
	}

	@Override
	public int size() {
		return mOverlayList.size();
	}

	@Override
	public void add(int pIndex, Overlay pElement) {
		mOverlayList.add(pIndex, pElement);
	}

	@Override
	public Overlay remove(int pIndex) {
		return mOverlayList.remove(pIndex);
	}

	@Override
	public Overlay set(int pIndex, Overlay pElement) {
		return mOverlayList.set(pIndex, pElement);
	}

	/**
	 * Gets the optional TilesOverlay class.
	 * 
	 * @return the tilesOverlay
	 */
	public TilesOverlay getTilesOverlay() {
		return mTilesOverlay;
	}

	/**
	 * Sets the optional TilesOverlay class. If set, this overlay will be drawn before all other
	 * overlays and will not be included in the editable list of overlays and can't be cleared
	 * except by a subsequent call to setTilesOverlay().
	 * 
	 * @param tilesOverlay
	 *            the tilesOverlay to set
	 */
	public void setTilesOverlay(TilesOverlay tilesOverlay) {
		mTilesOverlay = tilesOverlay;
	}

	public void setOptionsMenusEnabled(boolean pEnabled) {
		for (Overlay overlay : mOverlayList)
			overlay.setOptionsMenuEnabled(pEnabled);
	}

	public Iterable<Overlay> overlaysReversed() {
		return new Iterable<Overlay>() {
			@Override
			public Iterator<Overlay> iterator() {
				final ListIterator<Overlay> i = mOverlayList.listIterator(mOverlayList.size());

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

	public void onDraw(final Canvas c, final MapView pMapView) {
		if (mTilesOverlay != null)
			mTilesOverlay.onManagedDraw(c, pMapView);

		for (Overlay overlay : mOverlayList)
			overlay.onManagedDraw(c, pMapView);
	}

	public void onDetach(final MapView pMapView) {
		if (mTilesOverlay != null)
			mTilesOverlay.onDetach(pMapView);

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

		if (mTilesOverlay != null)
			result &= mTilesOverlay.onManagedCreateOptionsMenu(pMenu, menuIdOffset, mapView);

		return result;
	}

	public boolean onPrepareOptionsMenu(final Menu pMenu, final int menuIdOffset,
			final MapView mapView) {
		boolean result = true;
		for (Overlay overlay : this.overlaysReversed())
			result &= overlay.onManagedPrepareOptionsMenu(pMenu, menuIdOffset, mapView);

		if (mTilesOverlay != null)
			result &= mTilesOverlay.onManagedPrepareOptionsMenu(pMenu, menuIdOffset, mapView);

		return result;
	}

	public boolean onMenuItemSelected(final int featureId, final MenuItem item,
			final int menuIdOffset, final MapView mapView) {
		for (Overlay overlay : this.overlaysReversed())
			if (overlay.onManagedMenuItemSelected(featureId, item, menuIdOffset, mapView))
				return true;

		if (mTilesOverlay != null)
			if (mTilesOverlay.onManagedMenuItemSelected(featureId, item, menuIdOffset, mapView))
				return true;

		return false;
	}
}
