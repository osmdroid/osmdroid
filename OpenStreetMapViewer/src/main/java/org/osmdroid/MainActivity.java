// Created by plusminus on 18:23:13 - 03.10.2008
package org.osmdroid;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.samples.SampleExtensive;
import org.osmdroid.samples.SampleWithMinimapItemizedoverlay;
import org.osmdroid.samples.SampleWithMinimapZoomcontrols;
import org.osmdroid.samples.SampleWithTilesOverlay;
import org.osmdroid.samples.SampleWithTilesOverlayAndCustomTileSource;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.MapTileDownloader;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.views.MapView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    public static final String TAG = "OSM";
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request permissions to support Android Marshmallow and above devices
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions();
        }


        //cache management starts here
        File discoveredBestPath = OpenStreetMapTileProviderConstants.TILE_PATH_BASE;

        //grab the current user preferences for debug settings and where to store the tile cache data
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        OpenStreetMapTileProviderConstants.setCachePath(prefs.getString("textViewCacheDirectory", discoveredBestPath.getAbsolutePath()));
        OpenStreetMapTileProviderConstants.DEBUGMODE=prefs.getBoolean("checkBoxDebugMode",false);
        OpenStreetMapTileProviderConstants.DEBUG_TILE_PROVIDERS=prefs.getBoolean("checkBoxDebugTileProvider",false);
        MapView.hardwareAccelerated=prefs.getBoolean("checkBoxHardwareAcceleration",false);
        MapTileDownloader.DEBUG = prefs.getBoolean("checkBoxDebugDownloading", false);

        //uncomment this to force a cache trim
        //OpenStreetMapTileProviderConstants.TILE_MAX_CACHE_SIZE_BYTES = 16000;

        if (Build.VERSION.SDK_INT >= 9) {
            //https://github/osmdroid/osmdroid/issues/435
            //On startup, we auto set the max cache size to be the current cache size + free disk space
            //this reduces the chance of osmdroid completely filling up the storage device

            //if the default max cache size is greater than the available free space
            //reduce it to 95% of the available free space + the size of the cache
            File dbFile = new File(OpenStreetMapTileProviderConstants.TILE_PATH_BASE.getAbsolutePath() + File.separator + SqlTileWriter.DATABASE_FILENAME);
            if (dbFile.exists()) {
                long cacheSize = dbFile.length();
                long freeSpace = OpenStreetMapTileProviderConstants.TILE_PATH_BASE.getFreeSpace();

                Log.i(TAG, "Current cache size is " + cacheSize + " free space is " + freeSpace);
                if (OpenStreetMapTileProviderConstants.TILE_MAX_CACHE_SIZE_BYTES > (freeSpace + cacheSize)){
                    OpenStreetMapTileProviderConstants.TILE_MAX_CACHE_SIZE_BYTES = (long)((freeSpace + cacheSize) * 0.95);
                    OpenStreetMapTileProviderConstants.TILE_TRIM_CACHE_SIZE_BYTES = (long)((freeSpace + cacheSize) * 0.90);
                }
            } else {
                //this is probably the first time running osmdroid
                long freeSpace = OpenStreetMapTileProviderConstants.TILE_PATH_BASE.length();
                if (OpenStreetMapTileProviderConstants.TILE_MAX_CACHE_SIZE_BYTES > (freeSpace)){
                    OpenStreetMapTileProviderConstants.TILE_MAX_CACHE_SIZE_BYTES = (long)((freeSpace) * 0.95);
                    OpenStreetMapTileProviderConstants.TILE_TRIM_CACHE_SIZE_BYTES = (long)((freeSpace) * 0.90);
                }
            }
        }

        //cache management ends here


        // Generate a ListView with Sample Maps
        final ArrayList<String> list = new ArrayList<>();
        list.add("OSMDroid Sample map (Start Here)");
        list.add("OSMapView with Minimap, ZoomControls, Animations, Scale Bar and MyLocationOverlay");
        list.add("OSMapView with ItemizedOverlay");
        list.add("OSMapView with Minimap and ZoomControls");
        list.add("Sample with tiles overlay");
        list.add("Sample with tiles overlay and custom tile source");
        list.add("More Samples");
        list.add("Bug Drivers");
        list.add("Report a bug");
        list.add("Settings");

        ListView lv = (ListView) findViewById(R.id.activitylist);
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);

        TextView tv = (TextView) findViewById(R.id.sdcardstate_value);
        final String state = Environment.getExternalStorageState();

        boolean mSdCardAvailable = Environment.MEDIA_MOUNTED.equals(state);
        tv.setText(mSdCardAvailable ? "Mounted" : "Not Available");
        if (!mSdCardAvailable) {
            tv.setTextColor(Color.RED);
            tv.setTypeface(null, Typeface.BOLD);
        }

        tv = (TextView) findViewById(R.id.version_text);
        tv.setText(BuildConfig.VERSION_NAME + " " + BuildConfig.BUILD_TYPE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                this.startActivity(new Intent(this, StarterMapActivity.class));
                break;
            case 1:
                this.startActivity(new Intent(this, SampleExtensive.class));
                break;
            case 2:
                this.startActivity(new Intent(this, SampleWithMinimapItemizedoverlay.class));
                break;
            case 3:
                this.startActivity(new Intent(this, SampleWithMinimapZoomcontrols.class));
                break;
            case 4:
                this.startActivity(new Intent(this, SampleWithTilesOverlay.class));
                break;
            case 5:
                this.startActivity(new Intent(this, SampleWithTilesOverlayAndCustomTileSource.class));
                break;
            case 6:
                this.startActivity(new Intent(this, ExtraSamplesActivity.class));
                break;
            case 7:
                this.startActivity(new Intent(this, BugsTestingActivity.class));
                break;
            case 8:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/osmdroid/osmdroid/issues"));
                startActivity(browserIntent);
                break;
            case 9:
                Intent i = new Intent(this,PreferenceActivity.class);
                startActivity(i);
                break;
        }
    }

    // START PERMISSION CHECK
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        String message = "osmdroid permissions:";
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            message += "\nLocation to show user location.";
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            message += "\nStorage access to store map tiles.";
        }
        if (!permissions.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            String[] params = permissions.toArray(new String[permissions.size()]);
            requestPermissions(params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        } // else: We already have permissions, so handle as normal
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION and WRITE_EXTERNAL_STORAGE
                Boolean location = perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                Boolean storage = perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                if (location && storage) {
                    // All Permissions Granted
                    Toast.makeText(MainActivity.this, "All permissions granted", Toast.LENGTH_SHORT).show();
                } else if (location) {
                    Toast.makeText(this, "Storage permission is required to store map tiles to reduce data usage and for offline usage.", Toast.LENGTH_LONG).show();
                } else if (storage) {
                    Toast.makeText(this, "Location permission is required to show the user's location on map.", Toast.LENGTH_LONG).show();
                } else { // !location && !storage case
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "Storage permission is required to store map tiles to reduce data usage and for offline usage." +
                            "\nLocation permission is required to show the user's location on map.", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // END PERMISSION CHECK
}
