package org.osmdroid;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import org.osmdroid.bugtestfragments.Bug1783MyLocationOverlayNPE;
import org.osmdroid.model.IBaseActivity;

public class Bug1783Activity extends FragmentActivity implements IBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bug1783);
        Button button = findViewById(R.id.bug1782Button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                final DialogFragment dialog = new Bug1783MyLocationOverlayNPE();

                dialog.show(Bug1783Activity.this.getSupportFragmentManager(), "tag");
               /* try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
                Bug1783Activity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();

                    }
                });*/



            }
        });
    }

    @Override
    public String getActivityTitle() {
        return "My location overview dialog fragment";
    }
}