package org.osmdroid.samplefragments.drawing;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.osmdroid.R;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapView;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import static org.osmdroid.samplefragments.events.SampleMapEventListener.df;

/**
 * A simple sample to plot markers with a long press. It's a bit of noise this in the class
 * that is used to help the osmdroid devs troubleshoot things.
 * <p>
 * Map replication is ON for this sample (only viewable for numerically lower zoom levels (higher altitude))
 * <p>
 * created on 11/19/2017.
 *
 * @author Alex O'Ree
 * @since 6.0.0
 */

public class PressToPlot extends BaseSampleFragment implements View.OnClickListener, View.OnLongClickListener {
    ImageButton painting, panning;
    TextView textViewCurrentLocation;

    ImageButton btnRotateLeft, btnRotateRight;

    @Override
    public String getSampleTitle() {
        return "Long Press to Plot Marker";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_drawlines, null);
        btnRotateLeft = v.findViewById(R.id.btnRotateLeft);
        btnRotateRight = v.findViewById(R.id.btnRotateRight);
        btnRotateRight.setOnClickListener(this);
        btnRotateLeft.setOnClickListener(this);
        textViewCurrentLocation = v.findViewById(R.id.textViewCurrentLocation);
        mMapView = v.findViewById(R.id.mapview);
        mMapView.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                Log.i(IMapView.LOGTAG, System.currentTimeMillis() + " onScroll " + event.getX() + "," + event.getY());
                //Toast.makeText(getActivity(), "onScroll", Toast.LENGTH_SHORT).show();
                updateInfo();
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                Log.i(IMapView.LOGTAG, System.currentTimeMillis() + " onZoom " + event.getZoomLevel());
                updateInfo();
                return true;
            }
        });

        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(mMapView);
        mRotationGestureOverlay.setEnabled(true);
        mMapView.setMultiTouchControls(true);
        mMapView.getOverlayManager().add(mRotationGestureOverlay);
        mMapView.setOnLongClickListener(this);
        panning = v.findViewById(R.id.enablePanning);
        panning.setVisibility(View.GONE);

        painting = v.findViewById(R.id.enablePainting);
        painting.setVisibility(View.GONE);


        IconPlottingOverlay plotter = new IconPlottingOverlay(this.getResources().getDrawable(R.drawable.ic_follow_me_on));
        mMapView.getOverlayManager().add(plotter);

        return v;

    }

    private void updateInfo() {
        IGeoPoint mapCenter = mMapView.getMapCenter();
        textViewCurrentLocation.setText(df.format(mapCenter.getLatitude()) + "," +
                df.format(mapCenter.getLongitude())
                + ",zoom=" + mMapView.getZoomLevelDouble() + ",angle=" + mMapView.getMapOrientation());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnRotateLeft: {
                float angle = mMapView.getMapOrientation() + 10;
                if (angle > 360)
                    angle = 360 - angle;
                mMapView.setMapOrientation(angle);
                updateInfo();
            }
            break;
            case R.id.btnRotateRight: {
                float angle = mMapView.getMapOrientation() - 10;
                if (angle < 0)
                    angle += 360f;
                mMapView.setMapOrientation(angle);
                updateInfo();
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {

        return true;
    }
}
