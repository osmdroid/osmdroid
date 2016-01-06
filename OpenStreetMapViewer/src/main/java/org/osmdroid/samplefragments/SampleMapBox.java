package org.osmdroid.samplefragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.osmdroid.R;
import org.osmdroid.tileprovider.tilesource.MapBoxTileSource;

/**
 * Example for accessing a MapBox map source
 *
 * Created by alex on 10/18/15.
 */
public class SampleMapBox   extends BaseSampleFragment {

    @Override
    public String getSampleTitle() {
        return "MapBox";
    }

    @Override
    public void addOverlays() {

        //Since we distribute the sample app without any map box access tokens or maps, we prompt here for the user
        //to enter this information. If you're using this as a sample for your app, consider the following
        //this bit gets the key from the manifest

        /*
        MapBoxTileSource b=new MapBoxTileSource("MapBox",0,19,256, ".png");
        b.retrieveAccessToken(getContext());
        b.retrieveMapBoxMapId(getContext());
        //you can also programmatically set the token and map id here
        //b.setAccessToken("KEY");
        //b.setMapboxMapid("KEY");

        this.mMapView.setTileSource(b);
        */

        //End notes



        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptsView = li.inflate(R.layout.mapbox_prompt, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInputBoxId = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInputMapboxId);

        final EditText userInputToken = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInputMapboxAccessToken);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user inputs and set the map source
                                //this bit gets the key from the manifest

                                MapBoxTileSource b=new MapBoxTileSource("MapBox",0,19,256, ".png");
                                b.setMapboxMapid(userInputBoxId.getText().toString());
                                b.setAccessToken(userInputToken.getText().toString());
                                mMapView.setTileSource(b);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();



    }

}
