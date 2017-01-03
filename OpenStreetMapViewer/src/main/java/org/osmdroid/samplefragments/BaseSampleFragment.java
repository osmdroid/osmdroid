package org.osmdroid.samplefragments;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseSampleFragment extends Fragment {

	public static final String TAG = "osmBaseFrag";
	public abstract String getSampleTitle();

	// ===========================================================
	// Fields
	// ===========================================================

	protected MapView mMapView;

	public MapView getmMapView(){
		return mMapView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mMapView = new MapView(inflater.getContext());
		if (Build.VERSION.SDK_INT >= 12) {
			mMapView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
				/**
				 * mouse wheel zooming ftw
				 * http://stackoverflow.com/questions/11024809/how-can-my-view-respond-to-a-mousewheel
				 * @param v
				 * @param event
				 * @return
				 */
				@Override
				public boolean onGenericMotion(View v, MotionEvent event) {
					if (0 != (event.getSource() & InputDevice.SOURCE_CLASS_POINTER)) {
						switch (event.getAction()) {
							case MotionEvent.ACTION_SCROLL:
								if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f)
									mMapView.getController().zoomOut();
								else {
									mMapView.getController().zoomIn();
								}
								return true;
						}
					}
					return false;
				}
			});
		}
		Log.d(TAG, "onCreateView");
		return mMapView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated");
		addOverlays();

		if (mMapView!=null) {
			mMapView.getOverlays().add(new CopyrightOverlay(getActivity()));
			mMapView.setBuiltInZoomControls(true);
			mMapView.setMultiTouchControls(true);
			mMapView.setTilesScaledToDpi(true);
		}
	}

	@Override
	public void onDestroyView(){
		super.onDestroyView();
		Log.d(TAG, "onDetach");
		if (mMapView!=null)
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
