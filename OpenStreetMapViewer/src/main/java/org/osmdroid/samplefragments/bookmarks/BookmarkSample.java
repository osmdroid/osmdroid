package org.osmdroid.samplefragments.bookmarks;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import org.osmdroid.R;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * created on 2/11/2018.
 * TODO it would be nice to have the ability to select an icon for the location
 *
 * @author Alex O'Ree
 */

public class BookmarkSample extends BaseSampleFragment implements LocationListener {

    private LocationManager lm;
    private BookmarkDatastore datastore = null;
    private MyLocationNewOverlay mMyLocationOverlay = null;
    private Location currentLocation = null;


    @Override
    public String getSampleTitle() {
        return "Bookmark Sample";
    }


    AlertDialog addBookmark = null;

    @Override
    public void addOverlays() {
        super.addOverlays();
        if (datastore == null)
            datastore = new BookmarkDatastore();
        //add all our bookmarks to the view
        mMapView.getOverlayManager().addAll(datastore.getBookmarksAsMarkers(mMapView));

        this.mMyLocationOverlay = new MyLocationNewOverlay(mMapView);
        mMyLocationOverlay.setEnabled(true);


        this.mMapView.getOverlays().add(mMyLocationOverlay);
        //support long press to add a bookmark

        //TODO menu item to
        MapEventsOverlay events = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {


                showDialog(p);
                return true;
            }
        });
        mMapView.getOverlayManager().add(events);

    }

    private void showDialog(GeoPoint p) {
        if (addBookmark != null)
            addBookmark.dismiss();

        //TODO prompt for user input
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View view = View.inflate(getContext(), R.layout.bookmark_add_dialog, null);
        builder.setView(view);
        final EditText lat = view.findViewById(R.id.bookmark_lat);
        lat.setText(p.getLatitude() + "");
        final EditText lon = view.findViewById(R.id.bookmark_lon);
        lon.setText(p.getLongitude() + "");
        final EditText title = view.findViewById(R.id.bookmark_title);
        final EditText description = view.findViewById(R.id.bookmark_description);

        view.findViewById(R.id.bookmark_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBookmark.dismiss();
            }
        });
        view.findViewById(R.id.bookmark_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                boolean valid = true;
                double latD = 0;
                double lonD = 0;
                //basic validate input
                try {
                    latD = Double.parseDouble(lat.getText().toString());
                } catch (Exception ex) {
                    valid = false;
                }
                try {
                    lonD = Double.parseDouble(lon.getText().toString());
                } catch (Exception ex) {
                    valid = false;
                }

                if (!mMapView.getTileSystem().isValidLatitude(latD))
                    valid = false;
                if (!mMapView.getTileSystem().isValidLongitude(lonD))
                    valid = false;

                if (valid) {
                    Marker m = new Marker(mMapView);
                    m.setId(UUID.randomUUID().toString());
                    m.setTitle(title.getText().toString());
                    m.setSubDescription(description.getText().toString());

                    m.setPosition(new GeoPoint(latD, lonD));
                    m.setSnippet(m.getPosition().toDoubleString());
                    datastore.addBookmark(m);
                    mMapView.getOverlayManager().add(m);
                    mMapView.invalidate();
                }
                addBookmark.dismiss();
            }
        });

        addBookmark = builder.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            lm.removeUpdates(this);
        } catch (Exception ex) {
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            //this fails on AVD 19s, even with the appcompat check, says no provided named gps is available
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0l, 0f, this);
        } catch (Exception ex) {
        }

        try {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0l, 0f, this);
        } catch (Exception ex) {
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (datastore != null)
            datastore.close();
        datastore = null;
        if (addBookmark != null)
            addBookmark.dismiss();
        addBookmark = null;
    }


    private static final int MENU_BOOKMARK_MY_LOCATION = Menu.FIRST;
    private static final int MENU_BOOKMARK_IMPORT = MENU_BOOKMARK_MY_LOCATION + 1;
    private static final int MENU_BOOKMARK_EXPORT = MENU_BOOKMARK_IMPORT + 1;
    private static int MENU_LAST_ID = Menu.FIRST;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.add(0, MENU_BOOKMARK_MY_LOCATION, Menu.NONE, "Bookmark Current Location").setCheckable(false);
        MENU_LAST_ID++;
        menu.add(0, MENU_BOOKMARK_IMPORT, Menu.NONE, "Import from CSV").setCheckable(false);
        MENU_LAST_ID++;
        menu.add(0, MENU_BOOKMARK_EXPORT, Menu.NONE, "Export to CSV").setCheckable(false);
        MENU_LAST_ID++;
        try {
            mMapView.getOverlayManager().onCreateOptionsMenu(menu, MENU_BOOKMARK_MY_LOCATION + 1, mMapView);
        } catch (NullPointerException npe) {
            //can happen during CI tests and very rapid fragment switching
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        try {

            mMapView.getOverlayManager().onPrepareOptionsMenu(menu, MENU_LAST_ID, mMapView);
        } catch (NullPointerException npe) {
            //can happen during CI tests and very rapid fragment switching
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_BOOKMARK_MY_LOCATION) {
            //TODO
            if (currentLocation != null) {
                GeoPoint pt = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                showDialog(pt);
                return true;
            }

        } else if (item.getItemId() == MENU_BOOKMARK_IMPORT) {
            //TODO
            showFilePicker();
            return true;

        } else if (item.getItemId() == MENU_BOOKMARK_EXPORT) {
            //TODO
            showFileExportPicker();
            return true;

        } else if (mMapView.getOverlayManager().onOptionsItemSelected(item, MENU_LAST_ID, mMapView)) {
            return true;
        }
        return false;
    }


    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        //mMyLocationOverlay.setLocation(new GeoPoint(location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void showFileExportPicker() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.DIR_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);

        FilePickerDialog dialog = new FilePickerDialog(getContext(), properties);
        dialog.setTitle("Save CSV File");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(final String[] files) {
                //files is the array of the paths of files selected by the Application User.
                if (files.length == 1) {

                    //now prompt for a file name
                    AlertDialog.Builder builder = new AlertDialog.Builder(BookmarkSample.this.getContext());
                    builder.setTitle("Enter file name (.csv)");

                    // Set up the input
                    final EditText input = new EditText(BookmarkSample.this.getContext());
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                    input.setLines(1);
                    input.setText("export.csv");

                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //save the file here.
                            if (input.getText() == null)
                                return;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    String file = input.getText().toString();
                                    if (!file.toLowerCase().endsWith(".csv")) {
                                        file = file + ".csv";
                                    }
                                    exportToCsv(new File(files[0] + File.separator + file));
                                }
                            }).start();


                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();


                }
            }

        });
        dialog.show();
    }

    private void showFilePicker() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);

        Set<String> registeredExtensions = ArchiveFileFactory.getRegisteredExtensions();

        registeredExtensions.add("csv");


        String[] ret = new String[registeredExtensions.size()];
        ret = registeredExtensions.toArray(ret);
        properties.extensions = ret;

        FilePickerDialog dialog = new FilePickerDialog(getContext(), properties);
        dialog.setTitle("Select a CSV File");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(final String[] files) {
                //files is the array of the paths of files selected by the Application User.
                if (files.length == 1)
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            importFromCsv(new File(files[0]));
                        }
                    }).start();

            }

        });
        dialog.show();
    }

    private boolean exportStatus = true;

    /**
     * call me from a background thread
     */
    private void exportToCsv(File exportFile) {
        FileWriter fileWriter = null;
        exportStatus = true;
        try {
            fileWriter = new FileWriter(exportFile);
            CSVWriter writer = new CSVWriter(fileWriter);
            List<Marker> markers = datastore.getBookmarksAsMarkers(getmMapView());
            String[] headers = new String[]{"Latitude", "Longitude", "Description", "Title"};
            writer.writeNext(headers);
            for (Marker m : markers) {
                String[] items = new String[4];
                items[0] = m.getPosition().getLatitude() + "";
                items[1] = m.getPosition().getLongitude() + "";
                items[2] = m.getSubDescription();
                items[3] = m.getTitle();
                writer.writeNext(items);
            }
        } catch (Exception ex) {
            exportStatus = false;
            ex.printStackTrace();
        } finally {
            if (fileWriter != null)
                try {
                    fileWriter.close();
                } catch (Exception ex) {
                }
        }
        final Activity act = getActivity();
        if (act != null) {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (exportStatus) {
                        Toast.makeText(act, "Export Complete", Toast.LENGTH_LONG).show();
                    } else
                        Toast.makeText(act, "Export Failed", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * call me from a background thread
     */
    private void importFromCsv(File importFile) {

        final AtomicInteger imported = new AtomicInteger();
        final AtomicInteger failed = new AtomicInteger();
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(importFile);
            CSVReader reader = new CSVReader(fileReader);
            String[] nextLine = reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                try {
                    String lat = nextLine[0];
                    String lon = nextLine[1];
                    String description = nextLine[2];
                    String title = nextLine[3];
                    Marker m = new Marker(getmMapView());
                    m.setTitle(title);
                    m.setSubDescription(description);
                    m.setPosition(new GeoPoint(Double.parseDouble(lat), Double.parseDouble(lon)));
                    datastore.addBookmark(m);
                    getmMapView().getOverlayManager().add(m);
                    imported.getAndIncrement();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    failed.getAndIncrement();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (fileReader != null)
                try {
                    fileReader.close();
                } catch (Exception ex) {
                }
        }

        final Activity act = getActivity();
        if (act != null) {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(act, "Import Complete: " + imported.get() + "/" + failed.get() + "(imported/failed)", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
