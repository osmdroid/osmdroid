package org.osmdroid.events;

import org.osmdroid.views.MapView;

/*
 * The event generated when a map has finished scrolling to the coordinates (<code>x</code>,<code>y</code>).
 *
 * @author Theodore Hong
 */
public class ScrollEvent implements MapEvent {
	protected MapView source;
	protected int x;
	protected int y;

	public ScrollEvent(final MapView source, final int x, final int y) {
		this.source = source;
		this.x = x;
		this.y = y;
	}

	/*
	 * Return the map which generated this event.
	 */
	public MapView getSource() {
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
