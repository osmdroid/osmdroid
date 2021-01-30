package org.osmdroid.samplefragments.cache;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;

/**
 * Created by alex on 5/29/16.
 */
public class SampleCacheDownloaderCustomUI extends BaseSampleFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, TextWatcher, CacheManager.CacheManagerCallback {
    @Override
    public String getSampleTitle() {
        return "Cache Manager with custom UI";
    }

    ProgressDialog progressBar;

    Button btnCache, executeJob;
    SeekBar zoom_min;
    SeekBar zoom_max;
    EditText cache_north, cache_south, cache_east, cache_west;
    TextView cache_estimate;
    CacheManager mgr;
    AlertDialog downloadPrompt = null;
    CacheManager.CacheManagerTask downloadingTask = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.sample_cachemgr, container, false);

        //prevent the action bar/toolbar menu in order to prevent tile source changes.
        //if this is enabled, playstore users could actually download large volumes of tiles
        //from tile sources that do not allow it., causing our app to get banned, which would be
        //bad
        setHasOptionsMenu(false);

        mMapView = new MapView(getActivity());
        mMapView.setTileSource(TileSourceFactory.USGS_SAT);
        ((LinearLayout) root.findViewById(R.id.mapview)).addView(mMapView);
        btnCache = root.findViewById(R.id.btnCache);
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


    private void showCacheManagerDialog() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());


        // set title
        alertDialogBuilder.setTitle(R.string.cache_manager);
        //.setMessage(R.string.cache_manager_description);

        // set dialog message
        alertDialogBuilder.setItems(new CharSequence[]{
                        getResources().getString(R.string.cache_current_size),
                        getResources().getString(R.string.cache_download),
                        getResources().getString(R.string.cancelall),
                        getResources().getString(R.string.showpendingjobs),
                        getResources().getString(R.string.close)
                }, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                showCurrentCacheInfo();
                                break;
                            case 1:
                                downloadJobAlert();
                                break;
                            case 2:
                                mgr.cancelAllJobs();
                                Toast.makeText(getActivity(), "Jobs Canceled", Toast.LENGTH_LONG).show();
                                break;
                            case 3:
                                Toast.makeText(getActivity(), "Pending Jobs: " + mgr.getPendingJobs(), Toast.LENGTH_LONG).show();
                                break;
                        }
                        dialog.dismiss();
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

        BoundingBox boundingBox = mMapView.getBoundingBox();
        zoom_max = view.findViewById(R.id.slider_zoom_max);
        zoom_max.setMax((int) mMapView.getMaxZoomLevel());
        zoom_max.setOnSeekBarChangeListener(SampleCacheDownloaderCustomUI.this);


        zoom_min = view.findViewById(R.id.slider_zoom_min);
        zoom_min.setMax((int) mMapView.getMaxZoomLevel());
        zoom_min.setProgress((int) mMapView.getMinZoomLevel());
        zoom_min.setOnSeekBarChangeListener(SampleCacheDownloaderCustomUI.this);
        cache_east = view.findViewById(R.id.cache_east);
        cache_east.setText(boundingBox.getLonEast() + "");
        cache_north = view.findViewById(R.id.cache_north);
        cache_north.setText(boundingBox.getLatNorth() + "");
        cache_south = view.findViewById(R.id.cache_south);
        cache_south.setText(boundingBox.getLatSouth() + "");
        cache_west = view.findViewById(R.id.cache_west);
        cache_west.setText(boundingBox.getLonWest() + "");
        cache_estimate = view.findViewById(R.id.cache_estimate);

        //change listeners for both validation and to trigger the download estimation
        cache_east.addTextChangedListener(this);
        cache_north.addTextChangedListener(this);
        cache_south.addTextChangedListener(this);
        cache_west.addTextChangedListener(this);
        executeJob = view.findViewById(R.id.executeJob);
        executeJob.setOnClickListener(this);
        builder.setView(view);
        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cache_east = null;
                cache_south = null;
                cache_estimate = null;
                cache_north = null;
                cache_west = null;
                executeJob = null;
                zoom_min = null;
                zoom_max = null;
            }
        });
        downloadPrompt = builder.create();
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
                BoundingBox bb = new BoundingBox(n, e, s, w);
                int tilecount = mgr.possibleTilesInArea(bb, zoommin, zoommax);
                cache_estimate.setText(tilecount + " tiles");
                if (startJob) {
                    if (downloadPrompt != null) {
                        downloadPrompt.dismiss();
                        downloadPrompt = null;
                    }


                    // prepare for a progress bar dialog ( do this first! )
                    progressBar = new ProgressDialog(SampleCacheDownloaderCustomUI.this.getActivity());
                    progressBar.setCancelable(true);
                    progressBar.setMessage("Downloading ...");
                    progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressBar.setProgress(0);
                    progressBar.setCancelable(true);
                    progressBar.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            //cancel the job wit the dialog is closed
                            downloadingTask.cancel(true);
                            System.out.println("Pending jobs " + mgr.getPendingJobs());
                        }
                    });
                    //this triggers the download
                    downloadingTask = mgr.downloadAreaAsyncNoUI(getActivity(), bb, zoommin, zoommax, SampleCacheDownloaderCustomUI.this);


                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showCurrentCacheInfo() {
        Toast.makeText(getActivity(), "Calculating...", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        getActivity());


                // set title
                alertDialogBuilder.setTitle(R.string.cache_manager)
                        .setMessage("Cache Capacity (bytes): " + mgr.cacheCapacity() + "\n" +
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


    //cache manager callback
    @Override
    public void onTaskComplete() {
        progressBar.dismiss();
        progressBar = null;
        Toast.makeText(getActivity(), "Download complete!", Toast.LENGTH_LONG).show();
    }

    //cache manager callback
    @Override
    public void updateProgress(int progress, int currentZoomLevel, int zoomMin, int zoomMax) {
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
    }


    //cache manager callback
    @Override
    public void downloadStarted() {

        if (progressBar != null) {
            progressBar.show();
        }
    }

    //cache manager callback
    @Override
    public void setPossibleTilesInArea(int total) {
        if (progressBar != null) {
            progressBar.setMax(total);
        }
    }

    @Override
    public void onTaskFailed(int errors) {
        if (progressBar != null)
            progressBar.dismiss();
        progressBar = null;
        Toast.makeText(getActivity(), "Download complete with " + errors + " errors", Toast.LENGTH_LONG).show();

    }
}
