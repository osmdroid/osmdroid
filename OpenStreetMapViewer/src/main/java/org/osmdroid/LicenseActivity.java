package org.osmdroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class LicenseActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    TextView license;
    String[] values = new String[]{
            "osmdroid", "geopackage",
            "mapsforge", "ACRA", "leakcanary", "ormlite", "pngj"
    };

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Spinner spinner = findViewById(R.id.license_module_spinner);
        ArrayAdapter<String> array = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
        array.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(array);
        license = findViewById(R.id.license_body);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
