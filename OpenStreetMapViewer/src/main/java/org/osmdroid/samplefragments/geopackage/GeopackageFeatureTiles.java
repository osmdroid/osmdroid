package org.osmdroid.samplefragments.geopackage;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.R;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.gpkg.tiles.feature.GeoPackageFeatureTileProvider;
import org.osmdroid.gpkg.tiles.feature.GeopackageFeatureTilesOverlay;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.StorageUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.factory.GeoPackageFactory;

import static org.osmdroid.samplefragments.events.SampleMapEventListener.df;

/**
 * One way for viewing geopackage tiles to the osmdroid view
 * converts geopackage features to rendered tiles for viewing in osmdroid
 * <p>
 * created on 8/19/2017.
 *
 * @author Alex O'Ree
 * @ since.6.0.0
 */

public class GeopackageFeatureTiles extends BaseSampleFragment {
    TextView textViewCurrentLocation;

    XYTileSource currentSource = null;

    android.app.AlertDialog alertDialog = null;

    @Override
    public String getSampleTitle() {
        return "Geopackage Feature Tiles";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d(TAG, "onCreate");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.map_with_locationbox, container, false);
        mMapView = root.findViewById(R.id.mapview);

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

        textViewCurrentLocation = root.findViewById(R.id.textViewCurrentLocation);
        return root;
    }


    @Override
    public void addOverlays() {
        super.addOverlays();
        //first let's up our map source, mapsforge needs you to explicitly specify which map files to load
        //this bit does some basic file system scanning
        Set<File> mapfiles = findMapFiles();
        //do a simple scan of local storage for .gpkg files.
        File[] maps = new File[mapfiles.size()];
        maps = mapfiles.toArray(maps);
        if (maps.length == 0) {
            //show a warning that no map files were found
            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(
                    getContext());

            // set title
            alertDialogBuilder.setTitle("No Geopackage files found");

            // set dialog message
            alertDialogBuilder
                    .setMessage("In order to render map tiles, you'll need to either create or obtain .gpkg files. See http://www.geopackage.org/ for more info. Place them in "
                            + Configuration.getInstance().getOsmdroidBasePath().getAbsolutePath())
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (alertDialog != null) {
                                alertDialog.hide();
                                alertDialog.dismiss();
                            }
                        }
                    });


            // create alert dialog
            alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();

        } else {
            Toast.makeText(getContext(), "Loaded " + maps.length + " map files", Toast.LENGTH_LONG).show();

            // Get a manager
            GeoPackageManager manager = GeoPackageFactory.getManager(getContext());

            // Available databases
            List<String> databases = manager.databases();

            // Import database
            for (File f : maps) {
                try {
                    boolean imported = manager.importGeoPackage(f);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            if (!databases.isEmpty()) {

                for (int k = 0; k < databases.size(); k++) {
                    // Open database
                    GeoPackage geoPackage = manager.open(databases.get(k));
                    // Feature tile tables
                    List<String> features = geoPackage.getFeatureTables();
                    // Query Features
                    if (!features.isEmpty()) {
                        for (int i = 0; i < features.size(); i++) {
                            GeoPackageFeatureTileProvider provider = new GeoPackageFeatureTileProvider(
                                    new XYTileSource(databases.get(k) + ":" + features.get(i), 0, 22, 256, "png", new String[0])
                            );
                            GeopackageFeatureTilesOverlay overlay = new GeopackageFeatureTilesOverlay(provider, getContext());
                            overlay.setDatabaseAndFeatureTable(databases.get(k), features.get(i));
                            mMapView.getOverlayManager().add(overlay);
                        }
                    }
                }
            }
        }

        mMapView.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                Log.i(IMapView.LOGTAG, System.currentTimeMillis() + " onScroll " + event.getX() + "," + event.getY());
                updateInfo();
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                Log.i(IMapView.LOGTAG, System.currentTimeMillis() + " onZoom " + event.getZoomLevel());
                updateInfo();
                return true;
            }
        });
        updateInfo();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (alertDialog != null) {
            alertDialog.hide();
            alertDialog.dismiss();
        }
        alertDialog = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alertDialog != null) {
            alertDialog.hide();
            alertDialog.dismiss();
        }
        alertDialog = null;
        this.currentSource = null;


    }


    private void updateInfo() {
        StringBuilder sb = new StringBuilder();
        IGeoPoint mapCenter = mMapView.getMapCenter();
        sb.append(df.format(mapCenter.getLatitude()) + "," +
                df.format(mapCenter.getLongitude())
                + ",zoom=" + mMapView.getZoomLevelDouble());

        if (currentSource != null) {
            sb.append("\n");
            sb.append(currentSource.name() + "," + currentSource.getBaseUrl());
        }


        textViewCurrentLocation.setText(sb.toString());
    }

    /**
     * simple function to scan for paths that match /something/osmdroid/*.map to find database files
     *
     * @return
     */
    protected Set<File> findMapFiles() {
        Set<File> maps = new HashSet<>();
        List<StorageUtils.StorageInfo> storageList = StorageUtils.getStorageList(getActivity());
        for (int i = 0; i < storageList.size(); i++) {
            File f = new File(storageList.get(i).path + File.separator + "osmdroid" + File.separator);
            if (f.exists()) {
                maps.addAll(scan(f));
            }
        }
        return maps;
    }

    private Collection<? extends File> scan(File f) {
        List<File> ret = new ArrayList<>();
        File[] files = f.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().toLowerCase().endsWith(".gpkg");
            }
        });
        if (files != null) {
            Collections.addAll(ret, files);
        }
        return ret;
    }
}
