package org.osmdroid.events;

import org.osmdroid.views.MapView;

/*
 * The event generated when a map has finished scrolling to the coordinates (<code>x</code>,<code>y</code>).
 *
 * note coordinates represent integer based scroll coordinates, not latitude/longitude. They are
 * subject to integer overflow and will be revisited in a later version of osmdroid to use long values.
 *
 * In some cases, osmdroid will always send a 0,0 coordinate.
 * As such, this event is more useful to be notified when the map moves, but not really where it ended up.
 * Use other functions in {@link MapView#getBoundingBox()} and {@link MapView#getMapCenter()} for that.
 * @author Theodore Hong
 */
public class ScrollEvent implements MapEvent {
    protected MapView source;
    protected int x;
    protected int y;

    /**
     * note coordinates represent integer based scroll coordinates, not latitude/longitude. They are
     * subject to integer overflow and will be revisited in a later version of osmdroid to use long values.
     * In some cases, osmdroid will always send a 0,0 coordinate.
     * As such, this event is more useful to be notified when the map moves, but not really where it ended up.
     * Use other functions in {@link MapView#getBoundingBox()} and {@link MapView#getMapCenter()} for that.
     *
     * @param source
     * @param x
     * @param y
     */
    public ScrollEvent(final MapView source, final int x, final int y) {
        this.source = source;
        this.x = x;
        this.y = y;
    }

    /**
     * Return the map which generated this event.
     */
    public MapView getSource() {
        return source;
    }

    /**
     * Return the x-coordinate scrolled to.
     * note coordinates represent integer based scroll coordinates, not latitude/longitude. They are
     * subject to integer overflow and will be revisited in a later version of osmdroid to use long values.
     * In some cases, osmdroid will always send a 0,0 coordinate.
     * As such, this event is more useful to be notified when the map moves, but not really where it ended up.
     * Use other functions in {@link MapView#getBoundingBox()} and {@link MapView#getMapCenter()} for that.
     */
    public int getX() {
        return x;
    }

    /**
     * note coordinates represent integer based scroll coordinates, not latitude/longitude. They are
     * subject to integer overflow and will be revisited in a later version of osmdroid to use long values.
     * Return the y-coordinate scrolled to. In some cases, osmdroid will always send a 0,0 coordinate.
     * As such, this event is more useful to be notified when the map moves, but not really where it ended up.
     * Use other functions in {@link MapView#getBoundingBox()} and {@link MapView#getMapCenter()} for that.
     */
    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "ScrollEvent [source=" + source + ", x=" + x + ", y=" + y + "]";
    }
}
