package org.osmdroid.samplefragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;

public abstract class BaseSampleFragment extends Fragment {
    private static int MENU_LAST_ID = Menu.FIRST; // Always set to last unused id
    public static final String TAG = "osmBaseFrag";

    public abstract String getSampleTitle();

    // ===========================================================
    // Fields
    // ===========================================================

    protected MapView mMapView;

    public MapView getmMapView() {
        return mMapView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMapView = new MapView(inflater.getContext());
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
        Log.d(TAG, "onCreateView");
        return mMapView;
    }


	@Override
	public void onPause(){
        if (mMapView != null) {
            mMapView.onPause();
        }
		super.onPause();
	}

	@Override
	public void onResume(){
		super.onResume();
		if (mMapView != null) {
            mMapView.onResume();
        }
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");

        if (mMapView != null) {
            addOverlays();

            final Context context = this.getActivity();
            final DisplayMetrics dm = context.getResources().getDisplayMetrics();

            CopyrightOverlay copyrightOverlay = new CopyrightOverlay(getActivity());
            copyrightOverlay.setTextSize(10);

            mMapView.getOverlays().add(copyrightOverlay);
			mMapView.setMultiTouchControls(true);
			mMapView.setTilesScaledToDpi(true);
		}
	}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDetach");
        if (mMapView != null)
            mMapView.onDetach();
        mMapView = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

    }

    int MENU_VERTICAL_REPLICATION = 0;
    int MENU_HORIZTONAL_REPLICATION = 0;
    int MENU_ROTATE_CLOCKWISE = 0;
    int MENU_ROTATE_COUNTER_CLOCKWISE = 0;
    int MENU_SCALE_TILES = 0;


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem add = menu.add("Run Tests");
        MENU_LAST_ID++;
        MENU_VERTICAL_REPLICATION = MENU_LAST_ID;
        menu.add(0, MENU_VERTICAL_REPLICATION, Menu.NONE, "Vertical Replication").setCheckable(true);
        MENU_LAST_ID++;
        MENU_HORIZTONAL_REPLICATION = MENU_LAST_ID;
        menu.add(0, MENU_HORIZTONAL_REPLICATION, Menu.NONE, "Horizontal Replication").setCheckable(true);

        MENU_LAST_ID++;
        MENU_SCALE_TILES = MENU_LAST_ID;
        menu.add(0, MENU_SCALE_TILES, Menu.NONE, "Scale Tiles").setCheckable(true);

        MENU_LAST_ID++;
        MENU_ROTATE_CLOCKWISE = MENU_LAST_ID;
        menu.add(0, MENU_ROTATE_CLOCKWISE, Menu.NONE, "Rotate Clockwise");

        MENU_LAST_ID++;
        MENU_ROTATE_COUNTER_CLOCKWISE = MENU_LAST_ID;
        menu.add(0, MENU_ROTATE_COUNTER_CLOCKWISE, Menu.NONE, "Rotate Counter Clockwise");
        // Put overlay items first
        try {
            mMapView.getOverlayManager().onCreateOptionsMenu(menu, MENU_LAST_ID, mMapView);
        } catch (NullPointerException npe) {
            //can happen during CI tests and very rapid fragment switching
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        try {
            MenuItem item = menu.findItem(MENU_VERTICAL_REPLICATION);
            item.setChecked(mMapView.isVerticalMapRepetitionEnabled());
            item = menu.findItem(MENU_HORIZTONAL_REPLICATION);
            item.setChecked(mMapView.isHorizontalMapRepetitionEnabled());

            item = menu.findItem(MENU_SCALE_TILES);
            item.setChecked(mMapView.isTilesScaledToDpi());
            mMapView.getOverlayManager().onPrepareOptionsMenu(menu, MENU_LAST_ID, mMapView);
        } catch (NullPointerException npe) {
            //can happen during CI tests and very rapid fragment switching
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().equals("Run Tests")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        runTestProcedures();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            return true;
        } else if (item.getItemId() == MENU_HORIZTONAL_REPLICATION) {
            mMapView.setHorizontalMapRepetitionEnabled(!mMapView.isHorizontalMapRepetitionEnabled());
            mMapView.invalidate();
            return true;
        } else if (item.getItemId() == MENU_VERTICAL_REPLICATION) {
            mMapView.setVerticalMapRepetitionEnabled(!mMapView.isVerticalMapRepetitionEnabled());
            mMapView.invalidate();
            return true;
        } else if (item.getItemId() == MENU_SCALE_TILES) {
            mMapView.setTilesScaledToDpi(!mMapView.isTilesScaledToDpi());
            mMapView.invalidate();
            return true;
        } else if (item.getItemId() == MENU_ROTATE_CLOCKWISE){
            float currentRotation = mMapView.getMapOrientation() + 10;
            if (currentRotation > 360)
                currentRotation = currentRotation-360;
            mMapView.setMapOrientation(currentRotation, true);

            return true;
        } else if (item.getItemId() == MENU_ROTATE_COUNTER_CLOCKWISE){
            float currentRotation = mMapView.getMapOrientation() - 10;
            if (currentRotation < 0)
                currentRotation = currentRotation + 360;
            mMapView.setMapOrientation(currentRotation, true);
            return true;
        } else if (mMapView.getOverlayManager().onOptionsItemSelected(item, MENU_LAST_ID, mMapView)) {
            return true;
        }
        return false;
    }

    /**
     * An appropriate place to override and add overlays.
     */
    protected void addOverlays() {
        //
    }

    public boolean skipOnCiTests() {
        return true;
    }

    /**
     * optional place to put automated test procedures, used during the connectCheck tests
     * this is called OFF of the UI thread. block this method call util the test is done
     */
    public void runTestProcedures() throws Exception {

    }
}
