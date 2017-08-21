package org.osmdroid.wms;


import android.app.Activity;
import android.os.Bundle;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*Parser p = new Parser();
        try {
            ObjectMapper om = new ObjectMapper();
            om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);
            WMTMSCapabilities parse = p.parse(getAssets().open("geoserver_getcapabilities_1.3.0.xml"));
            System.out.println("WMS = " + om.writeValueAsString(parse));
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}
