package org.osmdroid.views.overlay.mylocation;

import android.location.Location;

public interface IMyLocationConsumer {
	/**
	 * Call when a provider has a new location to consume. This can be called on any thread.
	 */
	void onLocationChanged(Location location, IMyLocationProvider source);
}
