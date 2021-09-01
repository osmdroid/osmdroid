package org.osmdroid.samplefragments.data;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.R;
import org.osmdroid.data.DataRegion;
import org.osmdroid.data.DataRegionLoader;
import org.osmdroid.samplefragments.BaseSampleFragment;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.util.TileSystem;
import org.osmdroid.util.TileSystemWebMercator;
import org.osmdroid.views.Projection;
import org.osmdroid.views.drawing.MapSnapshot;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demo with the new "MapSnapshot" feature - a RecyclerView with bitmap maps of all USA states
 *
 * @author Fabrice Fontaine
 * @since 6.1.0
 */
public class SampleMapSnapshot extends BaseSampleFragment {

    private final TileSystem mTileSystem = new TileSystemWebMercator();
    private final Map<String, MapSnapshot> mMapSnapshots = new HashMap<>();
    private final Map<String, Bitmap> mBitmaps = new HashMap<>();

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private final Bitmap mDefaultBitmap;
        private final List<DataRegion> mDataSet;
        private final List<Overlay> mOverlays;

        class MyViewHolder extends RecyclerView.ViewHolder {

            private ImageView mImageView;
            private TextView mTextView;
            private ProgressBar mProgressBar;

            MyViewHolder(LinearLayout pLinearLayout) {
                super(pLinearLayout);
                mImageView = (ImageView) pLinearLayout.getChildAt(0);
                mTextView = (TextView) pLinearLayout.getChildAt(1);
                mProgressBar = (ProgressBar) pLinearLayout.getChildAt(2);

                pLinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getActivity(), mTextView.getText(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        MyAdapter(final List<DataRegion> pDataset) {
            mDataSet = pDataset;
            mDefaultBitmap = Bitmap.createBitmap(mMapSize, mMapSize, Bitmap.Config.ARGB_8888);
            mOverlays = new ArrayList<>();
            mOverlays.add(mScaleBarOverlay);
        }

        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            final ImageView imageView = new ImageView(getActivity());
            imageView.setImageBitmap(mDefaultBitmap);
            linearLayout.addView(imageView);
            linearLayout.addView(new TextView(getActivity()));
            final ProgressBar progressBar = new ProgressBar(getActivity());
            progressBar.setIndeterminate(true);
            linearLayout.addView(progressBar);
            return new MyViewHolder(linearLayout);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int pPosition) {
            final DataRegion dataRegion = mDataSet.get(pPosition);
            if (dataRegion == null) { // should never happen
                return;
            }
            final String key = dataRegion.getISO3166();
            holder.mTextView.setText(dataRegion.getName());
            final Bitmap bitmap = mBitmaps.get(key);
            if (bitmap != null) {
                holder.mImageView.setImageBitmap(bitmap);
                holder.mProgressBar.setVisibility(View.INVISIBLE);
                return;
            }
            holder.mImageView.setImageBitmap(mDefaultBitmap);
            holder.mProgressBar.setVisibility(View.VISIBLE);
            download(dataRegion);
        }

        @Override
        public int getItemCount() {
            return mDataSet.size();
        }

        private void download(final DataRegion pDataRegion) {
            final String key = pDataRegion.getISO3166();
            if (mMapSnapshots.get(key) != null) {
                return; // pending
            }
            final double zoom = mTileSystem.getBoundingBoxZoom(
                    pDataRegion.getBox(), mMapSize - 2 * mBorderSize, mMapSize - 2 * mBorderSize);
            final MapTileProviderBase mapTileProvider = new MapTileProviderBasic(getActivity());
            final MapSnapshot mapSnapshot = new MapSnapshot(new MapSnapshot.MapSnapshotable() {
                @Override
                public void callback(final MapSnapshot pMapSnapshot) {
                    if (pMapSnapshot.getStatus() != MapSnapshot.Status.CANVAS_OK) {
                        return;
                    }
                    final Bitmap bitmap = Bitmap.createBitmap(pMapSnapshot.getBitmap());
                    mBitmaps.put(key, bitmap);
                    mMapSnapshots.get(key).onDetach();
                    mMapSnapshots.remove(key);
                    if (mAdapter == null) {
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }, MapSnapshot.INCLUDE_FLAG_UPTODATE, mapTileProvider, mOverlays,
                    new Projection(zoom, mMapSize, mMapSize, pDataRegion.getBox().getCenterWithDateLine(), 0, true, true, 0, 0));
            mMapSnapshots.put(key, mapSnapshot);
            new Thread(mapSnapshot).start(); // TODO use AsyncTask, Executors instead?
        }
    }

    private RecyclerView.Adapter mAdapter;
    private ScaleBarOverlay mScaleBarOverlay;
    private int mMapSize;
    private int mBorderSize;

    @Override
    public String getSampleTitle() {
        return "MapSnapshot RecyclerView";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        mMapSize = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        mBorderSize = mMapSize / 15;

        mScaleBarOverlay = new ScaleBarOverlay(getActivity(), mMapSize, mMapSize);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setScaleBarOffset(mMapSize / 2, 10);

        final RecyclerView recyclerView = new RecyclerView(getActivity());
        recyclerView.setHasFixedSize(true);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        try {
            final DataRegionLoader dataRegionLoader = new DataRegionLoader(getActivity(), R.raw.data_region_usstates);
            mAdapter = new MyAdapter(new ArrayList<>(dataRegionLoader.getList().values()));
            recyclerView.setAdapter(mAdapter);
        } catch (Exception e) {
            // DataRegionLoader KO, not supposed to happen
        }

        return recyclerView;
    }

    @Override
    public void onDetach() {
        mAdapter = null;
        mScaleBarOverlay.onDetach(null);
        for (final String key : mMapSnapshots.keySet()) {
            final MapSnapshot mapSnapshot = mMapSnapshots.get(key);
            if (mapSnapshot != null) {
                mapSnapshot.onDetach();
            }
        }
        mMapSnapshots.clear();
        super.onDetach();
    }
}
