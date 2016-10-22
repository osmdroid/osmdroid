package org.osmdroid.samplefragments;

import org.osmdroid.views.MapView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseSampleFragment extends Fragment {

	public static final String TAG = "osmBaseFrag";
	public abstract String getSampleTitle();

	// ===========================================================
	// Fields
	// ===========================================================

	protected MapView mMapView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mMapView = new MapView(inflater.getContext());
		Log.d(TAG, "onCreateView");
		return mMapView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated");
		addOverlays();

		mMapView.setBuiltInZoomControls(true);
		mMapView.setMultiTouchControls(true);
		mMapView.setTilesScaledToDpi(true);
	}

	@Override
	public void onDestroyView(){
		super.onDestroyView();
		Log.d(TAG, "onDetach");
		mMapView.onDetach();
		mMapView=null;
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		Log.d(TAG, "onDestroy");

	}

	/**
	 * An appropriate place to override and add overlays.
	 */
	protected void addOverlays() {
		//
	}

	public boolean skipOnCiTests(){
		return false;
	}

	/**
	 * optional place to put automated test procedures, used during the connectCheck tests
	 * this is called OFF of the UI thread. block this method call util the test is done
	 */
	public void runTestProcedures() throws Exception{

	}
}
