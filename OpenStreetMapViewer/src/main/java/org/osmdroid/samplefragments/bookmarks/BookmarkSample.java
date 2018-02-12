package org.osmdroid.samplefragments.bookmarks;


import android.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import org.osmdroid.R;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.UUID;

/**
 * created on 2/11/2018.
 *
 * @author Alex O'Ree
 */

public class BookmarkSample extends BaseSampleFragment {

    BookmarkDatastore datastore = new BookmarkDatastore();

    @Override
    public String getSampleTitle() {
        return "Bookmark Sample";
    }


    AlertDialog addBookmark=null;
    @Override
    public void addOverlays() {
        super.addOverlays();
        //add all our bookmarks to the view
        mMapView.getOverlayManager().addAll(datastore.getBookmarksAsMarkers(mMapView));

        //support long press to add a bookmark

        //TODO menu item to
        MapEventsOverlay events = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                //TODO prompt for user input
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                View view = View.inflate(getContext(), R.layout.bookmark_add_dialog, null);
                builder.setView(view);
                final EditText lat = (EditText) view.findViewById(R.id.bookmark_lat);
                lat.setText(p.getLatitude() + "");
                final EditText lon = (EditText) view.findViewById(R.id.bookmark_lon);
                lon.setText(p.getLongitude() + "");
                final EditText title = (EditText) view.findViewById(R.id.bookmark_title);
                final EditText description = (EditText) view.findViewById(R.id.bookmark_description);

                view.findViewById(R.id.bookmark_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addBookmark.dismiss();
                    }
                });
                view.findViewById(R.id.bookmark_ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        Marker m = new Marker(mMapView);
                        m.setId(UUID.randomUUID().toString());
                        m.setTitle(title.getText().toString());
                        m.setSubDescription(description.getText().toString());
                        //TODO validate input
                        m.setPosition(new GeoPoint(
                            Double.parseDouble(lat.getText().toString()),
                            Double.parseDouble(lon.getText().toString())

                        ));
                        m.setSnippet(m.getPosition().toDoubleString());
                        datastore.addBookmark(m);
                        mMapView.getOverlayManager().add(m);
                        mMapView.invalidate();

                        addBookmark.dismiss();
                    }
                });

                addBookmark = builder.show();
                return true;
            }
        });
        mMapView.getOverlayManager().add(events);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        datastore.close();
        addBookmark.dismiss();
    }
}
