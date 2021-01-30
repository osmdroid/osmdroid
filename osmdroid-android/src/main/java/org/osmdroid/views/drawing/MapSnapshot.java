package org.osmdroid.views.drawing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Looper;

import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.TileStates;
import org.osmdroid.util.RectL;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Create a bitmap in the background from {@link MapView}-like data but without a {@link MapView}
 *
 * @author Fabrice Fontaine
 * @since 6.1.0
 */
public class MapSnapshot implements Runnable {

    public interface MapSnapshotable {
        void callback(final MapSnapshot pMapSnapshot);
    }

    public enum Status {
        NOTHING,
        STARTED,
        TILES_OK,
        PAINTING,
        CANVAS_OK
    }

    /**
     * The INCLUDE_FLAGs let you precise the tiles you accept in your snapshot,
     * depending on their states.
     * For instance, if your flag includes INCLUDE_FLAG_SCALED, that means that you accept
     * scaled tiles in your output.
     * If your flag equals INCLUDE_FLAG_UPTODATE, that means that you accept only up-to-date tiles,
     * and implicitly that you may have to wait, and need background downloads.
     * Cf. {@link ExpirableBitmapDrawable}
     */
    public static final int INCLUDE_FLAG_UPTODATE = 1;
    public static final int INCLUDE_FLAG_EXPIRED = 2;
    public static final int INCLUDE_FLAG_SCALED = 4;
    public static final int INCLUDE_FLAG_NOTFOUND = 8;
    public static final int INCLUDE_FLAGS_ALL =
            INCLUDE_FLAG_UPTODATE + INCLUDE_FLAG_EXPIRED + INCLUDE_FLAG_SCALED + INCLUDE_FLAG_NOTFOUND;

    /**
     * To be used in View-related Overlay's draw methods.
     * Not only are we not able to include View's in the snapshots,
     * but drawing those View's can make the app crash.
     * A solution is to catch an Exception when drawing,
     * and to be lenient when we're not on the UI thread
     */
    public static boolean isUIThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private final RectL mViewPort = new RectL();
    private final int mIncludeFlags;
    private Projection mProjection;
    private MapSnapshotHandler mHandler;
    private MapSnapshotable mMapSnapshotable;
    private MapTileProviderBase mTileProvider;
    private TilesOverlay mTilesOverlay;
    private List<Overlay> mOverlays;
    private Status mStatus = Status.NOTHING;
    private Bitmap mBitmap;
    private boolean mIsDetached;

    public MapSnapshot(final MapSnapshotable pMapSnapshotable,
                       final int pIncludeFlags,
                       final MapView pMapView) {
        this(pMapSnapshotable, pIncludeFlags,
                pMapView.getTileProvider(),
                pMapView.getOverlays(),
                pMapView.getProjection()
        );
    }

    public MapSnapshot(final MapSnapshotable pMapSnapshotable,
                       final int pIncludeFlags,
                       final MapTileProviderBase pTileProvider,
                       final List<Overlay> pOverlays,
                       final Projection pProjection) {
        mMapSnapshotable = pMapSnapshotable;
        mIncludeFlags = pIncludeFlags;
        mTileProvider = pTileProvider;
        mOverlays = pOverlays;
        mProjection = pProjection;
        mProjection.getMercatorViewPort(mViewPort);
        mTilesOverlay = new TilesOverlay(mTileProvider, null);
        mTilesOverlay.setHorizontalWrapEnabled(mProjection.isHorizontalWrapEnabled());
        mTilesOverlay.setVerticalWrapEnabled(mProjection.isVerticalWrapEnabled());
        mHandler = new MapSnapshotHandler(this);
        mTileProvider.getTileRequestCompleteHandlers().add(mHandler);
    }

    @Override
    public void run() {
        mStatus = Status.STARTED;
        refreshASAP();
    }

    public Status getStatus() {
        return mStatus;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public boolean save(final File pFile) {
        return save(mBitmap, pFile);
    }

    public void onDetach() {
        mIsDetached = true;
        mProjection = null;
        mTileProvider.getTileRequestCompleteHandlers().remove(mHandler);
        mTileProvider.detach();
        mTileProvider = null;
        mHandler.destroy();
        mHandler = null;
        mMapSnapshotable = null;
        mTilesOverlay = null;
        mOverlays = null;
        mBitmap = null;
    }

    private void draw() {
        mBitmap = Bitmap.createBitmap(mProjection.getWidth(), mProjection.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(mBitmap);
        mProjection.save(canvas, true, false);
        mTilesOverlay.drawTiles(canvas, mProjection, mProjection.getZoomLevel(), mViewPort);
        if (mOverlays != null) {
            for (final Overlay overlay : mOverlays) {
                if (overlay != null && overlay.isEnabled()) {
                    overlay.draw(canvas, mProjection);
                }
            }
        }
        mProjection.restore(canvas, false);
    }

    /**
     * Putting the tile in the memory cache by trying to draw them (but on a null Canvas)
     */
    private void refresh() {
        if (!refreshCheckStart()) {
            return;
        }
        final TileStates tileStates = mTilesOverlay.getTileStates();
        do {
            mTilesOverlay.drawTiles(null, mProjection, mProjection.getZoomLevel(), mViewPort);
            boolean ready = true;
            if (mIncludeFlags != 0 && mIncludeFlags != INCLUDE_FLAGS_ALL) {
                if (ready && (mIncludeFlags & INCLUDE_FLAG_UPTODATE) == 0 && tileStates.getUpToDate() != 0) {
                    ready = false;
                }
                if (ready && (mIncludeFlags & INCLUDE_FLAG_EXPIRED) == 0 && tileStates.getExpired() != 0) {
                    ready = false;
                }
                if (ready && (mIncludeFlags & INCLUDE_FLAG_SCALED) == 0 && tileStates.getScaled() != 0) {
                    ready = false;
                }
                if (ready && (mIncludeFlags & INCLUDE_FLAG_NOTFOUND) == 0 && tileStates.getNotFound() != 0) {
                    ready = false;
                }
            }
            if (ready) {
                if (mStatus == Status.CANVAS_OK || mStatus == Status.PAINTING) {
                    return;
                }
                if (!refreshCheckFinish()) {
                    return;
                }
                mStatus = Status.PAINTING;
                if (mIsDetached) {
                    return;
                }
                draw();
                mStatus = Status.CANVAS_OK;
                final MapSnapshotable mapSnapshotable = mMapSnapshotable;
                if (mapSnapshotable != null) {
                    mapSnapshotable.callback(MapSnapshot.this);
                }
            }
        } while (refreshCheckEnd());
    }

    synchronized private boolean refreshCheckStart() {
        if (mIsDetached) {
            return false;
        }
        if (mAlreadyFinished) {
            return false;
        }
        if (!mOneMoreTime) {
            return false;
        }
        if (mCurrentlyRunning) {
            return false;
        }
        mOneMoreTime = false;
        mCurrentlyRunning = true;
        return true;
    }

    synchronized private boolean refreshCheckEnd() {
        if (mIsDetached) {
            return false;
        }
        if (mAlreadyFinished) {
            return false;
        }
        if (!mOneMoreTime) {
            mCurrentlyRunning = false;
            return false;
        }
        mOneMoreTime = false;
        return true;
    }

    synchronized private boolean refreshCheckFinish() {
        final boolean result = !mAlreadyFinished;
        mAlreadyFinished = true;
        return result;
    }

    synchronized private boolean refreshAgain() {
        mOneMoreTime = true;
        return !mCurrentlyRunning;
    }

    public void refreshASAP() {
        if (refreshAgain()) {
            refresh();
        }
    }

    private boolean mOneMoreTime;
    private boolean mCurrentlyRunning;
    private boolean mAlreadyFinished;

    private static boolean save(Bitmap pBitmap, File pFile) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(pFile.getAbsolutePath());
            pBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}