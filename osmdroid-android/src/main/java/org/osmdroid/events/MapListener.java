package org.osmdroid.events;

import androidx.annotation.MainThread;
import androidx.annotation.UiThread;

import org.osmdroid.views.overlay.IViewBoundingBoxChangedListener;

/**
 * The listener interface for receiving map movement events.<br>
 * To process a map event, either implement this interface or extend {@link MapAdapter}, then register with the MapView using <i>setMapListener()</i>.
 *
 * @author Theodore Hong
 */
@UiThread @MainThread
public interface MapListener extends IViewBoundingBoxChangedListener {

    /** Called when a map is scrolled */
    boolean onScroll(ScrollEvent event);

    /** Called when a map is zoomed */
    boolean onZoom(ZoomEvent event);

}
