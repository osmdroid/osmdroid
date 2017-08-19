package org.osmdroid.gpkg.features;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;


/**
 * Multiple LatLng object
 * 
 * @author osbornb
 */
public class MultiLatLng {

	private List<GeoPoint> latLngs = new ArrayList<GeoPoint>();

	private MarkerOptions markerOptions;

	public void add(GeoPoint latLng) {
		latLngs.add(latLng);
	}

	public List<GeoPoint> getLatLngs() {
		return latLngs;
	}

	public MarkerOptions getMarkerOptions() {
		return markerOptions;
	}

	public void setMarkerOptions(MarkerOptions markerOptions) {
		this.markerOptions = markerOptions;
	}

	public void setLatLngs(List<GeoPoint> latLngs) {
		this.latLngs = latLngs;
	}

}
