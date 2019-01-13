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
import org.osmdroid.gpkg.tiles.raster.GeoPackageProvider;
import org.osmdroid.gpkg.tiles.raster.GeopackageRasterTileSource;
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

import static org.osmdroid.samplefragments.events.SampleMapEventListener.df;

/**
 * One way for viewing geopackage tiles to the osmdroid view
 * <p>
 * created on 1/12/2017.
 *
 * @author Alex O'Ree
 * @since 5.6.3
 */

public class GeopackageSample extends BaseSampleFragment {
    TextView textViewCurrentLocation;

    XYTileSource currentSource = null;
    GeoPackageProvider geoPackageProvider = null;
    android.app.AlertDialog alertDialog = null;
    @Override
    public String getSampleTitle() {
        return "Geopackage Raster Tiles";
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
        //first let's up our map source, geopackage needs you to explicitly specify which map files to load
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
            geoPackageProvider = new GeoPackageProvider(maps, this.getContext());
            mMapView.setTileProvider(geoPackageProvider);
            boolean sourceSet = false;

            List<GeopackageRasterTileSource> tileSources = geoPackageProvider.geoPackageMapTileModuleProvider().getTileSources();
            if (!tileSources.isEmpty()) {
                mMapView.setTileSource(tileSources.get(0));

                mMapView.zoomToBoundingBox(tileSources.get(0).getBounds(), true);
                mMapView.getController().setZoom(tileSources.get(0).getMinimumZoomLevel());
                sourceSet = true;
            }


            if (!sourceSet) {
                Toast.makeText(getContext(), "No tile source is available, get your geopackages for 'tiles' tables", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Tile source set to " + mMapView.getTileProvider().getTileSource().name(), Toast.LENGTH_LONG).show();

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
        if (geoPackageProvider != null)
            geoPackageProvider.detach();

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
    protected static Set<File> findMapFiles() {
        Set<File> maps = new HashSet<>();
        List<StorageUtils.StorageInfo> storageList = StorageUtils.getStorageList();
        for (int i = 0; i < storageList.size(); i++) {
            File f = new File(storageList.get(i).path + File.separator + "osmdroid" + File.separator);
            if (f.exists()) {
                maps.addAll(scan(f));
            }
        }
        return maps;
    }

    static private Collection<? extends File> scan(File f) {
        List<File> ret = new ArrayList<>();
        File[] files = f.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().toLowerCase().endsWith(".gpkg"))
                    return true;
                return false;
            }
        });
        if (files != null) {
            Collections.addAll(ret, files);
        }
        return ret;
    }
}
