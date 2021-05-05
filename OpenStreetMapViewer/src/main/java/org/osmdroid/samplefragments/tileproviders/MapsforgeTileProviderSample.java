package org.osmdroid.samplefragments.tileproviders;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.rendertheme.AssetsRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.osmdroid.config.Configuration;
import org.osmdroid.mapsforge.MapsForgeTileProvider;
import org.osmdroid.mapsforge.MapsForgeTileSource;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.tileprovider.util.StorageUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An example of using MapsForge in osmdroid
 * created on 1/12/2017.
 *
 * @author Alex O'Ree
 */

public class MapsforgeTileProviderSample extends BaseSampleFragment {
    MapsForgeTileSource fromFiles = null;
    MapsForgeTileProvider forge = null;
    AlertDialog alertDialog = null;

    @Override
    public String getSampleTitle() {
        return "Mapsforge tiles";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);   //turn off the menu to prevent accidential tile source changes
        Log.d(TAG, "onCreate");

        /**
         * super important to configure some of the mapsforge settings first
         */
        MapsForgeTileSource.createInstance(this.getActivity().getApplication());
        /*
        not sure how important these are....
        MapFile.wayFilterEnabled = true;
        MapFile.wayFilterDistance = 20;
        MapWorkerPool.DEBUG_TIMING = true;
        MapWorkerPool.NUMBER_OF_THREADS = MapWorkerPool.DEFAULT_NUMBER_OF_THREADS;
*/

    }


    @Override
    public void addOverlays() {
        super.addOverlays();
        //first let's up our map source, mapsforge needs you to explicitly specify which map files to load
        //this bit does some basic file system scanning
        Set<File> mapfiles = findMapFiles();
        //do a simple scan of local storage for .map files.
        File[] maps = new File[mapfiles.size()];
        maps = mapfiles.toArray(maps);
        if (maps == null || maps.length == 0) {
            //show a warning that no map files were found
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    getContext());

            // set title
            alertDialogBuilder.setTitle("No Mapsforge files found");

            // set dialog message
            alertDialogBuilder
                    .setMessage("In order to render map tiles, you'll need to either create or obtain mapsforge .map files. See https://github.com/mapsforge/mapsforge for more info. Store them in "
                            + Configuration.getInstance().getOsmdroidBasePath().getAbsolutePath())
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (alertDialog != null) alertDialog.dismiss();
                        }
                    });


            // create alert dialog
            alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();

        } else {
            Toast.makeText(getContext(), "Loaded " + maps.length + " map files", Toast.LENGTH_LONG).show();

            //this creates the forge provider and tile sources

            //protip: when changing themes, you should also change the tile source name to prevent cached tiles

            //null is ok here, uses the default rendering theme if it's not set
            XmlRenderTheme theme = null;
            try {
                theme = new AssetsRenderTheme(getContext().getApplicationContext(), "renderthemes/", "rendertheme-v4.xml");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            fromFiles = MapsForgeTileSource.createFromFiles(maps, theme, "rendertheme-v4");
            forge = new MapsForgeTileProvider(
                    new SimpleRegisterReceiver(getContext()),
                    fromFiles, null);


            mMapView.setTileProvider(forge);


            //now for a magic trick
            //since we have no idea what will be on the
            //user's device and what geographic area it is, this will attempt to center the map
            //on whatever the map data provides
            mMapView.getController().setZoom(fromFiles.getMinimumZoomLevel());
            mMapView.zoomToBoundingBox(fromFiles.getBoundsOsmdroid(), true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (alertDialog != null) alertDialog.dismiss();
        alertDialog = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alertDialog != null) {
            alertDialog.hide();
            alertDialog.dismiss();
            alertDialog = null;
        }
        if (fromFiles != null)
            fromFiles.dispose();
        if (forge != null)
            forge.detach();
        AndroidGraphicFactory.clearResourceMemoryCache();
    }

    /**
     * simple function to scan for paths that match /something/osmdroid/*.map to find mapforge database files
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
                return pathname.getName().toLowerCase().endsWith(".map");
            }
        });
        if (files != null) {
            Collections.addAll(ret, files);
        }
        return ret;
    }
}
