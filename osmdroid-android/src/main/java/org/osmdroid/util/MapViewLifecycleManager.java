package org.osmdroid.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.views.MapView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

public final class MapViewLifecycleManager {
    private static final String TAG = "MapViewLifecycleManager";

    private final LifecycleRegistry mLifecycleRegistry;
    @Nullable
    private DefaultLifecycleObserver mContextLifecycleObserver = null;
    private final DefaultLifecycleObserver mDefaultLifecycleObserver;
    private final Application.ActivityLifecycleCallbacks mActivityLifecycleCallbacks;
    private boolean mInitialized = false;

    public MapViewLifecycleManager(@NonNull final MapView mapView) {
        this.mLifecycleRegistry = new LifecycleRegistry(mapView);
        this.mDefaultLifecycleObserver = new DefaultLifecycleObserver() {
            @Override
            public void onCreate(@NonNull final LifecycleOwner owner) {
                MapViewLifecycleManager.this.mLifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);
                MapViewLifecycleManager.this.init(mapView);
            }
            @Override
            public void onStart(@NonNull final LifecycleOwner owner) {
                MapViewLifecycleManager.this.mLifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);
                MapViewLifecycleManager.this.init(mapView);
            }
            @Override
            public void onResume(@NonNull final LifecycleOwner owner) {
                MapViewLifecycleManager.this.mLifecycleRegistry.setCurrentState(Lifecycle.State.RESUMED);
                MapViewLifecycleManager.this.init(mapView);
                mapView.onResume();
            }
            @Override
            public void onPause(@NonNull final LifecycleOwner owner) {
                mapView.onPause();
                MapViewLifecycleManager.this.mLifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);
            }
            @Override
            public void onStop(@NonNull final LifecycleOwner owner) {
                MapViewLifecycleManager.this.mLifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);
            }
            @Override
            public void onDestroy(@NonNull final LifecycleOwner owner) {
                mapView.onDestroy();
                MapViewLifecycleManager.this.destroy(mapView);
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.mActivityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(@NonNull final Activity activity, @Nullable final Bundle savedInstanceState) {
                    MapViewLifecycleManager.this.mLifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);
                    MapViewLifecycleManager.this.init(mapView);
                }
                @Override
                public void onActivityStarted(@NonNull final Activity activity) {
                    MapViewLifecycleManager.this.mLifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);
                    MapViewLifecycleManager.this.init(mapView);
                }
                @Override
                public void onActivityResumed(@NonNull final Activity activity) {
                    if (mapView.isAttachedToWindow()) MapViewLifecycleManager.this.mLifecycleRegistry.setCurrentState(Lifecycle.State.RESUMED);
                    MapViewLifecycleManager.this.init(mapView);
                    mapView.onResume();
                }
                @Override
                public void onActivityPaused(@NonNull final Activity activity) {
                    mapView.onPause();
                    if (!mapView.isAttachedToWindow()) MapViewLifecycleManager.this.mLifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);
                }
                @Override
                public void onActivityStopped(@NonNull final Activity activity) {
                    MapViewLifecycleManager.this.mLifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);
                }
                @Override
                public void onActivitySaveInstanceState(@NonNull final Activity activity, @NonNull final Bundle outState) { /*nothing*/ }
                @Override
                public void onActivityDestroyed(@NonNull final Activity activity) {
                    mapView.onDestroy();
                    activity.unregisterActivityLifecycleCallbacks(this);
                    MapViewLifecycleManager.this.destroy(mapView);
                }
            };
        } else this.mActivityLifecycleCallbacks = null;
        mapView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            private boolean mWasAttachedToWindow = false;
            @Override
            public void onViewAttachedToWindow(@NonNull final View v) {
                if (MapViewLifecycleManager.this.mLifecycleRegistry.getCurrentState() != Lifecycle.State.RESUMED) MapViewLifecycleManager.this.mLifecycleRegistry.setCurrentState(Lifecycle.State.RESUMED);
                this.mWasAttachedToWindow = true;
            }
            @Override
            public void onViewDetachedFromWindow(@NonNull final View v) {
                if (this.mWasAttachedToWindow) {
                    mapView.onPause();
                    mapView.onDestroy();
                    this.mWasAttachedToWindow = false;
                }
            }
        });
    }

    public void updateLifecycle(@NonNull final Context context, @NonNull final MapView mapView, final boolean isFromFirstLayout) {
        if (context instanceof LifecycleOwner) {
            Lifecycle cLifecycle = null;
            if (isFromFirstLayout) cLifecycle = getFragmentLifecycleFromContext(context, mapView);
            if (cLifecycle == null) {
                if (isFromFirstLayout) return;
                cLifecycle = ((LifecycleOwner)context).getLifecycle();
            }
            if (this.mContextLifecycleObserver != null) ((LifecycleOwner)context).getLifecycle().removeObserver(this.mContextLifecycleObserver);
            cLifecycle.addObserver((this.mContextLifecycleObserver != null) ? this.mContextLifecycleObserver : this.mDefaultLifecycleObserver);
        } else if ((context instanceof Activity) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) && !isFromFirstLayout) {
            ((Activity)context).registerActivityLifecycleCallbacks(this.mActivityLifecycleCallbacks);
        } else {
            if (!isFromFirstLayout) throw new IllegalStateException("MapView's Context needs a Lifecycle implementation");
        }
    }

    private boolean isContainedIn(@NonNull final MapView mapView, @Nullable final View view) {
        if (view == null) return false;
        if (view instanceof ViewGroup cViewGroup) {
            for (int i=0; i<cViewGroup.getChildCount(); i++) {
                final View cView = cViewGroup.getChildAt(i);
                final boolean cIsSame = Objects.equals(cView, mapView);
                if ((cView instanceof ViewGroup) && !cIsSame) return isContainedIn(mapView, cView);
                else if (cIsSame) return true;
            }
        } else return Objects.equals(view, mapView);
        return false;
    }

    @Nullable
    private Lifecycle getFragmentLifecycleFromContext(@NonNull final Context context, @NonNull final MapView mapView) {
        if (context instanceof AppCompatActivity cAppCompatActivity) {
            final FragmentManager cFragmentManager = cAppCompatActivity.getSupportFragmentManager();
            for (final Fragment cFragment : cFragmentManager.getFragments()) {
                if (isContainedIn(mapView, cFragment.getView())) return cFragment.getLifecycle();
            }
        } else Log.w(TAG, "Unable to traverse Fragment because the provided Context is not supported");
        return null;
    }

    private void init(@NonNull final MapView mapView) {
        if (this.mInitialized) return;
        mapView.onLifecycleInit();
        this.mInitialized = true;
    }
    private void destroy(@NonNull final MapView mapView) {
        if (!this.mInitialized) return;
        if (this.mContextLifecycleObserver != null) {
            final Context cContext = mapView.getContext();
            if (cContext instanceof LifecycleOwner) ((LifecycleOwner)cContext).getLifecycle().removeObserver(this.mContextLifecycleObserver);
            final Lifecycle cFragmentLifecycle = getFragmentLifecycleFromContext(cContext, mapView);
            if (cFragmentLifecycle != null) cFragmentLifecycle.removeObserver(this.mContextLifecycleObserver);
        }
        this.mContextLifecycleObserver = null;
        this.mLifecycleRegistry.setCurrentState(Lifecycle.State.DESTROYED);
        mapView.onLifecycleDestroy();
    }

    public Lifecycle getLifecycle() { return this.mLifecycleRegistry; }
}
