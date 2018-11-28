package org.osmdroid;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * created on 1/14/2017.
 *
 * @author Alex O'Ree
 */

public class LicenseActivity extends Activity implements AdapterView.OnItemSelectedListener {
    TextView license;
    String[] values = new String[]{
        "osmdroid", "geopackage",
        "mapsforge", "ACRA", "leakcanary", "ormlite", "pngj"
    };


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        Spinner spinner = findViewById(R.id.license_module_spinner);
        ArrayAdapter<String> array = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, values);
        array.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(array);
        license = findViewById(R.id.license_body);
        spinner.setOnItemSelectedListener(this);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                license.setText(R.string.license_osmdroid);
                break;
            case 1:
                license.setText(R.string.license_geopackage);
                break;
            case 2:
                license.setText(R.string.license_mapsforge);
                break;
            case 3:
                license.setText(R.string.license_acra);
                break;
            case 4:
                license.setText(R.string.license_leakcanary);
                break;
            case 5:
                license.setText(R.string.license_ormlite);
                break;
            case 6:
                license.setText(R.string.license_pngj);
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
