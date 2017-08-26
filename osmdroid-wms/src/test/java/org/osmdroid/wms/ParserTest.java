package org.osmdroid.wms;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class
    , sdk = 21)
public class ParserTest{

    @Test
    public void testParserGeoserver() throws Exception {

        File input = new File("./src/test/resources/geoserver_getcapabilities_1.1.0.xml");
        if (!input.exists()){

            Assert.fail(new File(".").getAbsolutePath() + " = pwd. target file doesn't exist at " + input.getAbsolutePath());
        }
        FileInputStream fis = new FileInputStream(input);
        WMSEndpoint cap = WMSParser.parse(fis);
        fis.close();


        verify(cap);
        Assert.assertTrue(cap.getLayers().size()==22);
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
        Assert.assertTrue(cap.getLayers().size()>=1);

    }

    private void verify(WMSEndpoint cap) {
        Assert.assertNotNull(cap);
        Assert.assertNotNull(cap.getBaseurl());
        Assert.assertFalse(cap.getLayers().isEmpty());
        for (int i=0; i < cap.getLayers().size(); i++) {
            WMSLayer wmsLayer = cap.getLayers().get(i);
            Assert.assertNotNull(wmsLayer.getName());
            Assert.assertNotNull(wmsLayer.getDescription());
            Assert.assertNotNull(wmsLayer.getTitle());

        }
    }

    @Test
    public void testUSGSFile2() throws Exception {
        File input = new File("./src/test/resources/basemapNationalMapGov.xml");
        if (!input.exists()){

            Assert.fail(new File(".").getAbsolutePath() + " = pwd. target file doesn't exist at " + input.getAbsolutePath());
        }
        FileInputStream fis = new FileInputStream(input);
        WMSEndpoint cap = WMSParser.parse(fis);
        fis.close();
        verify(cap);
        Assert.assertTrue(cap.getLayers().size()==1);
        Assert.assertEquals("1.3.0", cap.getWmsVersion());
        Assert.assertFalse(cap.getLayers().get(0).getStyles().isEmpty());

    }

    @Test
    public void testUSGSFile() throws Exception {

        File input = new File("./src/test/resources/usgs_getcapabilities.xml");
        if (!input.exists()){

            Assert.fail(new File(".").getAbsolutePath() + " = pwd. target file doesn't exist at " + input.getAbsolutePath());
        }
        FileInputStream fis = new FileInputStream(input);
        WMSEndpoint cap = WMSParser.parse(fis);
        fis.close();

        verify(cap);
        Assert.assertTrue(cap.getLayers().size()==1);
        Assert.assertEquals("1.3.0", cap.getWmsVersion());
    }



}