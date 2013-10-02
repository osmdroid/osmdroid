// Created by plusminus on 20:32:01 - 27.09.2008
package org.osmdroid.views.overlay;

import java.util.concurrent.atomic.AtomicInteger;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.views.MapView;
import org.osmdroid.views.util.constants.OverlayConstants;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Base class representing an overlay which may be displayed on top of a {@link MapView}. To add an
 * overlay, subclass this class, create an instance, and add it to the list obtained from
 * getOverlays() of {@link MapView}.
 *
 * This class implements a form of Gesture Handling similar to
 * {@link android.view.GestureDetector.SimpleOnGestureListener} and
 * {@link GestureDetector.OnGestureListener}. The difference is there is an additional argument for
 * the item.
 *
 * @author Nicolas Gramlich
 */
public abstract class Overlay implements OverlayConstants {

	// ===========================================================
	// Constants
	// ===========================================================

	private static AtomicInteger sOrdinal = new AtomicInteger();

	// From Google Maps API
	protected static final float SHADOW_X_SKEW = -0.8999999761581421f;
	protected static final float SHADOW_Y_SCALE = 0.5f;

	// ===========================================================
	// Fields
	// ===========================================================

	protected final ResourceProxy mResourceProxy;
	protected final float mScale;
	private static final Rect mRect = new Rect();
	private boolean mEnabled = true;

	// ===========================================================
	// Constructors
	// ===========================================================

	public Overlay(final Context ctx) {
		mResourceProxy = new DefaultResourceProxyImpl(ctx);
		mScale = ctx.getResources().getDisplayMetrics().density;
	}

	public Overlay(final ResourceProxy pResourceProxy) {
		mResourceProxy = pResourceProxy;
		mScale = mResourceProxy.getDisplayMetricsDensity();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	/**
	 * Sets whether the Overlay is marked to be enabled. This setting does nothing by default, but
	 * should be checked before calling draw().
	 */
	public void setEnabled(final boolean pEnabled) {
		this.mEnabled = pEnabled;
	}

	/**
	 * Specifies if the Overlay is marked to be enabled. This should be checked before calling
	 * draw().
	 *
	 * @return true if the Overlay is marked enabled, false otherwise
	 */
	public boolean isEnabled() {
		return this.mEnabled;
	}

	/**
	 * Since the menu-chain will pass through several independent Overlays, menu IDs cannot be fixed
	 * at compile time. Overlays should use this method to obtain and store a menu id for each menu
	 * item at construction time. This will ensure that two overlays don't use the same id.
	 *
	 * @return an integer suitable to be used as a menu identifier
	 */
	protected final static int getSafeMenuId() {
		return sOrdinal.getAndIncrement();
	}

	/**
	 * Similar to <see cref="getSafeMenuId" />, except this reserves a sequence of IDs of length
	 * <param name="count" />. The returned number is the starting index of that sequential list.
	 *
	 * @return an integer suitable to be used as a menu identifier
	 */
	protected final static int getSafeMenuIdSequence(final int count) {
		return sOrdinal.getAndAdd(count);
	}

	// ===========================================================
	// Methods for SuperClass/Interfaces
	// ===========================================================

	/**
	 * Draw the overlay over the map. This will be called on all active overlays with shadow=true,
	 * to lay down the shadow layer, and then again on all overlays with shadow=false. Callers
	 * should check isEnabled() before calling draw(). By default, draws nothing.
	 */
	protected abstract void draw(final Canvas c, final MapView osmv, final boolean shadow);

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * Override to perform clean up of resources before shutdown. By default does nothing.
	 */
	public void onDetach(final MapView mapView) {
	}

	/**
	 * By default does nothing (<code>return false</code>). If you handled the Event, return
	 * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
	 * none of the following Overlays or the underlying {@link MapView} has the chance to handle
	 * this event.
	 */
	public boolean onKeyDown(final int keyCode, final KeyEvent event, final MapView mapView) {
		return false;
	}

	/**
	 * By default does nothing (<code>return false</code>). If you handled the Event, return
	 * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
	 * none of the following Overlays or the underlying {@link MapView} has the chance to handle
	 * this event.
	 */
	public boolean onKeyUp(final int keyCode, final KeyEvent event, final MapView mapView) {
		return false;
	}

	/**
	 * <b>You can prevent all(!) other Touch-related events from happening!</b><br />
	 * By default does nothing (<code>return false</code>). If you handled the Event, return
	 * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
	 * none of the following Overlays or the underlying {@link MapView} has the chance to handle
	 * this event.
	 */
	public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
		return false;
	}

	/**
	 * By default does nothing (<code>return false</code>). If you handled the Event, return
	 * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
	 * none of the following Overlays or the underlying {@link MapView} has the chance to handle
	 * this event.
	 */
	public boolean onTrackballEvent(final MotionEvent event, final MapView mapView) {
		return false;
	}

	/** GestureDetector.OnDoubleTapListener **/

	/**
	 * By default does nothing (<code>return false</code>). If you handled the Event, return
	 * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
	 * none of the following Overlays or the underlying {@link MapView} has the chance to handle
	 * this event.
	 */
	public boolean onDoubleTap(final MotionEvent e, final MapView mapView) {
		return false;
	}

	/**
	 * By default does nothing (<code>return false</code>). If you handled the Event, return
	 * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
	 * none of the following Overlays or the underlying {@link MapView} has the chance to handle
	 * this event.
	 */
	public boolean onDoubleTapEvent(final MotionEvent e, final MapView mapView) {
		return false;
	}

	/**
	 * By default does nothing (<code>return false</code>). If you handled the Event, return
	 * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
	 * none of the following Overlays or the underlying {@link MapView} has the chance to handle
	 * this event.
	 */
	public boolean onSingleTapConfirmed(final MotionEvent e, final MapView mapView) {
		return false;
	}

	/** OnGestureListener **/

	/**
	 * By default does nothing (<code>return false</code>). If you handled the Event, return
	 * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
	 * none of the following Overlays or the underlying {@link MapView} has the chance to handle
	 * this event.
	 */
	public boolean onDown(final MotionEvent e, final MapView mapView) {
		return false;
	}

	/**
	 * By default does nothing (<code>return false</code>). If you handled the Event, return
	 * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
	 * none of the following Overlays or the underlying {@link MapView} has the chance to handle
	 * this event.
	 */
	public boolean onFling(final MotionEvent pEvent1, final MotionEvent pEvent2,
			final float pVelocityX, final float pVelocityY, final MapView pMapView) {
		return false;
	}

	/**
	 * By default does nothing (<code>return false</code>). If you handled the Event, return
	 * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
	 * none of the following Overlays or the underlying {@link MapView} has the chance to handle
	 * this event.
	 */
	public boolean onLongPress(final MotionEvent e, final MapView mapView) {
		return false;
	}

	/**
	 * By default does nothing (<code>return false</code>). If you handled the Event, return
	 * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
	 * none of the following Overlays or the underlying {@link MapView} has the chance to handle
	 * this event.
	 */
	public boolean onScroll(final MotionEvent pEvent1, final MotionEvent pEvent2,
			final float pDistanceX, final float pDistanceY, final MapView pMapView) {
		return false;
	}

	public void onShowPress(final MotionEvent pEvent, final MapView pMapView) {
		return;
	}

	/**
	 * By default does nothing (<code>return false</code>). If you handled the Event, return
	 * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
	 * none of the following Overlays or the underlying {@link MapView} has the chance to handle
	 * this event.
	 */
	public boolean onSingleTapUp(final MotionEvent e, final MapView mapView) {
		return false;
	}

	/**
	 * Convenience method to draw a Drawable at an offset. x and y are pixel coordinates. You can
	 * find appropriate coordinates from latitude/longitude using the MapView.getProjection() method
	 * on the MapView passed to you in draw(Canvas, MapView, boolean).
	 *
	 * @param shadow
	 *            If true, draw only the drawable's shadow. Otherwise, draw the drawable itself.
	 * @param aMapOrientation
	 */
	protected synchronized static void drawAt(final Canvas canvas, final Drawable drawable,
											  final int x, final int y, final boolean shadow,
											  final float aMapOrientation) {
		canvas.save();
		canvas.rotate(-aMapOrientation, x, y);
		drawable.copyBounds(mRect);
		drawable.setBounds(mRect.left + x, mRect.top + y, mRect.right + x, mRect.bottom + y);
		drawable.draw(canvas);
		drawable.setBounds(mRect);
		canvas.restore();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	/**
	 * Interface definition for overlays that contain items that can be snapped to (for example,
	 * when the user invokes a zoom, this could be called allowing the user to snap the zoom to an
	 * interesting point.)
	 */
	public interface Snappable {

		/**
		 * Checks to see if the given x and y are close enough to an item resulting in snapping the
		 * current action (e.g. zoom) to the item.
		 *
		 * @param x
		 *            The x in screen coordinates.
		 * @param y
		 *            The y in screen coordinates.
		 * @param snapPoint
		 *            To be filled with the the interesting point (in screen coordinates) that is
		 *            closest to the given x and y. Can be untouched if not snapping.
		 * @param mapView
		 *            The {@link MapView} that is requesting the snap. Use MapView.getProjection()
		 *            to convert between on-screen pixels and latitude/longitude pairs.
		 * @return Whether or not to snap to the interesting point.
		 */
		boolean onSnapToItem(int x, int y, Point snapPoint, IMapView mapView);
	}

}
