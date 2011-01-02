package org.osmdroid.events;

import org.osmdroid.views.OpenStreetMapView;

/*
 * The event generated when a map has finished scrolling to the coordinates (<code>x</code>,<code>y</code>).
 *
 * @author Theodore Hong
 */
public class ScrollEvent implements MapEvent {
	protected OpenStreetMapView source;
	protected int x;
	protected int y;

	public ScrollEvent(OpenStreetMapView source, int x, int y) {
		this.source = source;
		this.x = x;
		this.y = y;
	}

	/*
	 * Return the map which generated this event.
	 */
	public OpenStreetMapView getSource() {
		return source;
	}

	/*
	 * Return the x-coordinate scrolled to.
	 */
	public int getX() {
		return x;
	}

	/*
	 * Return the y-coordinate scrolled to.
	 */
	public int getY() {
		return y;
	}

	@Override
	public String toString() {
		return "ScrollEvent [source=" + source + ", x=" + x + ", y=" + y + "]";
	}
}
