package org.osmdroid;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.IOverlayMenuProvider;
import org.osmdroid.views.overlay.SafeDrawOverlay;
import org.osmdroid.views.safecanvas.ISafeCanvas;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

public class RotationGestureOverlay extends SafeDrawOverlay implements
		RotationGestureDetector.RotationListener, IOverlayMenuProvider {

	private final static int MENU_ENABLED = 1;

	private final RotationGestureDetector mRotationDetector;
	private MapView mMapView;
	private boolean mOptionsMenuEnabled = true;

	public RotationGestureOverlay(Context context, MapView mapView) {
		super(context);
		mMapView = mapView;
		mRotationDetector = new RotationGestureDetector(this);
	}

	@Override
	protected void drawSafe(ISafeCanvas canvas, MapView mapView, boolean shadow) {
		// No drawing necessary
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		if (this.isEnabled()) {
			mRotationDetector.onTouch(event);
		}
		return super.onTouchEvent(event, mapView);
	}

	@Override
	public void onRotate(float deltaAngle) {
		mMapView.setMapOrientation(mMapView.getMapOrientation() + deltaAngle);
	}

	@Override
	public boolean isOptionsMenuEnabled() {
		return mOptionsMenuEnabled;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu pMenu, int pMenuIdOffset, MapView pMapView) {
		pMenu.add(0, MENU_ENABLED + pMenuIdOffset, Menu.NONE, "Enable rotation").setIcon(
				android.R.drawable.ic_menu_info_details);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem pItem, int pMenuIdOffset, MapView pMapView) {
		if (pItem.getItemId() == MENU_ENABLED + pMenuIdOffset) {
			if (this.isEnabled()) {
				mMapView.setMapOrientation(0);
				this.setEnabled(false);
			} else {
				this.setEnabled(true);
			}
			return true;
		}

		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu pMenu, final int pMenuIdOffset,
			final MapView pMapView) {
		pMenu.findItem(MENU_ENABLED + pMenuIdOffset).setTitle(
				this.isEnabled() ? "Disable rotation" : "Enable rotation");
		return false;
	}

	@Override
	public void setOptionsMenuEnabled(boolean enabled) {
		mOptionsMenuEnabled = enabled;
	}
}
