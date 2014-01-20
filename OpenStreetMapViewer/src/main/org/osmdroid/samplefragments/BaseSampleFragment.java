package org.osmdroid.samplefragments;

import org.osmdroid.ResourceProxy;
import org.osmdroid.ResourceProxyImpl;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.views.MapView;

import android.content.Context;
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
	protected ResourceProxy mResourceProxy;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mResourceProxy = new ResourceProxyImpl(inflater.getContext().getApplicationContext());
		mMapView = new MapView(inflater.getContext(), 256, mResourceProxy);
		mMapView.setUseSafeCanvas(true);
		return mMapView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final Context context = this.getActivity();

		// only do static initialisation if needed
		if (CloudmadeUtil.getCloudmadeKey().length() == 0) {
			CloudmadeUtil.retrieveCloudmadeKey(context.getApplicationContext());
		}

		addOverlays();

		mMapView.setBuiltInZoomControls(true);
		mMapView.setMultiTouchControls(true);
	}

	/**
	 * An appropriate place to override and add overlays.
	 */
	protected void addOverlays() {
		//
	}
}
