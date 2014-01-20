package org.osmdroid.samplefragments;

import org.osmdroid.R;
import org.osmdroid.views.MapView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SampleFragmentXmlLayout extends BaseSampleFragment {

	// ===========================================================
	// Fields
	// ===========================================================

	public static final String TITLE = "MapView in XML layout";

	@Override
	public String getSampleTitle() {
		return TITLE;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.mapview, null);
		mMapView = (MapView) v.findViewById(R.id.mapview);
		return v;
		// mResourceProxy = new ResourceProxyImpl(inflater.getContext().getApplicationContext());
		// mMapView = new MapView(inflater.getContext(), 256, mResourceProxy);
		// mMapView.setUseSafeCanvas(true);
		// return mMapView;
	}

	// @Override
	// public void onActivityCreated(Bundle savedInstanceState) {
	// super.onActivityCreated(savedInstanceState);
	//
	// final Context context = this.getActivity();
	//
	// // only do static initialisation if needed
	// if (CloudmadeUtil.getCloudmadeKey().length() == 0) {
	// CloudmadeUtil.retrieveCloudmadeKey(context.getApplicationContext());
	// }
	//
	// addOverlays();
	//
	// mMapView.setBuiltInZoomControls(true);
	// mMapView.setMultiTouchControls(true);
	// }
	//
	// /**
	// * An appropriate place to override and add overlays.
	// */
	// protected void addOverlays() {
	// //
	// }
}
