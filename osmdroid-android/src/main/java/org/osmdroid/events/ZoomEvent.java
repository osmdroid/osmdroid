package org.osmdroid.events;

import org.osmdroid.views.MapView;

/*
 * The event generated when a map has finished zooming to the level <code>zoomLevel</code>.
 *
 * @author Theodore Hong
 */
public class ZoomEvent implements MapEvent {
	protected MapView source;
	protected int zoomLevel;

	public ZoomEvent(final MapView source, final int zoomLevel) {
		this.source = source;
		this.zoomLevel = zoomLevel;
	}

	/*
	 * Return the map which generated this event.
	 */
	public MapView getSource() {
		return source;
	}

	/*
	 * Return the zoom level zoomed to.
	 */
	public int getZoomLevel() {
		return zoomLevel;
	}

	@Override
	public String toString() {
		return "ZoomEvent [source=" + source + ", zoomLevel=" + zoomLevel + "]";
	}
}
