package org.osmdroid.views.overlay;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

/**
 * Empty overlay than can be used to detect events on the map, 
 * and to throw them to a MapEventsReceiver. 
 * @see MapEventsReceiver
 * @author M.Kergall
 */
public class MapEventsOverlay extends Overlay {

	private MapEventsReceiver mReceiver;

	/** Use {@link #MapEventsOverlay(MapEventsReceiver)} instead */
	@Deprecated
	public MapEventsOverlay(Context ctx, MapEventsReceiver receiver) {
		this(receiver);
	}

	/**
	 * @param receiver the object that will receive/handle the events.
	 * It must implement MapEventsReceiver interface.
	 */
	public MapEventsOverlay(MapEventsReceiver receiver) {
        super();
		mReceiver = receiver;
    }

	@Override public void draw(Canvas c, MapView osmv, boolean shadow) {
		//Nothing to draw
	}
	
	@Override public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView){
		Projection proj = mapView.getProjection();
		GeoPoint p = (GeoPoint)proj.fromPixels((int)e.getX(), (int)e.getY());
		return mReceiver.singleTapConfirmedHelper(p);
	}
	
	@Override public boolean onLongPress(MotionEvent e, MapView mapView) {
		Projection proj = mapView.getProjection();
		GeoPoint p = (GeoPoint)proj.fromPixels((int)e.getX(), (int)e.getY());
		//throw event to the receiver:
		return mReceiver.longPressHelper(p);
    }

}

