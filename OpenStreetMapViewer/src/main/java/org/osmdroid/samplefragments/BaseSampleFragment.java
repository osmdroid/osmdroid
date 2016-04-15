package org.osmdroid.samplefragments;

import org.osmdroid.views.MapView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseSampleFragment extends Fragment {

	public abstract String getSampleTitle();

	// ===========================================================
	// Fields
	// ===========================================================

	protected MapView mMapView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mMapView = new MapView(inflater.getContext());

		return mMapView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		addOverlays();

		mMapView.setBuiltInZoomControls(true);
		mMapView.setMultiTouchControls(true);
		mMapView.setTilesScaledToDpi(true);
	}

	@Override
	public void onDetach(){
		super.onDetach();
		mMapView.onDetach();
		mMapView=null;
	}

	/**
	 * An appropriate place to override and add overlays.
	 */
	protected void addOverlays() {
		//
	}
}
