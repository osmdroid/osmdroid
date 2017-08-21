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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class
    , sdk = 21)
public class ParserTest{

    @Test
    public void testParserGeoserver() throws Exception {

        ParserWMS111 p = new ParserWMS111();
        File input = new File("./src/test/resources/geoserver_getcapabilities_1.1.0.xml");
        if (!input.exists()){

            Assert.fail(new File(".").getAbsolutePath() + " = pwd. target file doesn't exist at " + input.getAbsolutePath());
        }
        FileInputStream fis = new FileInputStream(input);
        WMSEndpoint cap = p.parse(fis);
        fis.close();

        Assert.assertNotNull(cap);
        Assert.assertNotNull(cap.baseurl);
        Assert.assertFalse(cap.layers.isEmpty());


    }

    @Test
    public void testUSGS() throws Exception {
        ParserWMS111 p = new  ParserWMS111();
        HttpURLConnection c = (HttpURLConnection) new URL("http://basemap.nationalmap.gov/arcgis/services/USGSTopo/MapServer/WMSServer?request=GetCapabilities&service=WMS").openConnection();
        InputStream is = c.getInputStream();
        WMSEndpoint parse = p.parse(is);
        Assert.assertNotNull(parse);

        is.close();
        c.disconnect();

    }

    @Test
    @Ignore
    public void testUSGSFile() throws Exception {

        ParserWMS111 p = new ParserWMS111();
        File input = new File("./src/test/resources/usgs_getcapabilities.xml");
        if (!input.exists()){

            Assert.fail(new File(".").getAbsolutePath() + " = pwd. target file doesn't exist at " + input.getAbsolutePath());
        }
        FileInputStream fis = new FileInputStream(input);
        WMSEndpoint cap = p.parse(fis);
        fis.close();


    }


}