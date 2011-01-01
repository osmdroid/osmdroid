package org.osmdroid.events;

import org.andnav.osm.views.OpenStreetMapView;

/*
 * The event generated when a map has finished zooming to the level <code>zoomLevel</code>.
 *
 * @author Theodore Hong
 */
public class ZoomEvent implements MapEvent {
	protected OpenStreetMapView source;
	protected int zoomLevel;

	public ZoomEvent(OpenStreetMapView source, int zoomLevel) {
		this.source = source;
		this.zoomLevel = zoomLevel;
	}

	/*
	 * Return the map which generated this event.
	 */
	public OpenStreetMapView getSource() {
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
