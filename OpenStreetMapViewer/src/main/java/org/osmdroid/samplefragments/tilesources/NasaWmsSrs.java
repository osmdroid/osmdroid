package org.osmdroid.samplefragments.tilesources;

import org.osmdroid.wms.WMSEndpoint;
import org.osmdroid.wms.WMSLayer;
import org.osmdroid.wms.WMSParser;

/**
 * A simple demo work for working with WMS endpoints. Tested and functional
 * NASA 1.1.1 WMS
 * created on 8/20/2017.
 *
 * @author Alex O'Ree
 * @see WMSLayer
 * @see WMSParser
 * @see WMSEndpoint
 * @since 5.6.5
 */

public class NasaWmsSrs extends SampleWMSSource {
    @Override
    public String getSampleTitle() {
        return "NASA WMS SRS";
    }

    protected String getDefaultUrl() {

        return "https://svs.gsfc.nasa.gov/cgi-bin/wms?version=1.1.1&service=WMS&request=GetCapabilities";
    }


}
