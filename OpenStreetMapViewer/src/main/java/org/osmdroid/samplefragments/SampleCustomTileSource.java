package org.osmdroid.samplefragments;

import org.osmdroid.samplefragments.models.USGSTileSource;

/**
 * Simple how to for setting a custom tile source
 * @author alex
 */
public class SampleCustomTileSource extends BaseSampleFragment{

     @Override
     public String getSampleTitle() {
          return "Custom Tile Source";
          
     }
      @Override
     public void addOverlays() {
          mMapView.setTileSource(new USGSTileSource());
          
     }
     
}
