package org.osmdroid.samplefragments.tileproviders;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.osmdroid.R;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.TileStates;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

/**
 * Demo of the new tile states feature:
 * - how many tiles are currently being displayed
 * - how many tiles in which state? [U: up to date, E: expired, S: scaled, N: not found]
 *
 * @author Fabrice Fontaine
 * @since 6.1.0
 */
public class SampleTileStates extends BaseSampleFragment {

    private TextView mTextView;
    private TileStates mTileStates;
    private boolean mOk;

    @Override
    public String getSampleTitle() {
        return "Tile States";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.map_with_locationbox, container, false);
        mMapView = root.findViewById(R.id.mapview);
        mTextView = root.findViewById(R.id.textViewCurrentLocation);
        mTileStates = mMapView.getMapOverlay().getTileStates();
        return root;
    }

    @Override
    protected void addOverlays() {
        super.addOverlays();

        final Bitmap ok = ((BitmapDrawable) getResources().getDrawable(R.drawable.baseline_done_outline_black_36)).getBitmap();
        final Bitmap ko = ((BitmapDrawable) getResources().getDrawable(R.drawable.twotone_warning_black_36)).getBitmap();
        mMapView.getOverlayManager().add(new Overlay() {
            @Override
            public void draw(Canvas c, Projection projection) {
                final Bitmap bitmap = mOk ? ok : ko;
                c.drawBitmap(bitmap, c.getWidth() / 2 - bitmap.getWidth() / 2, c.getHeight() / 2 - bitmap.getHeight() / 2, null);
            }
        });
        mMapView.getMapOverlay().getTileStates().getRunAfters().add(new Runnable() {
            @Override
            public void run() {
                mTextView.setText(mTileStates.toString());
                mOk = mTileStates.isDone() && mTileStates.getTotal() == mTileStates.getUpToDate();
            }
        });
    }
}
