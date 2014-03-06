package org.osmdroid.events;

/*
 * The listener interface for receiving map movement events. To process a map event, either implement
 * this interface or extend MapAdapter, then register with the MapView using
 * setMapListener.
 *
 * @author Theodore Hong
 */
public interface MapListener {
	/*
	 * Called when a map is scrolled.
	 */
	public boolean onScroll(ScrollEvent event);

	/*
	 * Called when a map is zoomed.
	 */
	public boolean onZoom(ZoomEvent event);
}
