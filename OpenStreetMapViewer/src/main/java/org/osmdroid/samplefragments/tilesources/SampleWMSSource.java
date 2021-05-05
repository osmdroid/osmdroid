package org.osmdroid.samplefragments.tilesources;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import org.osmdroid.R;
import org.osmdroid.samplefragments.data.SampleGridlines;
import org.osmdroid.wms.WMSEndpoint;
import org.osmdroid.wms.WMSLayer;
import org.osmdroid.wms.WMSParser;
import org.osmdroid.wms.WMSTileSource;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A simple demo work for working with WMS endpoints. Tested and functional with geo server
 * and
 * created on 8/20/2017.
 *
 * @author Alex O'Ree
 * @see WMSLayer
 * @see WMSParser
 * @see WMSEndpoint
 * @since 5.6.5
 */

public class SampleWMSSource extends SampleGridlines {
    AlertDialog show = null;
    AlertDialog layerPicker = null;
    AlertDialog alertDialog = null;
    MenuItem switchMenu = null;
    //this model represents our WMS server, it's "capabilities"
    WMSEndpoint cap;

    @Override
    public String getSampleTitle() {

        return "WMS Source";
    }

    protected String getDefaultUrl() {
        //"http://192.168.1.1:8080/geoserver/ows?service=wms&version=1.1.1&request=GetCapabilities"
        return "http://localhost:8080/geoserver/ows?service=wms&version=1.1.1&request=GetCapabilities";
    }

    @Override
    public void addOverlays() {
        super.addOverlays();
        // prompt for a WMS server
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        final EditText edittext = new EditText(getContext());
        edittext.setText(getDefaultUrl());
        alert.setMessage("Enter WMS Server Location");
        alert.setTitle("WMS Demo");

        alert.setView(edittext);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                String YouEditTextValue = edittext.getText().toString();

                downloadAndParse(YouEditTextValue);
                show.dismiss();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
                show.dismiss();
            }
        });

        show = alert.show();

    }

    private void downloadAndParse(final String youEditTextValue) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean ok = false;
                Exception root = null;
                try {
                    HttpURLConnection c = null;
                    InputStream is = null;
                    try {
                        c = (HttpURLConnection) new URL(youEditTextValue).openConnection();
                        is = c.getInputStream();
                        cap = WMSParser.parse(is);
                        ok = true;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        root = ex;
                    } finally {
                        if (is != null) try {
                            is.close();
                        } catch (Exception ex) {
                        }
                        if (c != null)
                            try {
                                c.disconnect();
                            } catch (Exception ex) {
                            }
                    }


                } catch (Exception ex) {
                    root = ex;
                    ex.printStackTrace();
                }

                if (ok) {
                    promptUserForLayerSelection();
                } else {

                    showErrorMessage(root);
                }
            }


        }).start();
    }

    private void showErrorMessage(final Exception root) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage("There was an error communicating with the server: \n" + root.getMessage());
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });
    }

    private void promptUserForLayerSelection() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
                builderSingle.setIcon(R.drawable.icon);
                builderSingle.setTitle("Select A Layer");

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice);
                for (int i = 0; i < cap.getLayers().size(); i++) {
                    arrayAdapter.add(cap.getLayers().get(i).getTitle());
                }


                builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        layerPicker.dismiss();
                    }
                });

                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);
                        for (WMSLayer layer : cap.getLayers()) {
                            if (strName.equals(layer.getTitle())) {
                                WMSTileSource source = WMSTileSource.createFrom(cap, layer);
                                if (layer.getBbox() != null) {
                                    //center map on this location
                                    try {
                                        //double centerLat = (Double.parseDouble(layer.getBbox().getMaxy()) + Double.parseDouble(layer.getBbox().getMiny())) / 2;
                                        //double centerLon = (Double.parseDouble(layer.getBbox().getMaxx()) + Double.parseDouble(layer.getBbox().getMinx())) / 2;
                                        //mMapView.getController().animateTo(new GeoPoint(centerLat, centerLon));

                                        mMapView.zoomToBoundingBox(layer.getBbox(), true);
                                        mMapView.zoomToBoundingBox(layer.getBbox(), true);
                                        mMapView.zoomToBoundingBox(layer.getBbox(), true);

                                    } catch (java.lang.Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }

                                mMapView.setTileSource(source);

                                break;
                            }
                        }
                        layerPicker.dismiss();

                    }
                });
                layerPicker = builderSingle.show();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        if (show != null && show.isShowing()) {
            show.dismiss();
        }
        if (layerPicker != null && layerPicker.isShowing()) {
            layerPicker.dismiss();
        }
    }
    /* android context menu */
    /* android context menu */
    /* android context menu */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        switchMenu = menu.add("Switch WMS Layer");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (switchMenu == item) {
            if (layerPicker != null) {
                layerPicker.show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /* END android context menu */
    /* END android context menu */
    /* END android context menu */

}
