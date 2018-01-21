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
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.constants.OpenStreetMapConstants;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.samplefragments.SampleFactory;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
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
     private CopyrightOverlay mCopyrightOverlay;

     public static StarterMapFragment newInstance() {
         return new StarterMapFragment();
     }

     @Override
     public void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
     }

     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

         //Note! we are programmatically construction the map view
         //be sure to handle application lifecycle correct (see note in on pause)
          mMapView = new MapView(inflater.getContext());
        // Call this method to turn off hardware acceleration at the View level but only if you run into problems ( please report them too!)
          // setHardwareAccelerationOff();
         //update, no longer needed, the mapView is hardware acceleration off by defaul tnow

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
                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
                           && 0 != (event.getSource() & InputDevice.SOURCE_CLASS_POINTER)) {
                         switch (event.getAction()) {
                             case MotionEvent.ACTION_SCROLL:
                                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1
                                         && event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f)
                                     mMapView.getController().zoomOut();
                                 else {
                                     //this part just centers the map on the current mouse location before the zoom action occurs
                                     IGeoPoint iGeoPoint = mMapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
                                     mMapView.getController().animateTo(iGeoPoint);
                                     mMapView.getController().zoomIn();
                                 }
                                 return true;
                         }
                     }
                     return false;
                 }
             });
         }
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


          mCopyrightOverlay = new CopyrightOverlay(context);

         //i hate this very much, but it seems as if certain versions of android and/or
         //device types handle screen offsets differently
         if (Build.VERSION.SDK_INT <= 10)
             mCopyrightOverlay.setOffset(0,(int)(55*dm.density));

          mScaleBarOverlay = new ScaleBarOverlay(mMapView);
          mScaleBarOverlay.setCentred(true);
          mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);

          mRotationGestureOverlay = new RotationGestureOverlay(mMapView);
          mRotationGestureOverlay.setEnabled(true);

          mMapView.setBuiltInZoomControls(true);
          mMapView.setMultiTouchControls(true);

          mMapView.setTilesScaledToDpi(true);
          mMapView.getOverlays().add(this.mLocationOverlay);
          mMapView.getOverlays().add(this.mCopyrightOverlay);

          mMapView.getOverlays().add(this.mMinimapOverlay);
          mMapView.getOverlays().add(this.mScaleBarOverlay);
          mMapView.getOverlays().add(this.mRotationGestureOverlay);

          final float zoomLevel = mPrefs.getFloat(PREFS_ZOOM_LEVEL_DOUBLE, mPrefs.getInt(PREFS_ZOOM_LEVEL, 1));
          mMapView.getController().setZoom(zoomLevel);
          final float orientation = mPrefs.getFloat(PREFS_ORIENTATION, 0);
          mMapView.setMapOrientation(orientation, false);
          final String latitudeString = mPrefs.getString(PREFS_LATITUDE_STRING, null);
          final String longitudeString = mPrefs.getString(PREFS_LONGITUDE_STRING, null);
          if (latitudeString == null || longitudeString == null) { // case handled for historical reasons only
              final int scrollX = mPrefs.getInt(PREFS_SCROLL_X, 0);
              final int scrollY = mPrefs.getInt(PREFS_SCROLL_Y, 0);
              mMapView.scrollTo(scrollX, scrollY);
          } else {
              final double latitude = Double.valueOf(latitudeString);
              final double longitude = Double.valueOf(longitudeString);
              mMapView.setExpectedCenter(new GeoPoint(latitude, longitude));
          }

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
          edit.putFloat(PREFS_ORIENTATION, mMapView.getMapOrientation());
          edit.putString(PREFS_LATITUDE_STRING, String.valueOf(mMapView.getMapCenter().getLatitude()));
          edit.putString(PREFS_LONGITUDE_STRING, String.valueOf(mMapView.getMapCenter().getLongitude()));
          edit.putFloat(PREFS_ZOOM_LEVEL_DOUBLE, (float)mMapView.getZoomLevelDouble());
          edit.putBoolean(PREFS_SHOW_LOCATION, mLocationOverlay.isMyLocationEnabled());

         //sorry for the spaghetti code this is to filter out the compass on api 8
         //Note: the compass overlay causes issues on API 8 devices. See https://github.com/osmdroid/osmdroid/issues/218
          if (mCompassOverlay!=null) {
              edit.putBoolean(PREFS_SHOW_COMPASS, mCompassOverlay.isCompassEnabled());
              this.mCompassOverlay.disableCompass();
          }
          edit.commit();

          this.mLocationOverlay.disableMyLocation();


          mMapView.onPause();
          super.onPause();
     }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        //this part terminates all of the overlays and background threads for osmdroid
        //only needed when you programmatically create the map
        mMapView.onDetach();

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
              if (mCompassOverlay!=null) {
                  //this call is needed because onPause, the orientation provider is destroyed to prevent context leaks
                  this.mCompassOverlay.setOrientationProvider(new InternalCompassOrientationProvider(getActivity()));
                  this.mCompassOverlay.enableCompass();
              }
          }
         mMapView.onResume();
     }

     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
          // Put overlay items first
          mMapView.getOverlayManager().onCreateOptionsMenu(menu, MENU_LAST_ID, mMapView);

          // Put samples next
          SubMenu samplesSubMenu = menu.addSubMenu(0, MENU_SAMPLES, Menu.NONE, org.osmdroid.R.string.samples)
               .setIcon(android.R.drawable.ic_menu_gallery);
          ISampleFactory sampleFactory = SampleFactory.getInstance();
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
