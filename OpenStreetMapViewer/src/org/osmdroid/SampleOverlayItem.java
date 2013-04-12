package org.osmdroid;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class SampleOverlayItem extends OverlayItem {

	public SampleOverlayItem(String aUid, String aTitle, String aDescription, GeoPoint aGeoPoint,
			Drawable aMarker, HotspotPlace aHotspotPlace) {
		super(aUid, aTitle, aDescription, aGeoPoint);
		this.setMarker(aMarker);
		this.setMarkerHotspot(aHotspotPlace);
	}

	public void draw(Canvas canvas) {
		//
	}

}
