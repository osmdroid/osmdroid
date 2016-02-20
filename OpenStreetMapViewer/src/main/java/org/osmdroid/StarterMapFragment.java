// Created by plusminus on 00:23:14 - 03.10.2008
package org.osmdroid;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.constants.OpenStreetMapConstants;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.samplefragments.SampleFactory;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

/**
 * Default map view activity.
 *
 * @author Marc Kurtz
 * @author Manuel Stahl
 *
 */
public class StarterMapFragment extends Fragment implements OpenStreetMapConstants {
    // ===========================================================
     // Constants
     // ===========================================================

     private static final int DIALOG_ABOUT_ID = 1;

     private static final int MENU_SAMPLES = Menu.FIRST + 1;
     private static final int MENU_ABOUT = MENU_SAMPLES + 1;

     private static final int MENU_LAST_ID = MENU_ABOUT + 1; // Always set to last unused id

    // ===========================================================
     // Fields
     // ===========================================================
     private SharedPreferences mPrefs;
     private MapView mMapView;
     private MyLocationNewOverlay mLocationOverlay;
     private CompassOverlay mCompassOverlay=null;
     private MinimapOverlay mMinimapOverlay;
     private ScaleBarOverlay mScaleBarOverlay;
     private RotationGestureOverlay mRotationGestureOverlay;

     public static StarterMapFragment newInstance() {
         return new StarterMapFragment();
     }

     @Override
     public void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
     }

     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

          mMapView = new MapView(inflater.getContext());
        // Call this method to turn off hardware acceleration at the View level but only if you run into problems ( please report them too!)
          // setHardwareAccelerationOff();
          return mMapView;
     }

     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     private void setHardwareAccelerationOff() {
          // Turn off hardware acceleration here, or in manifest
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
               mMapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
          }
     }

     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
          super.onActivityCreated(savedInstanceState);

          final Context context = this.getActivity();
          final DisplayMetrics dm = context.getResources().getDisplayMetrics();
          // mResourceProxy = new ResourceProxyImpl(getActivity().getApplicationContext());

          mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

          this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context),
               mMapView);

          mMinimapOverlay = new MinimapOverlay(context, mMapView.getTileRequestCompleteHandler());
          mMinimapOverlay.setWidth(dm.widthPixels / 5);
          mMinimapOverlay.setHeight(dm.heightPixels / 5);

          mScaleBarOverlay = new ScaleBarOverlay(mMapView);
          mScaleBarOverlay.setCentred(true);
          mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);

          mRotationGestureOverlay = new RotationGestureOverlay(context, mMapView);
          mRotationGestureOverlay.setEnabled(true);

          mMapView.setBuiltInZoomControls(true);
          mMapView.setMultiTouchControls(true);
          mMapView.getOverlays().add(this.mLocationOverlay);

          mMapView.getOverlays().add(this.mMinimapOverlay);
          mMapView.getOverlays().add(this.mScaleBarOverlay);
          mMapView.getOverlays().add(this.mRotationGestureOverlay);

          mMapView.getController().setZoom(mPrefs.getInt(PREFS_ZOOM_LEVEL, 1));
          mMapView.scrollTo(mPrefs.getInt(PREFS_SCROLL_X, 0), mPrefs.getInt(PREFS_SCROLL_Y, 0));

          mLocationOverlay.enableMyLocation();

         //sorry for the spaghetti code this is to filter out the compass on api 8
         //Note: the compass overlay causes issues on API 8 devices. See https://github.com/osmdroid/osmdroid/issues/218
          if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
              mCompassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context),
                      mMapView);
              mCompassOverlay.enableCompass();
              mMapView.getOverlays().add(this.mCompassOverlay);
          }

          setHasOptionsMenu(true);
     }

     @Override
     public void onPause() {
          final SharedPreferences.Editor edit = mPrefs.edit();
          edit.putString(PREFS_TILE_SOURCE, mMapView.getTileProvider().getTileSource().name());
          edit.putInt(PREFS_SCROLL_X, mMapView.getScrollX());
          edit.putInt(PREFS_SCROLL_Y, mMapView.getScrollY());
          edit.putInt(PREFS_ZOOM_LEVEL, mMapView.getZoomLevel());
          edit.putBoolean(PREFS_SHOW_LOCATION, mLocationOverlay.isMyLocationEnabled());

         //sorry for the spaghetti code this is to filter out the compass on api 8
         //Note: the compass overlay causes issues on API 8 devices. See https://github.com/osmdroid/osmdroid/issues/218
          if (mCompassOverlay!=null) {
              edit.putBoolean(PREFS_SHOW_COMPASS, mCompassOverlay.isCompassEnabled());
              this.mCompassOverlay.disableCompass();
          }
          edit.commit();

          this.mLocationOverlay.disableMyLocation();


          super.onPause();
     }

     @Override
     public void onResume() {
          super.onResume();
          final String tileSourceName = mPrefs.getString(PREFS_TILE_SOURCE,
                  TileSourceFactory.DEFAULT_TILE_SOURCE.name());
          try {
               final ITileSource tileSource = TileSourceFactory.getTileSource(tileSourceName);
               mMapView.setTileSource(tileSource);
          } catch (final IllegalArgumentException e) {
               mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
          }
          if (mPrefs.getBoolean(PREFS_SHOW_LOCATION, false)) {
               this.mLocationOverlay.enableMyLocation();
          }

         //sorry for the spaghetti code this is to filter out the compass on api 8
         //Note: the compass overlay causes issues on API 8 devices. See https://github.com/osmdroid/osmdroid/issues/218
          if (mPrefs.getBoolean(PREFS_SHOW_COMPASS, false)) {
              if (mCompassOverlay!=null)
                 this.mCompassOverlay.enableCompass();
          }
     }

     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
          // Put overlay items first
          mMapView.getOverlayManager().onCreateOptionsMenu(menu, MENU_LAST_ID, mMapView);

          // Put samples next
          SubMenu samplesSubMenu = menu.addSubMenu(0, MENU_SAMPLES, Menu.NONE, org.osmdroid.R.string.samples)
               .setIcon(android.R.drawable.ic_menu_gallery);
          SampleFactory sampleFactory = SampleFactory.getInstance();
          for (int a = 0; a < sampleFactory.count(); a++) {
               final BaseSampleFragment f = sampleFactory.getSample(a);
               samplesSubMenu.add(f.getSampleTitle()).setOnMenuItemClickListener(
                    new OnMenuItemClickListener() {
                         @Override
                         public boolean onMenuItemClick(MenuItem item) {
                              startSampleFragment(f);
                              return true;
                         }
                    });
          }

          // Put "About" menu item last
          menu.add(0, MENU_ABOUT, Menu.CATEGORY_SECONDARY, org.osmdroid.R.string.about).setIcon(
               android.R.drawable.ic_menu_info_details);

          super.onCreateOptionsMenu(menu, inflater);
     }

     protected void startSampleFragment(Fragment fragment) {
          FragmentManager fm = getFragmentManager();
          fm.beginTransaction().hide(this).add(android.R.id.content, fragment, "SampleFragment")
               .addToBackStack(null).commit();
     }

     @Override
     public void onPrepareOptionsMenu(final Menu pMenu) {
          mMapView.getOverlayManager().onPrepareOptionsMenu(pMenu, MENU_LAST_ID, mMapView);
          super.onPrepareOptionsMenu(pMenu);
     }

     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
          if (mMapView.getOverlayManager().onOptionsItemSelected(item, MENU_LAST_ID, mMapView)) {
               return true;
          }

          switch (item.getItemId()) {
               case MENU_ABOUT:
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                            .setTitle(org.osmdroid.R.string.app_name).setMessage(org.osmdroid.R.string.about_message)
                            .setIcon(org.osmdroid.R.drawable.icon)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int whichButton) {
                                    //
                               }
                            }
                    );
                    builder.create().show();
                    return true;
          }
          return super.onOptionsItemSelected(item);
     }

     public MapView getMapView() {
          return mMapView;
     }

    // @Override
     // public boolean onTrackballEvent(final MotionEvent event) {
     // return this.mMapView.onTrackballEvent(event);
     // }
}
