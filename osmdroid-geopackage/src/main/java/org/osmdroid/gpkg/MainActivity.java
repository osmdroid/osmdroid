package org.osmdroid.gpkg;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.util.StorageUtils;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;
import java.io.FileFilter;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity implements MapListener {
    MapView mOsmv;
    TextView viewcenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        //first let's up our map source, mapsforge needs you to explicitly specify which map files to load
        //this bit does some basic file system scanning
        Set<File> mapfiles = findMapFiles();
        //do a simple scan of local storage for .gpkg files.
        File[] maps = new File[mapfiles.size()];
        maps = mapfiles.toArray(maps);
        if (maps.length==0){
            //show a warning that no map files were found
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

            // set title
            alertDialogBuilder.setTitle("No Mapsforge files found");

            // set dialog message
            alertDialogBuilder
                .setMessage("In order to render map tiles, you'll need to either create or obtain mapsforge .map files. See https://github.com/mapsforge/mapsforge for more info.")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {

                    }
                });


            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();

        }
        else
            Toast.makeText(this, "Loaded " + maps.length + " map files", Toast.LENGTH_LONG).show();


        //ACK! terrible idea, ArchiveFileFactory.registerArchiveFileProvider(GeoPackageFileArchiveProvider.class, "gpkg");


        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        //Configuration.getInstance().setDebugTileProviders(true);
        Configuration.getInstance().setDebugMode(true);
        setContentView(R.layout.activity_main);




        mOsmv = (MapView) findViewById(R.id.mapview);

        GeoPackageProvider geoPackageProvider = new GeoPackageProvider(maps, this.getApplicationContext());
        mOsmv.setTileProvider(geoPackageProvider);
        List<GeoPackageMapTileModuleProvider.Container> tileSources = geoPackageProvider.geoPackageMapTileModuleProvider().getTileSources();

        boolean sourceSet=false;
        for (int i=0; i < tileSources.size(); i++) {
            //this is a list of geopackages, since we only support tile tables, pick the first one of those
            if (tileSources.get(i).tiles!=null && !tileSources.get(i).tiles.isEmpty()) {
                mOsmv.setTileSource(GeoPackageProvider.getTileSource(tileSources.get(i).database,
                    tileSources.get(0).tiles.get(i)
                ));
                sourceSet = true;
            }
        }


        if (!sourceSet) {
            Toast.makeText(this, "No tile source is available, get your geopackages for 'tiles' tables", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Tile source set to " + mOsmv.getTileProvider().getTileSource().name(), Toast.LENGTH_LONG).show();
        }
        mOsmv.setUseDataConnection(false);
        mOsmv.setMultiTouchControls(true);
        mOsmv.setBuiltInZoomControls(true);
        mOsmv.setMapListener(this);
        mOsmv.setMinZoomLevel(0);
        mOsmv.setMaxZoomLevel(18);


        viewcenter= (TextView) findViewById(R.id.viewcenter);
        updateView();

    }

    @Override
    public void onStart() {
        super.onStart();
        //white horse sample set
        mOsmv.getController().setCenter(new GeoPoint(60.84892, -135.18848));
        mOsmv.getController().setZoom(12);
    }

    /**
     * simple function to scan for paths that match /something/osmdroid/*.map to find mapforge database files
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
            for (int i = 0; i < files.length; i++) {
                ret.add(files[i]);
            }
        }
        return ret;
    }

    @Override
    public boolean onScroll(ScrollEvent event) {
        updateView();
        return false;
    }

    @Override
    public boolean onZoom(ZoomEvent event) {
        updateView();
        return false;
    }

    private void updateView() {
        IGeoPoint mapCenter = mOsmv.getMapCenter();
        viewcenter.setText(mapCenter.getLatitude() + "," + mapCenter.getLongitude() + " " + mOsmv.getZoomLevel());
    }
}
