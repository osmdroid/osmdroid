package org.osmdroid.wms;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ParserTest {

    @Test
    public void testParserGeoserver() throws Exception {

        File input = new File("./src/test/resources/geoserver_getcapabilities_1.1.0.xml");
        if (!input.exists()) {

            Assert.fail(new File(".").getAbsolutePath() + " = pwd. target file doesn't exist at " + input.getAbsolutePath());
        }
        FileInputStream fis = new FileInputStream(input);
        WMSEndpoint cap = WMSParser.parse(fis);
        fis.close();


        verify(cap);
        Assert.assertTrue(cap.getLayers().size() == 22);
        Assert.assertEquals("1.1.1", cap.getWmsVersion());
    }

    @Ignore //only ignored to support offline builds
    @Test
    public void testUSGS() throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL("https://basemap.nationalmap.gov/arcgis/services/USGSTopo/MapServer/WMSServer?request=GetCapabilities&service=WMS").openConnection();
        InputStream is = c.getInputStream();
        WMSEndpoint cap = WMSParser.parse(is);
        Assert.assertNotNull(cap);

        is.close();
        c.disconnect();

        verify(cap);
        Assert.assertTrue(cap.getLayers().size() >= 1);
    }

    private void verify(WMSEndpoint cap) {
        Assert.assertNotNull(cap);
        Assert.assertNotNull(cap.getBaseurl());
        Assert.assertFalse(cap.getLayers().isEmpty());
        for (int i = 0; i < cap.getLayers().size(); i++) {
            WMSLayer wmsLayer = cap.getLayers().get(i);
            Assert.assertNotNull(wmsLayer.getName() + wmsLayer.getDescription() + wmsLayer.getTitle(), wmsLayer.getName());
//            Assert.assertNotNull(wmsLayer.getName() + wmsLayer.getDescription() + wmsLayer.getTitle(), wmsLayer.getDescription());
            Assert.assertNotNull(wmsLayer.getName() + wmsLayer.getDescription() + wmsLayer.getTitle(), wmsLayer.getTitle());
        }
    }

    @Test
    public void testUSGSFile2() throws Exception {
        File input = new File("./src/test/resources/basemapNationalMapGov.xml");
        if (!input.exists()) {

            Assert.fail(new File(".").getAbsolutePath() + " = pwd. target file doesn't exist at " + input.getAbsolutePath());
        }
        FileInputStream fis = new FileInputStream(input);
        WMSEndpoint cap = WMSParser.parse(fis);
        fis.close();
        verify(cap);
        Assert.assertEquals("https://basemap.nationalmap.gov:443/arcgis/services/USGSTopo/MapServer/WmsServer?", cap.getBaseurl());
        Assert.assertTrue(cap.getLayers().size() == 1);
        Assert.assertEquals("1.3.0", cap.getWmsVersion());
        Assert.assertFalse(cap.getLayers().get(0).getStyles().isEmpty());
    }

    @Test
    public void testUSGSFile() throws Exception {

        File input = new File("./src/test/resources/usgs_getcapabilities.xml");
        if (!input.exists()) {

            Assert.fail(new File(".").getAbsolutePath() + " = pwd. target file doesn't exist at " + input.getAbsolutePath());
        }
        FileInputStream fis = new FileInputStream(input);
        WMSEndpoint cap = WMSParser.parse(fis);
        fis.close();
        Assert.assertEquals("http://basemap.nationalmap.gov/arcgis/services/USGSTopo/MapServer/WmsServer?", cap.getBaseurl());
        verify(cap);
        Assert.assertTrue(cap.getLayers().size() == 1);
        Assert.assertEquals("1.3.0", cap.getWmsVersion());
    }

    @Test
    public void testNASA111File() throws Exception {

        File input = new File("./src/test/resources/nasawms111.xml");
        if (!input.exists()) {

            Assert.fail(new File(".").getAbsolutePath() + " = pwd. target file doesn't exist at " + input.getAbsolutePath());
        }
        FileInputStream fis = new FileInputStream(input);
        WMSEndpoint cap = WMSParser.parse(fis);
        fis.close();

        verify(cap);
        Assert.assertTrue(cap.getLayers().size() == 129);
        Assert.assertEquals("1.1.1", cap.getWmsVersion());
    }

    @Test
    public void testNASA130File() throws Exception {

        File input = new File("./src/test/resources/nasawms130.xml");
        if (!input.exists()) {

            Assert.fail(new File(".").getAbsolutePath() + " = pwd. target file doesn't exist at " + input.getAbsolutePath());
        }
        FileInputStream fis = new FileInputStream(input);
        WMSEndpoint cap = WMSParser.parse(fis);
        fis.close();

        Assert.assertEquals("https://neo.sci.gsfc.nasa.gov/wms/wms", cap.getBaseurl());
        verify(cap);
        Assert.assertTrue(cap.getLayers().size() == 129);
        Assert.assertEquals("1.3.0", cap.getWmsVersion());
    }

    @Test
    public void testNASA130SRSFile() throws Exception {

        File input = new File("./src/test/resources/nasasvs.xml");
        if (!input.exists()) {

            Assert.fail(new File(".").getAbsolutePath() + " = pwd. target file doesn't exist at " + input.getAbsolutePath());
        }
        FileInputStream fis = new FileInputStream(input);
        WMSEndpoint cap = WMSParser.parse(fis);
        fis.close();

        Assert.assertEquals("http://svs.gsfc.nasa.gov/cgi-bin/wms?", cap.getBaseurl());
        verify(cap);
        Assert.assertEquals(288, cap.getLayers().size());
        Assert.assertEquals("1.1.1", cap.getWmsVersion());
        for (int i = 0; i < cap.getLayers().size(); i++) {
            WMSLayer wmsLayer = cap.getLayers().get(i);
            if (wmsLayer.getName().equals("3238_22718_705010")) {
                Assert.assertEquals(1024, wmsLayer.getPixelSize());
                Assert.assertTrue(wmsLayer.getSrs().contains("EPSG:4326"));
            }
        }
    }
}
