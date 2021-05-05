package org.osmdroid.views.overlay.gestures;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.IOverlayMenuProvider;
import org.osmdroid.views.overlay.Overlay;

public class RotationGestureOverlay extends Overlay implements
        RotationGestureDetector.RotationListener, IOverlayMenuProvider {
    private final static boolean SHOW_ROTATE_MENU_ITEMS = false;

    private final static int MENU_ENABLED = getSafeMenuId();
    private final static int MENU_ROTATE_CCW = getSafeMenuId();
    private final static int MENU_ROTATE_CW = getSafeMenuId();

    private final RotationGestureDetector mRotationDetector;
    private MapView mMapView;
    private boolean mOptionsMenuEnabled = true;

    /**
     * use {@link #RotationGestureOverlay(MapView)} instead.
     */
    @Deprecated
    public RotationGestureOverlay(Context context, MapView mapView) {
        this(mapView);
    }

    public RotationGestureOverlay(MapView mapView) {
        super();
        mMapView = mapView;
        mRotationDetector = new RotationGestureDetector(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
        mRotationDetector.onTouch(event);
        return super.onTouchEvent(event, mapView);
    }

    long timeLastSet = 0L;
    final long deltaTime = 25L;
    float currentAngle = 0f;

    @Override
    public void onRotate(float deltaAngle) {
        currentAngle += deltaAngle;
        if (System.currentTimeMillis() - deltaTime > timeLastSet) {
            timeLastSet = System.currentTimeMillis();
            mMapView.setMapOrientation(mMapView.getMapOrientation() + currentAngle);
        }
    }

    @Override
    public void onDetach(MapView map) {
        mMapView = null;
    }

    @Override
    public boolean isOptionsMenuEnabled() {
        return mOptionsMenuEnabled;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu pMenu, int pMenuIdOffset, MapView pMapView) {
        pMenu.add(0, MENU_ENABLED + pMenuIdOffset, Menu.NONE, "Enable rotation").setIcon(
                android.R.drawable.ic_menu_info_details);
        if (SHOW_ROTATE_MENU_ITEMS) {
            pMenu.add(0, MENU_ROTATE_CCW + pMenuIdOffset, Menu.NONE,
                    "Rotate maps counter clockwise").setIcon(android.R.drawable.ic_menu_rotate);
            pMenu.add(0, MENU_ROTATE_CW + pMenuIdOffset, Menu.NONE, "Rotate maps clockwise")
                    .setIcon(android.R.drawable.ic_menu_rotate);
        }
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
                return true;
            }
        } else if (pItem.getItemId() == MENU_ROTATE_CCW + pMenuIdOffset) {
            mMapView.setMapOrientation(mMapView.getMapOrientation() - 10);
        } else if (pItem.getItemId() == MENU_ROTATE_CW + pMenuIdOffset) {
            mMapView.setMapOrientation(mMapView.getMapOrientation() + 10);
        }

        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu pMenu, final int pMenuIdOffset, final MapView pMapView) {
        pMenu.findItem(MENU_ENABLED + pMenuIdOffset).setTitle(
                this.isEnabled() ? "Disable rotation" : "Enable rotation");
        return false;
    }

    @Override
    public void setOptionsMenuEnabled(boolean enabled) {
        mOptionsMenuEnabled = enabled;
    }

    @Override
    public void setEnabled(final boolean pEnabled) {
        mRotationDetector.setEnabled(pEnabled);
        super.setEnabled(pEnabled);
    }
}
