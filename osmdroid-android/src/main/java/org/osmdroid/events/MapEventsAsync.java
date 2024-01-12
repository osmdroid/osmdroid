package org.osmdroid.events;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.util.SimpleInvalidationHandler;
import org.osmdroid.util.ReusablePoolDynamic;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class used to get detailed/low-level Map Events that normal {@link MapView} implementation doesn't offers.
 */
public final class MapEventsAsync implements ViewModelStoreOwner {
    private static final String TAG = "MapEventsAsync";

    private final MapView mMapView;
    private final Handler mMainThreadHandler;
    private final MapEventsAsyncListener mMapEventsAsyncListener;
    private final HashMap<Integer,CallbackWorkingWithResult<?>> mCallbackWorkingWithResults = new HashMap<>();
    private final HashMap<Integer,CallbackEvent<?>> mCallbackEvents = new HashMap<>();
    private final List<TileLoadListener> mTileLoadListeners = new ArrayList<>();
    private final List<ViewBoundingBoxChangedListener> mViewBoundingBoxChangedListeners = new ArrayList<>();
    private final HashMap<Long,Long> mRemainingToResolve = new HashMap<>();
    private final HashMap<Integer,Integer> mPendingLoadingTypes = new HashMap<>();
    private long mLastLoadingTileProgression = 0L;
    @Nullable
    private MapView.OnFirstLayoutListener mOnFirstLayoutListener = null;
        //ViewModel
    private final ViewModelStore mViewModelStore = new ViewModelStore();
    private final MapEventsAsyncViewModel mMapEventsAsyncViewModel;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value={ TH_MAINMESSAGE_ONINIT, TH_MAINMESSAGE_ONINITDONE, TH_MAINMESSAGE_ONFIRSTLAYOUT, TH_MAINMESSAGE_ONSCROLL, TH_MAINMESSAGE_ONSCROLLDONE, TH_MAINMESSAGE_ONZOOM,
            TH_MAINMESSAGE_ONZOOMDONE, TH_MAINMESSAGE_ONTILELOADSUCCESS, TH_MAINMESSAGE_ONTILELOADFAILED, TH_MAINMESSAGE_ONTILELOADING, TH_MAINMESSAGE_ONLOADINGDONE,
            TH_MAINMESSAGE_ONVIEWBOUNDINGBOXCHANGED, TH_MAINMESSAGE_ONPAUSE, TH_MAINMESSAGE_ONRESUME, TH_MAINMESSAGE_ONDETACHEDFROMWINDOW, TH_MAINMESSAGE_ONTILELOADCANCELLED })
    private @interface TH_MAINMESSAGE {}
    private static final int TH_MAINMESSAGE_ONINIT                      = 1 << 0;
    private static final int TH_MAINMESSAGE_ONINITDONE                  = 1 << 1;
    private static final int TH_MAINMESSAGE_ONFIRSTLAYOUT               = 1 << 2;
    private static final int TH_MAINMESSAGE_ONSCROLL                    = 1 << 3;
    private static final int TH_MAINMESSAGE_ONSCROLLDONE                = 1 << 4;
    private static final int TH_MAINMESSAGE_ONZOOM                      = 1 << 5;
    private static final int TH_MAINMESSAGE_ONZOOMDONE                  = 1 << 6;
    private static final int TH_MAINMESSAGE_ONTILELOADSUCCESS           = 1 << 7;
    private static final int TH_MAINMESSAGE_ONTILELOADFAILED            = 1 << 8;
    private static final int TH_MAINMESSAGE_ONTILELOADING               = 1 << 9;
    private static final int TH_MAINMESSAGE_ONTILELOADCANCELLED         = 1 << 10;
    private static final int TH_MAINMESSAGE_ONLOADINGDONE               = 1 << 11;
    private static final int TH_MAINMESSAGE_ONVIEWBOUNDINGBOXCHANGED    = 1 << 12;
    private static final int TH_MAINMESSAGE_ONPAUSE                     = 1 << 13;
    private static final int TH_MAINMESSAGE_ONRESUME                    = 1 << 14;
    private static final int TH_MAINMESSAGE_ONDETACHEDFROMWINDOW        = 1 << 15;
    private static int maskThMainMessage(@TH_MAINMESSAGE final int thMainMessage, @IMapTileProviderCallback.TILEPROVIDERTYPE final int providerType) { return (thMainMessage | providerType); }
    @SuppressLint("WrongConstant")
    @TH_MAINMESSAGE
    public static int unmaskThMainMessage(final int messageWhat) { return (messageWhat & 0xFFFF); }
    @SuppressLint("WrongConstant")
    @IMapTileProviderCallback.TILEPROVIDERTYPE
    public static int unmaskTileProviderType(final int messageWhat) { return (messageWhat & 0xFFFF0000); }

    @UiThread @MainThread
    public MapEventsAsync(@NonNull final MapView mapView, @NonNull final MapEventsAsyncListener listener, @Nullable final ThreadParams params) {
        mMapView = mapView;
        mMapEventsAsyncViewModel = new ViewModelProvider(this).get(MapEventsAsyncViewModel.class);
        mMapEventsAsyncViewModel.mLoadingLiveData.observe(mapView, new Observer<Long>() {
            @Override
            public void onChanged(@Nullable final Long value) {
                if (value == null) return;
                final long v = value;
                @LOADINGTYPE
                final int cLoadingType = (int)(v >> 32);
                final int cPercentage = (short)(v >> 16);
                @LOADINGTYPE
                final int cRemaining = (short)v;
                listener.onLoadingProgress(cLoadingType, cPercentage, cRemaining);
            }
        });
        mMapView.getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override public void onResume(@NonNull final LifecycleOwner owner) { postToMainThread_OnResume(); }
            @Override public void onPause(@NonNull final LifecycleOwner owner) { postToMainThread_OnPause(); }
        });
        mMapEventsAsyncListener = listener;
        mMapView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(@NonNull final View v) { onMapViewAttachedToWindow(mapView, listener, params); }
            @Override public void onViewDetachedFromWindow(@NonNull final View v) { onMapViewDetachedFromWindow(mapView, listener, params); }
        });
        mMainThreadHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            private WorkingThread mWorkingThread;
            {
                this.ensureThreadIsRunning();
            }
            /** @noinspection unchecked*/
            @UiThread @MainThread
            @Override
            public boolean handleMessage(@NonNull final Message msg) {
                @TH_MAINMESSAGE
                final int cUnmaskedWhat = unmaskThMainMessage(msg.what);
                switch (cUnmaskedWhat) {
                    case TH_MAINMESSAGE_ONINIT: {
                        this.ensureThreadIsRunning();
                        MapEventsAsync.this.mPendingLoadingTypes.put(LOADINGTYPE_MAPINIT, LOADINGTYPE_MAPINIT);
                        MapEventsAsync.this.postOnLoadingProgressToViewModel(LOADINGTYPE_MAPINIT, null, null, null, (MapEventsAsync.this.getCurrentPendingLoadingTypes() &~ LOADINGTYPE_MAPINIT));
                        postToWorkingThread_OnInit(this.mWorkingThread, msg.obj);
                        break;
                    }
                    case TH_MAINMESSAGE_ONINITDONE: {
                        MapEventsAsync.this.mPendingLoadingTypes.remove(LOADINGTYPE_MAPINIT);
                        MapEventsAsync.this.postToMainThread_OnLoadingDone(LOADINGTYPE_MAPINIT, (MapEventsAsync.this.getCurrentPendingLoadingTypes() &~ LOADINGTYPE_MAPINIT));
                        listener.onInitDone(mapView);
                        break;
                    }
                    case TH_MAINMESSAGE_ONFIRSTLAYOUT: {
                        /*
                         * - arg1: (int) onLayout().left
                         * - arg2: (int) onLayout().top
                         * - obj: (long) width_and_height
                         */
                        final int cLeft = msg.arg1;
                        final int cTop = msg.arg2;
                        final long cWidthAndHeight = (long)msg.obj;
                        final int cWidth = (int)(cWidthAndHeight >> 32);
                        final int cHeight = (int)cWidthAndHeight;
                        listener.onFirstLayout(cLeft, cTop, cLeft+cWidth, cTop+cHeight);
                        break;
                    }
                    case TH_MAINMESSAGE_ONSCROLL: {
                        /*
                         * - obj: (ScrollEvent)
                         */
                        final ScrollEvent cScrollEvent = (ScrollEvent)msg.obj;
                        postToWorkingThread_OnScroll(this.mWorkingThread, cScrollEvent);
                        break;
                    }
                    case TH_MAINMESSAGE_ONSCROLLDONE: {
                        /*
                         * - obj: (Object[]) callbackResult
                         */
                        final CallbackWorkingWithResult<Object> cCallbackWorkingWithResult = (CallbackWorkingWithResult<Object>)MapEventsAsync.this.mCallbackWorkingWithResults.get(CALLBACKWORKINGWITHRESULTTYPE_ONSCROLL);
                        if (cCallbackWorkingWithResult != null) cCallbackWorkingWithResult.onDone(MapEventsAsync.this.mMapView, msg.obj);
                        break;
                    }
                    case TH_MAINMESSAGE_ONZOOM: {
                        /*
                         * - obj: (ZoomEvent)
                         */
                        final ZoomEvent cZoomEvent = (ZoomEvent)msg.obj;
                        postToWorkingThread_OnZoom(this.mWorkingThread, cZoomEvent);
                        break;
                    }
                    case TH_MAINMESSAGE_ONZOOMDONE: {
                        /*
                         * - obj: (Object[]) callbackResult
                         */
                        final CallbackWorkingWithResult<Object> cCallbackWorkingWithResult = (CallbackWorkingWithResult<Object>)MapEventsAsync.this.mCallbackWorkingWithResults.get(CALLBACKWORKINGWITHRESULTTYPE_ONZOOM);
                        if (cCallbackWorkingWithResult != null) cCallbackWorkingWithResult.onDone(MapEventsAsync.this.mMapView, msg.obj);
                        break;
                    }
                    case TH_MAINMESSAGE_ONTILELOADCANCELLED :
                    case TH_MAINMESSAGE_ONTILELOADING       :
                    case TH_MAINMESSAGE_ONTILELOADFAILED    :
                    case TH_MAINMESSAGE_ONTILELOADSUCCESS   : {
                        /*
                         * - arg1: (int) half-long-A for mapTileIndex
                         * - arg2: (int) half-long-B for mapTileIndex
                         * - obj: (long) loadingTime_ms
                         */
                        /*
                        @IMapTileProviderCallback.TILEPROVIDERTYPE
                        final int cProviderType = unmaskTileProviderType(msg.what);
                        final long cMapTileIndex = ((long)msg.arg1 << 32) | (msg.arg2 & 0xFFFFFFFFL);
                        @Nullable
                        Long cLoadingTime_ms = (Long)msg.obj;
                        if (cLoadingTime_ms == null) cLoadingTime_ms = -1L;
                        final int cX = MapTileIndex.getX(cMapTileIndex);
                        final int cY = MapTileIndex.getY(cMapTileIndex);
                        final int cZ = MapTileIndex.getZoom(cMapTileIndex);
                        if (cUnmaskedWhat == TH_MAINMESSAGE_ONTILELOADING) {
                            boolean cIsFirst = false;
                            if (!MapEventsAsync.this.mRemainingToResolve.containsKey(cMapTileIndex)) {
                                MapEventsAsync.this.mRemainingToResolve.put(cMapTileIndex, cMapTileIndex);
                                cIsFirst = (MapEventsAsync.this.mRemainingToResolve.size() == 1);
                                if (cIsFirst) MapEventsAsync.this.mPendingLoadingTypes.put(LOADINGTYPE_TILES, LOADINGTYPE_TILES);
                                for (final TileLoadListener cTileListener : MapEventsAsync.this.mTileLoadListeners) cTileListener.onTileLoading(MapEventsAsync.this.mMapView, cX, cY, cZ, cProviderType);
                            }
                            final long cSystemClock;
                            final boolean cIsTimerExpired = (((cSystemClock = SystemClock.elapsedRealtime()) - this.mLastLoadingTileProgression) >= 333/*ms* /);
                            if (cIsFirst || cIsTimerExpired) {
                                MapEventsAsync.this.postOnLoadingProgressToViewModel(LOADINGTYPE_TILES, null, null, null, (this.getCurrentPendingLoadingTypes() &~ LOADINGTYPE_TILES));
                                this.mLastLoadingTileProgression = cSystemClock;
                            }
                        } else {
                            for (final TileLoadListener cTileListener : MapEventsAsync.this.mTileLoadListeners) {
                                if (cUnmaskedWhat == TH_MAINMESSAGE_ONTILELOADSUCCESS) cTileListener.onTileLoadSuccess(MapEventsAsync.this.mMapView, cX, cY, cZ, cProviderType, cLoadingTime_ms);
                                else if (cUnmaskedWhat == TH_MAINMESSAGE_ONTILELOADCANCELLED) cTileListener.onTileLoadCancelled(MapEventsAsync.this.mMapView, cX, cY, cZ, cProviderType, cLoadingTime_ms);
                                else cTileListener.onTileLoadFail(MapEventsAsync.this.mMapView, cX, cY, cZ, cProviderType, cLoadingTime_ms);
                            }
                            @Nullable
                            final Long cRemoved = MapEventsAsync.this.mRemainingToResolve.remove(cMapTileIndex);
                            if (cRemoved == null) {
                                Log.e(TAG, "IIIIIIIIIIIIIIIIIIIIIIII:" + cMapTileIndex);
                            }
                        }
                            //check final counter
                        Log.e(TAG, "__remaining: " + MapEventsAsync.this.mRemainingToResolve.values());
                        if (MapEventsAsync.this.mRemainingToResolve.size() == 0) {
                            @LOADINGTYPE
                            int cPendingLoadingTypes = this.getCurrentPendingLoadingTypes();
                            if (MapEventsAsync.this.mRemainingToResolve.size() == 0) {
                                cPendingLoadingTypes &=~ LOADINGTYPE_TILES;
                                MapEventsAsync.this.mPendingLoadingTypes.remove(LOADINGTYPE_TILES);
                            }
                            MapEventsAsync.this.postToMainThread_OnLoadingDone(LOADINGTYPE_TILES, cPendingLoadingTypes);
                        }
                        */
                        handleTileLoadingData(msg.what, msg.arg1, msg.arg2, (Long)msg.obj);
                        break;
                    }
                    case TH_MAINMESSAGE_ONLOADINGDONE: {
                        /*
                         * - arg1: (int) @LOADINGTYPE current just done
                         * - arg2: (int) @LOADINGTYPE remainingLoadingTypes flags
                         */
                        @LOADINGTYPE
                        final int cLoadingType = msg.arg1;
                        @LOADINGTYPE
                        final int cRemainingLoadingTypes = msg.arg2;
                        listener.onLoadingDone(cLoadingType, cRemainingLoadingTypes);
                        break;
                    }
                    case TH_MAINMESSAGE_ONVIEWBOUNDINGBOXCHANGED: {
                        /*
                         * - arg1: (int) zooms
                         */
                        final short cToZoom = (short)(msg.arg1 >> 16);
                        final short cFromZoom = (short)msg.arg1;
                        for (final ViewBoundingBoxChangedListener cListener : mViewBoundingBoxChangedListeners) {
                            cListener.onViewBoundingBoxChanged(cFromZoom, cToZoom);
                        }
                        break;
                    }
                    case TH_MAINMESSAGE_ONPAUSE: {
                        postToWorkingThread_OnPause(this.mWorkingThread);
                        break;
                    }
                    case TH_MAINMESSAGE_ONRESUME: {
                        this.ensureThreadIsRunning();
                        postToWorkingThread_OnResume(this.mWorkingThread);
                        break;
                    }
                    case TH_MAINMESSAGE_ONDETACHEDFROMWINDOW: {
                        postToWorkingThread_Quit(this.mWorkingThread);
                        break;
                    }
                }
                return false;
            }
            /** @noinspection SynchronizationOnLocalVariableOrMethodParameter*/
            private WorkingThread ensureThreadIsRunning() {
                if ((mWorkingThread == null) || !mWorkingThread.isAlive() || mWorkingThread.isInterrupted()) {
                    final ReusablePoolDynamic.SyncObj<Boolean> cSyncObj = new ReusablePoolDynamic.SyncObj<>(Boolean.FALSE);
                    mWorkingThread = new WorkingThread(MapEventsAsync.this, params, cSyncObj);
                    mWorkingThread.start();
                    while (cSyncObj.get() != Boolean.TRUE) {
                        try { synchronized (cSyncObj) { cSyncObj.wait(); } } catch (Throwable e) { /*nothing*/ }
                    }
                }
                return mWorkingThread;
            }
        });
        if (mapView.isAttachedToWindow()) this.onMapViewAttachedToWindow(mapView, listener, params);
    }

    private void handleTileLoadingData(final int what, final int halfLongA, final int halfLongB, @Nullable final Long loadingTime_ms) {
        @TH_MAINMESSAGE
        final int cUnmaskedWhat = unmaskThMainMessage(what);
        @IMapTileProviderCallback.TILEPROVIDERTYPE
        final int cProviderType = unmaskTileProviderType(what);
        final long cMapTileIndex = ((long)halfLongA << 32) | (halfLongB & 0xFFFFFFFFL);
        @Nullable
        Long cLoadingTime_ms = loadingTime_ms;
        if (cLoadingTime_ms == null) cLoadingTime_ms = -1L;
        final int cX = MapTileIndex.getX(cMapTileIndex);
        final int cY = MapTileIndex.getY(cMapTileIndex);
        final int cZ = MapTileIndex.getZoom(cMapTileIndex);
        if (cUnmaskedWhat == TH_MAINMESSAGE_ONTILELOADING) {
            boolean cIsFirst = false;
            if (!this.mRemainingToResolve.containsKey(cMapTileIndex)) {
                this.mRemainingToResolve.put(cMapTileIndex, cMapTileIndex);
                cIsFirst = (this.mRemainingToResolve.size() == 1);
                if (cIsFirst) this.mPendingLoadingTypes.put(LOADINGTYPE_TILES, LOADINGTYPE_TILES);
                for (final TileLoadListener cTileListener : MapEventsAsync.this.mTileLoadListeners) cTileListener.onTileLoading(MapEventsAsync.this.mMapView, cX, cY, cZ, cProviderType);
            }
            final long cSystemClock;
            final boolean cIsTimerExpired = (((cSystemClock = SystemClock.elapsedRealtime()) - MapEventsAsync.this.mLastLoadingTileProgression) >= 333/*ms*/);
            if (cIsFirst || cIsTimerExpired) {
                MapEventsAsync.this.postOnLoadingProgressToViewModel(LOADINGTYPE_TILES, null, null, null, (MapEventsAsync.this.getCurrentPendingLoadingTypes() &~ LOADINGTYPE_TILES));
                this.mLastLoadingTileProgression = cSystemClock;
            }
        } else {
            for (final TileLoadListener cTileListener : MapEventsAsync.this.mTileLoadListeners) {
                if (cUnmaskedWhat == TH_MAINMESSAGE_ONTILELOADSUCCESS) cTileListener.onTileLoadSuccess(MapEventsAsync.this.mMapView, cX, cY, cZ, cProviderType, cLoadingTime_ms);
                else if (cUnmaskedWhat == TH_MAINMESSAGE_ONTILELOADCANCELLED) cTileListener.onTileLoadCancelled(MapEventsAsync.this.mMapView, cX, cY, cZ, cProviderType, cLoadingTime_ms);
                else cTileListener.onTileLoadFail(MapEventsAsync.this.mMapView, cX, cY, cZ, cProviderType, cLoadingTime_ms);
            }
            @Nullable
            final Long cRemoved = this.mRemainingToResolve.remove(cMapTileIndex);
            /*
            if (cRemoved == null) {
                Log.e(TAG, "IIIIIIIIIIIIIIIIIIIIIIII:" + cMapTileIndex);
            }
            */
        }
            //check final counter
        //Log.e(TAG, "__remaining: " + this.mRemainingToResolve.values());
        if (this.mRemainingToResolve.size() == 0) {
            @LOADINGTYPE
            int cPendingLoadingTypes = this.getCurrentPendingLoadingTypes();
            if (this.mRemainingToResolve.size() == 0) {
                cPendingLoadingTypes &=~ LOADINGTYPE_TILES;
                this.mPendingLoadingTypes.remove(LOADINGTYPE_TILES);
            }
            MapEventsAsync.this.postToMainThread_OnLoadingDone(LOADINGTYPE_TILES, cPendingLoadingTypes);
        }
    }

    @LOADINGTYPE
    private int getCurrentPendingLoadingTypes() {
        @LOADINGTYPE
        int res = 0;
        for (@LOADINGTYPE final int cType : MapEventsAsync.this.mPendingLoadingTypes.keySet()) res |= cType;
        return res;
    }

    @SuppressLint("HandlerLeak")
    private void onMapViewAttachedToWindow(@NonNull final MapView mapView, @NonNull final MapEventsAsyncListener listener, @Nullable final ThreadParams params) {
        mapView.addMapListener(new MapListener() {
            @UiThread @MainThread
            @Override
            public void onViewBoundingBoxChanged(@NonNull final Rect fromBounds, final int fromZoom, @NonNull final Rect toBounds, final int toZoom) {
                postToMainThread_OnViewBoundingBoxChangedEvent(toBounds, fromZoom, toZoom);
            }
            @UiThread @MainThread
            @Override
            public boolean onScroll(@NonNull final ScrollEvent event) {
                postToMainThread_OnScroll(event);
                return false;
            }
            @UiThread @MainThread
            @Override
            public boolean onZoom(@NonNull final ZoomEvent event) {
                postToMainThread_OnZoom(event);
                return false;
            }
        });
        if (mOnFirstLayoutListener != null) mapView.removeOnFirstLayoutListener(mOnFirstLayoutListener);
        mapView.addOnFirstLayoutListener(mOnFirstLayoutListener = new MapView.OnFirstLayoutListener() {
            @Override
            public void onFirstLayout(@NonNull final View v, final int left, final int top, final int right, final int bottom) { postToMainThread_OnFirstLayout(left, top, right, bottom); }
        });
        mapView.setTileRequestCompleteHandler(new SimpleInvalidationHandler(mapView) {
            @Override
            @UiThread @MainThread
            public void handleMessage(@NonNull final Message msg) {
                @IMapTileProviderCallback.TILEPROVIDERTYPE
                final int cProviderType = MapTileProviderBase.unmaskTileProviderType(msg.what);
                @MapTileProviderBase.MAPTYPERESULT
                final int cMapTypeResult = MapTileProviderBase.unmaskMapTypeResult(msg.what);
                switch (cMapTypeResult) {
                    case MapTileProviderBase.MAPTYPERESULT_LOADING                  : {
                        /*
                         * - arg1: (int) half-long-A for mapTileIndex
                         * - arg2: (int) half-long-B for mapTileIndex
                         */
                        final long cMapTileIndex = (((long)msg.arg1 << 32) | (msg.arg2 & 0xFFFFFFFFL));
                        //Log.e(TAG, "__loading("+ IMapTileProviderCallback.decodeTileProviderName(cProviderType)+"): " + cMapTileIndex + " ("+MapTileIndex.getX(cMapTileIndex)+","+MapTileIndex.getY(cMapTileIndex)+","+MapTileIndex.getZoom(cMapTileIndex)+")");
                        /*
                        postToMainThread_OnTileLoading(msg.arg1, msg.arg2, cProviderType);
                        */handleTileLoadingData(maskThMainMessage(TH_MAINMESSAGE_ONTILELOADING, cProviderType), msg.arg1, msg.arg2, 0L);
                        break;
                    }
                    case MapTileProviderBase.MAPTYPERESULT_FAIL                     :
                    case MapTileProviderBase.MAPTYPERESULT_SUCCESS                  :
                    case MapTileProviderBase.MAPTYPERESULT_DONE_BUT_UNKNOWN         :
                    case MapTileProviderBase.MAPTYPERESULT_DISCARTED_OUT_OF_BOUNDS  : {
                        /*
                         * - arg1: (int) half-long-A for mapTileIndex
                         * - arg2: (int) half-long-B for mapTileIndex
                         * - obj: (long) loadingTime_ms
                         */
                        final long cMapTileIndex = (((long)msg.arg1 << 32) | (msg.arg2 & 0xFFFFFFFFL));
                        final long cLoadingTime_ms = (long)msg.obj;
                        final String cResult;
                        final int cWhat;
                        if (cMapTypeResult == MapTileProviderBase.MAPTYPERESULT_FAIL) {
                            /*
                            postToMainThread_OnTileLoadFailed(msg.arg1, msg.arg2, cProviderType, cLoadingTime_ms);
                            */cWhat = maskThMainMessage(TH_MAINMESSAGE_ONTILELOADFAILED, cProviderType);
                            cResult = "__failed";
                        } else if ((cMapTypeResult == MapTileProviderBase.MAPTYPERESULT_DISCARTED_OUT_OF_BOUNDS) || (cMapTypeResult == MapTileProviderBase.MAPTYPERESULT_DONE_BUT_UNKNOWN)) {
                            /*
                            postToMainThread_OnTileLoadCancelled(msg.arg1, msg.arg2, cProviderType, cLoadingTime_ms);
                            */cWhat = maskThMainMessage(TH_MAINMESSAGE_ONTILELOADCANCELLED, cProviderType);
                            cResult = "__cancelled";
                        } else {
                            /*
                            postToMainThread_OnTileLoadSuccess(msg.arg1, msg.arg2, cProviderType, cLoadingTime_ms);
                            */cWhat = maskThMainMessage(TH_MAINMESSAGE_ONTILELOADSUCCESS, cProviderType);
                            cResult = "__success";
                        }
                        //Log.e(TAG, cResult+"("+IMapTileProviderCallback.decodeTileProviderName(cProviderType)+"): " + cMapTileIndex + " ("+MapTileIndex.getX(cMapTileIndex)+","+MapTileIndex.getY(cMapTileIndex)+","+MapTileIndex.getZoom(cMapTileIndex)+") in "+cLoadingTime_ms+"ms | reason: #" + cMapTypeResult);
                        handleTileLoadingData(cWhat, msg.arg1, msg.arg2, cLoadingTime_ms);
                        break;
                    }
                    default: {
                        Log.e(TAG, "__unknown tile result: " + cMapTypeResult);
                        /*
                        postToMainThread_OnTileLoadCancelled(msg.arg1, msg.arg2, cProviderType, 0L);
                        */handleTileLoadingData(maskThMainMessage(TH_MAINMESSAGE_ONTILELOADCANCELLED, cProviderType), msg.arg1, msg.arg2, 0L);
                    }
                }
                super.handleMessage(msg);
            }
        });
        postToMainThread_OnInit(params);
    }

    @NonNull
    @Override
    public ViewModelStore getViewModelStore() { return mViewModelStore; }

    private void onMapViewDetachedFromWindow(@NonNull final MapView mapView, @NonNull final MapEventsAsyncListener listener, @Nullable final ThreadParams params) {
        postToMainThread_OnDetachedFromWindow();
        if (mOnFirstLayoutListener != null) {
            mapView.removeOnFirstLayoutListener(mOnFirstLayoutListener);
            mOnFirstLayoutListener = null;
        }
    }

    private boolean postToMainThread(@TH_MAINMESSAGE final int what) { return mMainThreadHandler.sendEmptyMessage(what); }
    private boolean postToMainThread(@TH_MAINMESSAGE final int what, final int arg1, final int arg2) { return this.postToMainThread(what, arg1, arg2, null, false, false, 0); }
    private boolean postToMainThread(@TH_MAINMESSAGE final int what, final int arg1, final int arg2, @Nullable final Object obj) { return this.postToMainThread(what, arg1, arg2, obj, false, false, 0); }
    private boolean postToMainThread(@TH_MAINMESSAGE final int what, final int arg1, final int arg2, @Nullable final Object obj, final int delay) { return this.postToMainThread(what, arg1, arg2, obj, false, false, delay); }
    private boolean postToMainThread(@TH_MAINMESSAGE final int what, final int arg1, final int arg2, @Nullable final Object obj, final boolean removePrevSimilarMessages, final boolean dontDuplicatePost, final int delay) {
        if (dontDuplicatePost && mMainThreadHandler.hasMessages(what)) return true;
        if (removePrevSimilarMessages) mMainThreadHandler.removeMessages(what);
        final Message cMessage = Message.obtain(mMainThreadHandler, what, arg1, arg2, obj);
        if (delay > 0) return mMainThreadHandler.sendMessageDelayed(cMessage, delay);
        return mMainThreadHandler.sendMessage(cMessage);
    }
    @UiThread @MainThread
    private boolean postToMainThread_OnInit(@NonNull final Object... threadParams) { return postToMainThread(TH_MAINMESSAGE_ONINIT, 0, 0, threadParams, false, false, 0); }
    @WorkerThread
    private boolean postToMainThread_OnInitDone() { return postToMainThread(TH_MAINMESSAGE_ONINITDONE); }
    @UiThread @MainThread
    private boolean postToMainThread_OnFirstLayout(final int left, final int top, final int right, final int bottom) { return postToMainThread(TH_MAINMESSAGE_ONFIRSTLAYOUT, left, top, ((long)(right-left) << 32) | ((bottom-top) & 0xFFFFFFFFL), false, false, 0); }
    @UiThread @MainThread
    private boolean postToMainThread_OnDetachedFromWindow() { return postToMainThread(TH_MAINMESSAGE_ONDETACHEDFROMWINDOW); }
    private boolean postToMainThread_OnScroll(@NonNull final ScrollEvent event) { return postToMainThread(TH_MAINMESSAGE_ONSCROLL, 0, 0, event, false, false, 0); }
    @WorkerThread
    private boolean postToMainThread_OnScrollDone(@Nullable final Object callbackResult) { return postToMainThread(TH_MAINMESSAGE_ONSCROLLDONE, 0, 0, callbackResult, false, false, 0); }
    private boolean postToMainThread_OnZoom(@NonNull final ZoomEvent event) { return postToMainThread(TH_MAINMESSAGE_ONZOOM, 0, 0, event, false, false, 0); }
    private boolean postToMainThread_OnViewBoundingBoxChangedEvent(@NonNull final Rect toBounds, final int fromZoom, final int toZoom) {
        return postToMainThread(TH_MAINMESSAGE_ONVIEWBOUNDINGBOXCHANGED, ((toZoom << 16) | (fromZoom & 0xFFFF)), 0, null, false, false, 0);
    }
    @WorkerThread
    private boolean postToMainThread_OnZoomDone(@NonNull final Object callbackResult) { return postToMainThread(TH_MAINMESSAGE_ONZOOMDONE, 0, 0, callbackResult, false, false, 0); }
    /*
    private boolean postToMainThread_OnTileLoadFailed(final int halfLongA, final int halfLongB, @IMapTileProviderCallback.TILEPROVIDERTYPE final int providerType, @Nullable final Long cLoadingTime_ms) { return postToMainThread(maskThMainMessage(TH_MAINMESSAGE_ONTILELOADFAILED, providerType), halfLongA, halfLongB, cLoadingTime_ms); }
    private boolean postToMainThread_OnTileLoadSuccess(final int halfLongA, final int halfLongB, @IMapTileProviderCallback.TILEPROVIDERTYPE final int providerType, @Nullable final Long cLoadingTime_ms) { return postToMainThread(maskThMainMessage(TH_MAINMESSAGE_ONTILELOADSUCCESS, providerType), halfLongA, halfLongB, cLoadingTime_ms); }
    private boolean postToMainThread_OnTileLoading(final int halfLongA, final int halfLongB, @IMapTileProviderCallback.TILEPROVIDERTYPE final int providerType) { return postToMainThread(maskThMainMessage(TH_MAINMESSAGE_ONTILELOADING, providerType), halfLongA, halfLongB); }
    private boolean postToMainThread_OnTileLoadCancelled(final int halfLongA, final int halfLongB, @IMapTileProviderCallback.TILEPROVIDERTYPE final int providerType, @Nullable final Long cLoadingTime_ms) { return postToMainThread(maskThMainMessage(TH_MAINMESSAGE_ONTILELOADCANCELLED, providerType), halfLongA, halfLongB, cLoadingTime_ms); }
    */
    private boolean postToMainThread_OnPause() { return postToMainThread(TH_MAINMESSAGE_ONPAUSE); }
    private boolean postToMainThread_OnResume() { return postToMainThread(TH_MAINMESSAGE_ONRESUME); }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(flag=true, value={ LOADINGTYPE_MAPINIT, LOADINGTYPE_TILES })
    public @interface LOADINGTYPE {}
    /* ===================================================
       ===  EACH TYPE SHOULD BE BITWISE SHIFTED USING  ===
       ===               "<<" OPERATOR                 ===
       =================================================== */
    private static final int LOADINGTYPE_MAPINIT    = 1 << 30;
    private static final int LOADINGTYPE_TILES      = 1 << 31;
    /* ===================================================
       =================================================== */
    private void postOnLoadingProgressToViewModel(@LOADINGTYPE final int loadingType, @Nullable final Number min, @Nullable final Number max, @Nullable final Number current, @LOADINGTYPE final int remainingLoadingTypes) {
        final int cPercentage = (((min != null) && (max != null) && (current != null)) ? (int)Math.round(((current.doubleValue() - min.doubleValue()) * 100.0d)/(max.doubleValue() - min.doubleValue())) : -1);
        final long cMapTileIndex = ((long)loadingType << 32) | ((long)cPercentage & 0xFFFF0000L) | (remainingLoadingTypes & 0x0000FFFFL);
        mMapEventsAsyncViewModel.mLoadingLiveData.postValue(cMapTileIndex);
    }
    private boolean postToMainThread_OnLoadingDone(@LOADINGTYPE final int loadingType, @LOADINGTYPE final int remainingLoadingTypes) { return postToMainThread(TH_MAINMESSAGE_ONLOADINGDONE, loadingType, remainingLoadingTypes); }
    public static String decodeLoadingType(@LOADINGTYPE final int loadingType) {
        final List<String> res = new ArrayList<>();
        if ((loadingType & LOADINGTYPE_MAPINIT) == LOADINGTYPE_MAPINIT) res.add("MAP_INIT");
        if ((loadingType & LOADINGTYPE_TILES) == LOADINGTYPE_TILES) res.add("TILE(s)");
        final String cString = String.join(",", res);
        final int cSize = res.size();
        return ((cSize > 0) ? ((cSize > 1) ? "[" + cString + "]" : cString) : "NONE");
    }

    @WorkerThread
    private static final class WorkingThread extends Thread implements Handler.Callback {
        private static final String TAG = "WorkingThread";

        @Retention(RetentionPolicy.SOURCE)
        @IntDef(value={ TH_WORKERMESSAGE_STARTED, TH_WORKERMESSAGE_QUIT, TH_WORKERMESSAGE_ONINIT, TH_WORKERMESSAGE_ONSCROLL, TH_WORKERMESSAGE_ONZOOM, TH_WORKERMESSAGE_ONPAUSE, TH_WORKERMESSAGE_ONRESUME })
        private @interface TH_WORKERMESSAGE {}
        private static final int TH_WORKERMESSAGE_STARTED   = 1;
        private static final int TH_WORKERMESSAGE_QUIT      = 2;
        private static final int TH_WORKERMESSAGE_ONINIT    = 3;
        private static final int TH_WORKERMESSAGE_ONSCROLL  = 4;
        private static final int TH_WORKERMESSAGE_ONZOOM    = 5;
        private static final int TH_WORKERMESSAGE_ONPAUSE   = 6;
        private static final int TH_WORKERMESSAGE_ONRESUME  = 7;

        private final ReusablePoolDynamic.SyncObj<Boolean> mSyncObj;
        private final MapEventsAsync mMapEventsAsync;
        private Handler mWorkingHandler;
        private final ThreadParams mThreadParams;
        private Boolean mIsMapViewInPause = null;

        @UiThread @MainThread
        public WorkingThread(@NonNull final MapEventsAsync mapEventsAsync, @Nullable final ThreadParams params, @NonNull final ReusablePoolDynamic.SyncObj<Boolean> syncObj) {
            this.mMapEventsAsync = mapEventsAsync;
            this.mThreadParams = params;
            this.mSyncObj = syncObj;
        }
        @Override
        public void run() {
            super.run();
            Looper.prepare();
            //noinspection DataFlowIssue
            this.mWorkingHandler = new Handler(Looper.myLooper(), this);
            this.postToWorkingThread(TH_WORKERMESSAGE_STARTED);
            Looper.loop();
        }
        @WorkerThread
        @Override
        public boolean handleMessage(@NonNull final Message message) {
            @TH_WORKERMESSAGE
            final int cWhat = message.what;
            switch (cWhat) {
                case TH_WORKERMESSAGE_STARTED: {
                    setPriority(Thread.MIN_PRIORITY);
                    synchronized (this.mSyncObj) {
                        this.mSyncObj.set(Boolean.TRUE);
                        this.mSyncObj.notifyAll();
                    }
                    break;
                }
                case TH_WORKERMESSAGE_QUIT: {
                    try { final Looper cLooper = Looper.myLooper(); if (cLooper != null) cLooper.quit(); } catch (Exception e) { /*nothing*/ }
                    break;
                }
                case TH_WORKERMESSAGE_ONINIT: {
                    /*
                     * - obj (Object[]) threadParams
                     */
                    this.mMapEventsAsync.mMapEventsAsyncListener.onInitWorking(this.mMapEventsAsync.mMapView);
                    this.mMapEventsAsync.postToMainThread_OnInitDone();
                    break;
                }
                case TH_WORKERMESSAGE_ONSCROLL: {
                    /*
                     * - obj: (ScrollEvent)
                     */
                    final CallbackWorkingWithResult<?> cAaa = this.mMapEventsAsync.mCallbackWorkingWithResults.get(CALLBACKWORKINGWITHRESULTTYPE_ONSCROLL);
                    if (cAaa != null) {
                        final ScrollEvent cScrollEvent = (ScrollEvent)message.obj;
                        this.mMapEventsAsync.postToMainThread_OnScrollDone(cAaa.onWorking(this.mMapEventsAsync.mMapView, cScrollEvent));
                    }
                    break;
                }
                case TH_WORKERMESSAGE_ONZOOM: {
                    /*
                     * - obj: (ZoomEvent)
                     */
                    final CallbackWorkingWithResult<?> cAaa = this.mMapEventsAsync.mCallbackWorkingWithResults.get(CALLBACKWORKINGWITHRESULTTYPE_ONZOOM);
                    if (cAaa != null) {
                        final ZoomEvent cZoomEvent = (ZoomEvent)message.obj;
                        this.mMapEventsAsync.postToMainThread_OnZoomDone(cAaa.onWorking(this.mMapEventsAsync.mMapView, cZoomEvent));
                    }
                    break;
                }
                case TH_WORKERMESSAGE_ONPAUSE: {
                    WorkingThread.this.mIsMapViewInPause = true;
                    break;
                }
                case TH_WORKERMESSAGE_ONRESUME: {
                    WorkingThread.this.mIsMapViewInPause = false;
                    break;
                }
            }
            return false;
        }
        private boolean postToWorkingThread(@TH_WORKERMESSAGE final int what) { return this.postToWorkingThread(what, 0, 0, null, false, false, 0); }
        private boolean postToWorkingThread(@TH_WORKERMESSAGE final int what, final int arg1, final int arg2) { return this.postToWorkingThread(what, arg1, arg2, null, false, false, 0); }
        private boolean postToWorkingThread(@TH_WORKERMESSAGE final int what, @Nullable final Object obj) { return this.postToWorkingThread(what, 0, 0, obj, false, false, 0); }
        private boolean postToWorkingThread(@TH_WORKERMESSAGE final int what, final int arg1, final int arg2, @Nullable final Object obj) { return this.postToWorkingThread(what, arg1, arg2, obj, false, false, 0); }
        private boolean postToWorkingThread(@TH_WORKERMESSAGE final int what, final int arg1, final int arg2, @Nullable final Object obj, final int delay) { return this.postToWorkingThread(what, arg1, arg2, obj, false, false, delay); }
        private boolean postToWorkingThread(@TH_WORKERMESSAGE final int what, final int arg1, final int arg2, @Nullable final Object obj, final boolean removePrevSimilarMessages, final boolean dontDuplicatePost, final int delay) {
            if (removePrevSimilarMessages) this.mWorkingHandler.removeMessages(what);
            if (dontDuplicatePost && this.mWorkingHandler.hasMessages(what)) return true;
            final Message cMessage = Message.obtain(this.mWorkingHandler, what, arg1, arg2, obj);
            if (delay > 0) return this.mWorkingHandler.sendMessageDelayed(cMessage, delay);
            return this.mWorkingHandler.sendMessage(cMessage);
        }
    }

    private boolean postToWorkingThread_OnInit(@NonNull final WorkingThread workingThread, @Nullable final Object... args) { return workingThread.postToWorkingThread(WorkingThread.TH_WORKERMESSAGE_ONINIT, args); }
    private boolean postToWorkingThread_OnScroll(@NonNull final WorkingThread workingThread, @NonNull final ScrollEvent event) { return workingThread.postToWorkingThread(WorkingThread.TH_WORKERMESSAGE_ONSCROLL, event); }
    private boolean postToWorkingThread_OnZoom(@NonNull final WorkingThread workingThread, @NonNull final ZoomEvent event) { return workingThread.postToWorkingThread(WorkingThread.TH_WORKERMESSAGE_ONZOOM, event); }
    private boolean postToWorkingThread_OnPause(@NonNull final WorkingThread workingThread) { return workingThread.postToWorkingThread(WorkingThread.TH_WORKERMESSAGE_ONPAUSE); }
    private boolean postToWorkingThread_OnResume(@NonNull final WorkingThread workingThread) { return workingThread.postToWorkingThread(WorkingThread.TH_WORKERMESSAGE_ONRESUME); }
    private boolean postToWorkingThread_Quit(@NonNull final WorkingThread workingThread) { return workingThread.postToWorkingThread(WorkingThread.TH_WORKERMESSAGE_QUIT); }

    /** Threads Parameter(s) */
    private static class ThreadParams {
        /* fill as you need */
    }

    @WorkerThread
    public interface MapEventsAsyncListener {
        /**
         * (<b>NON-BLOCKING method executed in another <i>Thread</i>, so please be careful!!</b>)<br><br>
         * Event raised when the {@link MapView} is created (but Tiles nor <i>FirstLayout</i> event could not be ready yet).<br>
         * <br>
         * This event is normally used when we want to execute some blocking/heavy operation(s), like loading big KML/GPX files or a lot of Markers, at the beginning.<br>
         * When the blocking operation is finished the {@link #onInitDone(MapView)} event is raised and executed in {@link UiThread}/{@link MainThread}.
         */
        @WorkerThread
        void onInitWorking(@NonNull MapView mapView);
        /** When {@link #onInitWorking(MapView)} has done its work, this Event will be raised and executed in {@link UiThread}/{@link MainThread}. */
        @UiThread @MainThread
        void onInitDone(@NonNull MapView mapView);
        /**
         * Event raised when a <i>Loading</i> procedure is just started or still is occurring.<br>
         * <br>
         *
         * @param loadingType Which <i>Loading</i> is currently running...
         * @param percentage Completion percentage from 0 to 100% (negative values means "unknown" so an Indeterminate progressbar could be useful to display)
         * @param otherRemainingTypes Other <i>LoadingTypes</i> currently pending excluding the Current <i>loadingType</i> (if ZERO it means there isn't any other Loading procedure pending)
         */
        @UiThread @MainThread
        void onLoadingProgress(@LOADINGTYPE int loadingType, int percentage, @LOADINGTYPE int otherRemainingTypes);
        /**
         * Event raised when a <i>Loading</i> procedure is finished.<br>
         * <br>
         * @param loadingType Which <i>Loading</i> is currently running...
         * @param otherRemainingTypes Other <i>LoadingTypes</i> currently pending excluding the Current <i>loadingType</i> (if ZERO it means there isn't any other Loading procedure pending)
         */
        @UiThread @MainThread
        void onLoadingDone(@LOADINGTYPE int loadingType, @LOADINGTYPE int otherRemainingTypes);
        /** Event that replaces the {@link org.osmdroid.views.MapView.OnFirstLayoutListener#onFirstLayout(View, int, int, int, int)} one */
        @UiThread @MainThread
        void onFirstLayout(int left, int top, int right, int bottom);
    }
    public static abstract class MapAsyncEventListener implements MapEventsAsyncListener {
        /** {@inheritDoc} */
        @WorkerThread
        @Override
        public void onInitWorking(@NonNull final MapView mapView) { /*nothing*/ }
        /** {@inheritDoc} */
        @UiThread @MainThread
        @Override
        public void onInitDone(@NonNull final MapView mapView) { /*nothing*/ }
        /** {@inheritDoc} */
        @UiThread @MainThread
        @Override
        public void onLoadingProgress(@LOADINGTYPE final int loadingType, final int percentage, @LOADINGTYPE final int otherRemainingTypes) { /*nothing*/ }
        /** {@inheritDoc} */
        @UiThread @MainThread
        @Override
        public void onLoadingDone(@LOADINGTYPE final int loadingType, @LOADINGTYPE final int otherRemainingTypes) { /*nothing*/ }
        /** {@inheritDoc} */
        @UiThread @MainThread
        @Override
        public void onFirstLayout(final int left, final int top, final int right, final int bottom) { /*nothing*/ }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value={ CALLBACKWORKINGWITHRESULTTYPE_ONSCROLL, CALLBACKWORKINGWITHRESULTTYPE_ONZOOM })
    /*
     * Types that raises "CallbackWorkingWithResult#onWorking(MapView, Object... params)" (executed in a @WorkerThread) while executing "blocking things"
     * and will raises "CallbackWorkingWithResult#onDone(MapView, Object workingResult)" when this "things" are completed.
     */
    public @interface CALLBACKWORKINGWITHRESULTTYPE {}
    public static final int CALLBACKWORKINGWITHRESULTTYPE_ONSCROLL  = 1;
    public static final int CALLBACKWORKINGWITHRESULTTYPE_ONZOOM    = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value={ /*CALLBACKEVENTTYPE_ONVIEWBOUNDINGBOXCHANGED*/ })
    public @interface CALLBACKEVENTTYPE {}
    /** A <i>Loading</i> action is currently in progress... */
    //public static final int CALLBACKEVENTTYPE_ONVIEWBOUNDINGBOXCHANGED = 1;

    public interface CallbackWorkingWithResult<T> {
        /**
         * (<b>NON-BLOCKING method executed in another <i>Thread</i>, so please be careful!!</b>)<br><br>
         * Code executed Asynchronously.<br>
         * When the blocking operation is finished the {@link #onDone(MapView, Object)} event is raised and executed in {@link UiThread}/{@link MainThread} by passing THIS result.
         */
        @WorkerThread
        T onWorking(@NonNull MapView mapView, @Nullable Object... args);
        /***/
        @UiThread @MainThread
        void onDone(@NonNull MapView mapView, @Nullable T workingResult);
    }
    public MapEventsAsync addCallbackWorkingWithResult(@CALLBACKWORKINGWITHRESULTTYPE final int type, @Nullable final CallbackWorkingWithResult<?> callback) { if (callback == null) return this; mCallbackWorkingWithResults.put(type, callback); return this; }
    public boolean hasCallbackWorkingWithResult(@CALLBACKWORKINGWITHRESULTTYPE final int type) { return mCallbackWorkingWithResults.containsKey(type); }
    private CallbackWorkingWithResult<?> getCallbackWorkingWithResult(@CALLBACKWORKINGWITHRESULTTYPE final int type) { return mCallbackWorkingWithResults.get(type); }

    public interface CallbackEvent<T> {
        void onCallback(@NonNull MapView mapView, @Nullable T value);
    }
    public MapEventsAsync addCallbackEvent(@CALLBACKEVENTTYPE final int type, @Nullable final CallbackEvent<?> callback) { if (callback == null) return this; mCallbackEvents.put(type, callback); return this; }
    public boolean hasCallbackEvent(@CALLBACKEVENTTYPE final int type) { return mCallbackEvents.containsKey(type); }
    private CallbackEvent<?> getCallbackEvent(@CALLBACKEVENTTYPE final int type) { return mCallbackEvents.get(type); }

    public interface ViewBoundingBoxChangedListener {
        @UiThread @MainThread
        void onViewBoundingBoxChanged(int fromZoom, int toZoom);
    }
    public MapEventsAsync addEventListener_OnViewBoundingBoxChanged(@NonNull final ViewBoundingBoxChangedListener listener) { if (!mViewBoundingBoxChangedListeners.contains(listener)) mViewBoundingBoxChangedListeners.add(listener); return this; }
    public MapEventsAsync removeEventListener_OnViewBoundingBoxChanged(@NonNull final ViewBoundingBoxChangedListener listener) { mViewBoundingBoxChangedListeners.remove(listener); return this; }

    public interface TileLoadListener {
        /** Event raised when the specific X,Y,Z Tile begins to be loaded */
        void onTileLoading(@NonNull MapView mapView, int x, int y, int z, @IMapTileProviderCallback.TILEPROVIDERTYPE int providerType);
        /** Event raised when the specific X,Y,Z Tile has successfully loaded */
        void onTileLoadSuccess(@NonNull MapView mapView, int x, int y, int z, @IMapTileProviderCallback.TILEPROVIDERTYPE int providerType, long loadingTime_ms);
        /** Event raised when the specific X,Y,Z Tile got an error during its load */
        void onTileLoadFail(@NonNull MapView mapView, int x, int y, int z, @IMapTileProviderCallback.TILEPROVIDERTYPE int providerType, long loadingTime_ms);
        /** Event raised when the specific X,Y,Z Tile has successfully loaded */
        void onTileLoadCancelled(@NonNull MapView mapView, int x, int y, int z, @IMapTileProviderCallback.TILEPROVIDERTYPE int providerType, long loadingTime_ms);
    }
    public MapEventsAsync addTileLoadListener(@Nullable final TileLoadListener listener) {
        if (mTileLoadListeners.contains(listener)) return this;
        mTileLoadListeners.add(listener);
        return this;
    }

    public static final class MapEventsAsyncViewModel extends ViewModel {
        private final MutableLiveData<Long> mLoadingLiveData = new MutableLiveData<>();
        public MapEventsAsyncViewModel() {

        }
    }

}
