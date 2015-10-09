package org.osmdroid.samplefragments;

import android.widget.Toast;
import java.io.File;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

/**
 *
 * @author alex
 */
public class SampleOfflineOnly extends BaseSampleFragment {

     @Override
     public String getSampleTitle() {
          return "Offline Only Tiles";
     }
     
     @Override
     public void addOverlays() {
          //not even needed!
          this.mMapView.setUseDataConnection(false);
          
          File f = new File("/sdcard/osmdroid/");
          if (f.exists()){
          
               File[] list = f.listFiles();
               for (int i=0; i < list.length; i++){
                    if (list[i].getAbsolutePath().toLowerCase().endsWith(".sqlite") ||
                         list[i].getAbsolutePath().toLowerCase().endsWith(".zip") ||
                         list[i].getAbsolutePath().toLowerCase().endsWith(".mbtiles") ||
                         list[i].getAbsolutePath().toLowerCase().endsWith(".gemf")){
                         try {
                              //important, the file name, minus the extension
                              //must match the name of the tile source.
                              
                              //in zip files, this is the first directly
                              //in sqlite, it's the column for tile soure
                              this.mMapView.setTileProvider(new OfflineTileProvider(new SimpleRegisterReceiver(getActivity()),
                                   new File[]{list[i]}));
                              Toast.makeText(getActivity(), "Using " + list[i].getAbsolutePath(), Toast.LENGTH_LONG).show();
                              this.mMapView.invalidate();
                              return;
                         } catch (Exception ex) {
                              ex.printStackTrace();
                         }
                    }
               }
               Toast.makeText(getActivity(), "/sdcard/osmdroid/ did not have any files I can open! Try using MOBAC", Toast.LENGTH_LONG).show();
          } else{
               Toast.makeText(getActivity(), "/sdcard/osmdroid dir not found!", Toast.LENGTH_LONG).show();
          }
          
	}
     
}
