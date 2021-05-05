package org.osmdroid.samplefragments.drawing;

import android.graphics.Color;
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
 * created on 1/13/2017.
 *
 * @author Alex O'Ree
 */

public class SampleDrawPolyline extends BaseSampleFragment implements View.OnClickListener {

    ImageButton painting, panning;
    TextView textViewCurrentLocation;
    CustomPaintingSurface paint;
    ImageButton btnRotateLeft, btnRotateRight;

    @Override
    public String getSampleTitle() {
        return "Draw a polyline on screen";
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
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(mMapView);
        mRotationGestureOverlay.setEnabled(true);
        mMapView.setMultiTouchControls(true);
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
        mMapView.getOverlayManager().add(mRotationGestureOverlay);
        panning = v.findViewById(R.id.enablePanning);
        panning.setOnClickListener(this);
        panning.setBackgroundColor(Color.BLACK);
        painting = v.findViewById(R.id.enablePainting);
        painting.setOnClickListener(this);
        paint = v.findViewById(R.id.paintingSurface);
        paint.init(mMapView);
        paint.setMode(CustomPaintingSurface.Mode.Polyline);
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
            case R.id.enablePanning:
                paint.setVisibility(View.GONE);
                panning.setBackgroundColor(Color.BLACK);
                painting.setBackgroundColor(Color.TRANSPARENT);
                break;
            case R.id.enablePainting:
                paint.setVisibility(View.VISIBLE);
                painting.setBackgroundColor(Color.BLACK);
                panning.setBackgroundColor(Color.TRANSPARENT);
                break;
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
}
