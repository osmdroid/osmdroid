package org.osmdroid.sample;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsAsync;
import org.osmdroid.simplemap.R;
import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

/**
 * Bare bones osmdroid example
 * created on 2/17/2018.
 *
 * @author Alex O'Ree
 */

public class MapActivity extends android.app.Activity implements LifecycleOwner {
    private static final String TAG = "MapActivity";

    private final LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);
    private MapView mMapView = null;
    private ProgressBar mLoadingProgressBar = null;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        mLifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        //TODO check permissions
        setContentView(R.layout.activity_main);
        mMapView = findViewById(R.id.mapView_activityMain);
        mLoadingProgressBar = findViewById(R.id.progressBar_activityMain_loading);

        new MapEventsAsync(mMapView, new MapEventsAsync.MapAsyncEventListener() {
            @Nullable
            private Overlay mOverlay = null;
            @WorkerThread
            @Override
            public void onInitWorking(@NonNull final MapView mapView) {
                /*
                ...BLOCKING OPERATION(s) HERE...
                ...for example loading a big KML/GPX or many-and-many Markers...
                this.mOverlay = createOverlayFromGPXFile(...
                */
            }
            @UiThread @MainThread
            @Override
            public void onInitDone(@NonNull final MapView mapView) {
                if (this.mOverlay != null) mapView.getOverlayManager().add(this.mOverlay);
                mapView.invalidate();
            }
            @UiThread @MainThread
            @Override
            public void onLoadingProgress(@MapEventsAsync.LOADINGTYPE final int loadingType, final int percentage, @MapEventsAsync.LOADINGTYPE final int otherRemainingTypes) {
                //Log.e(TAG, "onLoadingProgress(type: " + MapEventsAsync.decodeLoadingType(loadingType) + " | " + (percentage < 0 ? "indeterminate" : percentage+"%") + " | otherRemainingTypes: " + MapEventsAsync.decodeLoadingType(otherRemainingTypes) + ")");
                final boolean cIsIndeterminate = (percentage < 0);
                final boolean cIsLoading = (cIsIndeterminate || (otherRemainingTypes != 0));
                mLoadingProgressBar.setIndeterminate(cIsIndeterminate);
                mLoadingProgressBar.setVisibility(cIsLoading ? View.VISIBLE : View.GONE);
            }
            @UiThread @MainThread
            @Override
            public void onLoadingDone(@MapEventsAsync.LOADINGTYPE final int loadingType, @MapEventsAsync.LOADINGTYPE final int otherRemainingTypes) {
                //Log.e(TAG, "onLoadingDone(type: " + MapEventsAsync.decodeLoadingType(loadingType) + " | otherRemainingTypes: " + MapEventsAsync.decodeLoadingType(otherRemainingTypes) + ")");
                final boolean cIsLoading = (otherRemainingTypes != 0);
                mLoadingProgressBar.setVisibility(cIsLoading ? View.VISIBLE : View.GONE);
            }
            @UiThread @MainThread
            @Override
            public void onFirstLayout(final int left, final int top, final int right, final int bottom) {
                //Log.e(TAG, "onFirstLayout(" + left + ", " + top + ", " + right + ", " + bottom + ")");
            }
        }, null).addCallbackWorkingWithResult(MapEventsAsync.CALLBACKWORKINGWITHRESULTTYPE_ONSCROLL, new MapEventsAsync.CallbackWorkingWithResult<Void>() {
            @WorkerThread
            @Override
            public Void onWorking(@NonNull final MapView mapView, @Nullable final Object... args) {
                return null;
            }
            @UiThread @MainThread
            @Override
            public void onDone(@NonNull final MapView mapView, @Nullable final Void workingResult) { /*nothing*/ }
        }).addCallbackWorkingWithResult(MapEventsAsync.CALLBACKWORKINGWITHRESULTTYPE_ONZOOM, new MapEventsAsync.CallbackWorkingWithResult<Void>() {
            @WorkerThread
            @Override
            public Void onWorking(@NonNull final MapView mapView, @Nullable final Object... args) {
                return null;
            }
            @UiThread @MainThread
            @Override
            public void onDone(@NonNull final MapView mapView, @Nullable final Void workingResult) { /*nothing*/ }
        }).addTileLoadListener(new MapEventsAsync.TileLoadListener() {
            @UiThread @MainThread
            @Override
            public void onTileLoading(@NonNull final MapView mapView, final int x, final int y, final int z, @IMapTileProviderCallback.TILEPROVIDERTYPE final int providerType) {
                //Log.e(TAG, "Loading Tile " + x + "," + y + "," + z + " [" + IMapTileProviderCallback.decodeTileProviderName(providerType) + "]...");
            }
            @UiThread @MainThread
            @Override
            public void onTileLoadSuccess(@NonNull final MapView mapView, final int x, final int y, final int z, @IMapTileProviderCallback.TILEPROVIDERTYPE final int providerType, final long loadingTime_ms) {
                //Log.e(TAG, "Tile " + x + "," + y + "," + z + " [" + IMapTileProviderCallback.decodeTileProviderName(providerType) + "] loaded successfully in " + loadingTime_ms + "ms");
            }
            @UiThread @MainThread
            @Override
            public void onTileLoadFail(@NonNull final MapView mapView, final int x, final int y, final int z, @IMapTileProviderCallback.TILEPROVIDERTYPE final int providerType, final long loadingTime_ms) {
                //Log.e(TAG, "Error loading Tile " + x + "," + y + "," + z + " [" + IMapTileProviderCallback.decodeTileProviderName(providerType) + "] after " + loadingTime_ms + "ms!!");
            }
            @UiThread @MainThread
            @Override
            public void onTileLoadCancelled(@NonNull final MapView mapView, final int x, final int y, final int z, @IMapTileProviderCallback.TILEPROVIDERTYPE final int providerType, final long loadingTime_ms) {
                //Log.e(TAG, "Tile loading cancelled " + x + "," + y + "," + z + " [" + IMapTileProviderCallback.decodeTileProviderName(providerType) + "] after " + loadingTime_ms + "ms!!");
            }
        }).addEventListener_OnViewBoundingBoxChanged(new MapEventsAsync.ViewBoundingBoxChangedListener() {
            @Override
            public void onViewBoundingBoxChanged(final int fromZoom, final int toZoom) {
                //Log.e(TAG, "OnViewBoundingBoxChanged(from " + fromZoom + " to " + toZoom + ")");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLifecycleRegistry.setCurrentState(Lifecycle.State.RESUMED);
    }

    @Override
    protected void onPause() {
        mLifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);
        super.onPause();
    }

    @Override
    protected void onStop() {
        mLifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mLifecycleRegistry.setCurrentState(Lifecycle.State.DESTROYED);
        super.onDestroy();
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() { return mLifecycleRegistry; }
}
