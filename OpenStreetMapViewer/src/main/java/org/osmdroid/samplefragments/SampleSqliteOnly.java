package org.osmdroid.samplefragments;

import android.os.Environment;
import android.widget.Toast;

import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MapTileAssetsProvider;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

import java.io.File;
import java.util.Set;


/**
 * test to force assets only loaded
 * https://github.com/osmdroid/osmdroid/issues/272
 * Created by alex on 2/21/16.
 */
public class SampleSqliteOnly extends BaseSampleFragment {
    @Override
    public String getSampleTitle() {
        return "SQLite Only";
    }

    @Override
    public void addOverlays() {
        OpenStreetMapTileProviderConstants.DEBUGMODE=true;
        OpenStreetMapTileProviderConstants.DEBUG_TILE_PROVIDERS=true;
        this.mMapView.setUseDataConnection(false);

        //first we'll look at the default location for tiles that we support
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/osmdroid/");
        if (f.exists()){

            File[] list = f.listFiles();
            for (int i=0; i < list.length; i++){
                if (list[i].isDirectory())
                    continue;
                String name = list[i].getName().toLowerCase();
                if (!name.contains("."))
                    continue; //skip files without an extension
                name=name.substring(name.lastIndexOf(".")+1);
                if (name.length() ==0)
                    continue;
                //narrow it down to only sqlite tiles
                if (ArchiveFileFactory.isFileExtensionRegistered(name) &&
                        name.equals("sqlite")) {
                    try {

                        //ok found a file we support and have a driver for the format, for this demo, we'll just use the first one

                        //create the offline tile provider, it will only do offline file archives
                        //again using the first file
                        OfflineTileProvider tileProvider= new OfflineTileProvider(new SimpleRegisterReceiver(getActivity()),
                                new File[]{list[i]});
                        //tell osmdroid to use that provider instead of the default rig which is (asserts, cache, files/archives, online
                        mMapView.setTileProvider(tileProvider);

                        //this bit enables us to find out what tiles sources are available. note, that this action may take some time to run
                        //and should be ran asynchronously. we've put it inline for simplicity

                        String source="";
                        IArchiveFile[] archives= tileProvider.getArchives();
                        if (archives.length > 0){
                            //cheating a bit here, get the first archive file and ask for the tile sources names it contains
                            Set<String> tileSources = archives[0].getTileSources();
                            //presumably, this would be a great place to tell your users which tiles sources are available
                            if (!tileSources.isEmpty()) {
                                //ok good, we found at least one tile source, create a basic file based tile source using that name
                                //and set it. If we don't set it, osmdroid will attempt to use the default source, which is "MAPNIK",
                                //which probably won't match your offline tile source, unless it's MAPNIK
                                source = tileSources.iterator().next();
                                this.mMapView.setTileSource(FileBasedTileSource.getSource(source));
                            }
                            else{
                                this.mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                            }

                        } else
                            this.mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);

                        Toast.makeText(getActivity(), "Using " + list[i].getAbsolutePath() + " " + source, Toast.LENGTH_LONG).show();
                        this.mMapView.invalidate();
                        return;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            Toast.makeText(getActivity(), f.getAbsolutePath() +  " did not have any files I can open! Try using MOBAC", Toast.LENGTH_LONG).show();
        } else{
            Toast.makeText(getActivity(), f.getAbsolutePath() + " dir not found!", Toast.LENGTH_LONG).show();
        }


    }
}
