// Created by plusminus on 00:23:14 - 03.10.2008
package org.osmdroid;

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
	private static final String MAP_FRAGMENT_TAG = "org.osmdroid.MAP_FRAGMENT_TAG";

    // ===========================================================
    // Constructors
    // ===========================================================
    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.main);

        FragmentManager fm = this.getSupportFragmentManager();

		if (fm.findFragmentByTag(MAP_FRAGMENT_TAG) == null) {
			MapFragment mapFragment = MapFragment.newInstance();
			fm.beginTransaction().add(R.id.map_container, mapFragment, MAP_FRAGMENT_TAG).commit();
		}
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
