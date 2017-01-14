package org.osmdroid.samplefragments.tileproviders;

import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

import java.io.File;
import java.util.Set;

/**
 * An example on how to setup osmdroid to only use offline map archives, how to
 * query the map archives for the available tile sources
 *
 * @author alex
 * @since 5.0
 */
public class SampleOfflineOnly extends BaseSampleFragment {

    @Override
    public String getSampleTitle() {
        return "Offline Only Tiles with custom 404 image";
    }

    @Override
    public void addOverlays() {
        //not even needed since we are using the offline tile provider only
        this.mMapView.setUseDataConnection(false);

        //https://github.com/osmdroid/osmdroid/issues/330
        //custom image placeholder for files that aren't available
        mMapView.getTileProvider().setTileLoadFailureImage(getResources().getDrawable(R.drawable.notfound));


        //first we'll look at the default location for tiles that we support
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/osmdroid/");
        if (f.exists()) {

            File[] list = f.listFiles();
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    if (list[i].isDirectory()) {
                        continue;
                    }
                    String name = list[i].getName().toLowerCase();
                    if (!name.contains(".")) {
                        continue; //skip files without an extension
                    }
                    name = name.substring(name.lastIndexOf(".") + 1);
                    if (name.length() == 0) {
                        continue;
                    }
                    if (ArchiveFileFactory.isFileExtensionRegistered(name)) {
                        try {

                            //ok found a file we support and have a driver for the format, for this demo, we'll just use the first one

                            //create the offline tile provider, it will only do offline file archives
                            //again using the first file
                            OfflineTileProvider tileProvider = new OfflineTileProvider(new SimpleRegisterReceiver(getActivity()),
                                    new File[]{list[i]});

                            //tell osmdroid to use that provider instead of the default rig which is (asserts, cache, files/archives, online
                            mMapView.setTileProvider(tileProvider);

                            //this bit enables us to find out what tiles sources are available. note, that this action may take some time to run
                            //and should be ran asynchronously. we've put it inline for simplicity

                            String source = "";
                            IArchiveFile[] archives = tileProvider.getArchives();
                            if (archives.length > 0) {
                                //cheating a bit here, get the first archive file and ask for the tile sources names it contains
                                Set<String> tileSources = archives[0].getTileSources();
                                //presumably, this would be a great place to tell your users which tiles sources are available
                                if (!tileSources.isEmpty()) {
                                    //ok good, we found at least one tile source, create a basic file based tile source using that name
                                    //and set it. If we don't set it, osmdroid will attempt to use the default source, which is "MAPNIK",
                                    //which probably won't match your offline tile source, unless it's MAPNIK
                                    source = tileSources.iterator().next();
                                    this.mMapView.setTileSource(FileBasedTileSource.getSource(source));
                                } else {
                                    this.mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                                }

                            } else {
                                this.mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                            }

                            Snackbar.make(getView(), "Using " + list[i].getAbsolutePath() + " " + source, Snackbar.LENGTH_SHORT).show();
                            this.mMapView.invalidate();
                            return;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            Toast.makeText(getActivity(), f.getAbsolutePath() + " did not have any files I can open! Try using MOBAC", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), f.getAbsolutePath() + " dir not found!", Toast.LENGTH_SHORT).show();
        }

    }

}
