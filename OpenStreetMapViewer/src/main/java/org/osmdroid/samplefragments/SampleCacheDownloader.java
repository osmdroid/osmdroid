package org.osmdroid.samplefragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.renderscript.Double2;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import org.osmdroid.R;
import org.osmdroid.bonuspack.cachemanager.CacheManager;
import org.osmdroid.constants.OpenStreetMapConstants;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;

/**
 * Sample for using the cache manager to download tiles on screen
 * Created by alex on 2/21/16.
 */
public class SampleCacheDownloader extends BaseSampleFragment implements View.OnClickListener, OnSeekBarChangeListener, TextWatcher {
    @Override
    public String getSampleTitle() {
        return "Cache Manager";
    }

    Button btnCache,executeJob;
    SeekBar zoom_min;
    SeekBar zoom_max;
    EditText cache_north, cache_south, cache_east,cache_west;
    TextView cache_estimate;
    CacheManager mgr;
    AlertDialog downloadPrompt=null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.sample_cachemgr, container,false);

        mMapView = (MapView) root.findViewById(R.id.mapview);
        btnCache = (Button) root.findViewById(R.id.btnCache);
        btnCache.setOnClickListener(this);
        mgr = new CacheManager(mMapView);
        return root;
    }

    @Override
    public void addOverlays() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.executeJob:
                updateEstimate(true);
                break;

            case R.id.btnCache:
                showCacheManagerDialog();
                break;

        }
    }


    private void showCacheManagerDialog(){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());


        // set title
        alertDialogBuilder.setTitle(R.string.cache_manager);
                //.setMessage(R.string.cache_manager_description);

        // set dialog message
        alertDialogBuilder.setItems(new CharSequence[]{
                getResources().getString(R.string.cache_current_size),
                getResources().getString(R.string.cache_download),
                getResources().getString(R.string.cancel)
                }, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                showCurrentCacheInfo();
                                break;
                            case 1:
                                downloadJobAlert();
                            default:
                                dialog.dismiss();
                                break;
                        }
                    }
                }
        );


        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();


        //mgr.possibleTilesInArea(mMapView.getBoundingBox(), 0, 18);
       // mgr.
    }

    private void downloadJobAlert() {
        //prompt for input params
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = View.inflate(getActivity(), R.layout.sample_cachemgr_input, null);

        BoundingBoxE6 boundingBox = mMapView.getBoundingBox();
        zoom_max=(SeekBar) view.findViewById(R.id.slider_zoom_max);
        zoom_max.setMax(mMapView.getMaxZoomLevel());
        zoom_max.setOnSeekBarChangeListener(SampleCacheDownloader.this);


        zoom_min=(SeekBar) view.findViewById(R.id.slider_zoom_min);
        zoom_min.setMax(mMapView.getMaxZoomLevel());
        zoom_min.setProgress(mMapView.getMinZoomLevel());
        zoom_min.setOnSeekBarChangeListener(SampleCacheDownloader.this);
        cache_east= (EditText) view.findViewById(R.id.cache_east);
        cache_east.setText(boundingBox.getLonEastE6() /1E6 +"");
        cache_north= (EditText) view.findViewById(R.id.cache_north);
        cache_north.setText(boundingBox.getLatNorthE6() /1E6 +"");
        cache_south= (EditText) view.findViewById(R.id.cache_south);
        cache_south.setText(boundingBox.getLatSouthE6() /1E6 +"");
        cache_west= (EditText) view.findViewById(R.id.cache_west);
        cache_west.setText(boundingBox.getLonWestE6() /1E6 +"");
        cache_estimate = (TextView) view.findViewById(R.id.cache_estimate);

        //change listeners for both validation and to trigger the download estimation
        cache_east.addTextChangedListener((TextWatcher) this);
        cache_north.addTextChangedListener((TextWatcher) this);
        cache_south.addTextChangedListener((TextWatcher) this);
        cache_west.addTextChangedListener((TextWatcher) this);
        executeJob= (Button) view.findViewById(R.id.executeJob);
        executeJob.setOnClickListener(this);
        builder.setView(view);
        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cache_east=null;
                cache_south=null;
                cache_estimate=null;
                cache_north=null;
                cache_west=null;
                executeJob=null;
                zoom_min=null;
                zoom_max=null;
            }
        });
        downloadPrompt=builder.create();
        downloadPrompt.show();


    }

    /**
     * if true, start the job
     * if false, just update the dialog box
     */
    private void updateEstimate(boolean startJob) {
        try {
            if (cache_east != null &&
                    cache_west != null &&
                    cache_north != null &&
                    cache_south != null &&
                    zoom_max != null &&
                    zoom_min != null) {
                double n = Double.parseDouble(cache_north.getText().toString());
                double s = Double.parseDouble(cache_south.getText().toString());
                double e = Double.parseDouble(cache_east.getText().toString());
                double w = Double.parseDouble(cache_west.getText().toString());

                int zoommin = zoom_min.getProgress();
                int zoommax = zoom_max.getProgress();
                //nesw
                BoundingBoxE6 bb= new BoundingBoxE6(n, e, s, w);
                int tilecount = mgr.possibleTilesInArea(bb, zoommin, zoommax);
                cache_estimate.setText(tilecount + " tiles");
                if (startJob)
                {
                    if ( downloadPrompt!=null) {
                        downloadPrompt.dismiss();
                        downloadPrompt=null;
                    }
                    mgr.downloadAreaAsync(getActivity(), bb, zoommin, zoommax, new CacheManager.CacheManagerCallback() {
                        @Override
                        public void onTaskComplete() {
                            Toast.makeText(getActivity(), "Download complete!", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void showCurrentCacheInfo() {
        Toast.makeText(getActivity(), "Calculating..." ,Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        getActivity());


                // set title
                alertDialogBuilder.setTitle(R.string.cache_manager)
                        .setMessage("Cache Capacity (bytes): " + mgr.cacheCapacity() + "\n"+
                                "Cache Usage (bytes): " + mgr.currentCacheUsage());

                // set dialog message
                alertDialogBuilder.setItems(new CharSequence[]{

                                getResources().getString(R.string.cancel)
                        }, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }
                );




                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // show it
                        // create alert dialog
                        final AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                });

            }
        }).start();



    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateEstimate(false);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        updateEstimate(false);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
