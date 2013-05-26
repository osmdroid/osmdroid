// Created by plusminus on 00:23:14 - 03.10.2008
package org.osmdroid;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.osmdroid.http.HttpClientFactory;
import org.osmdroid.http.IHttpClientFactory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * Default map view activity.
 *
 * @author Manuel Stahl
 *
 */
public class MapActivity extends FragmentActivity
{

    private static final int DIALOG_ABOUT_ID = 1;

    // ===========================================================
    // Constructors
    // ===========================================================
    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.main);

        // FrameLayout mapContainer = (FrameLayout) findViewById(R.id.map_container);
        // RelativeLayout parentContainer = (RelativeLayout) findViewById(R.id.parent_container);
        FragmentManager fm = this.getSupportFragmentManager();

        MapFragment mapFragment = new MapFragment();

        fm.beginTransaction().add(R.id.map_container, mapFragment).commit();

		HttpClientFactory.setInstance(new IHttpClientFactory() {
			@Override
			public HttpClient createHttpClient() {
				return new DefaultHttpClient();
			}
		});
    }

    @Override
    protected Dialog onCreateDialog(final int id)
    {
        Dialog dialog;

        switch (id) {
            case DIALOG_ABOUT_ID:
                return new AlertDialog.Builder(MapActivity.this).setIcon(R.drawable.icon)
                        .setTitle(R.string.app_name).setMessage(R.string.about_message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int whichButton)
                            {
                                //
                            }
                        }).create();

            default:
                dialog = null;
                break;
        }
        return dialog;
    }
}
