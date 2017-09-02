package org.osmdroid.samplefragments.animations;
/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */


import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Property;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MarkerAnimation {

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void animateMarkerToGB(final MapView map, final Marker marker, final GeoPoint finalPosition, final GeoPointInterpolator GeoPointInterpolator) {
        final GeoPoint startPosition = marker.getPosition();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 3000;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                marker.setPosition(GeoPointInterpolator.interpolate(v, startPosition, finalPosition));
                map.invalidate();
                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public static ValueAnimator animateMarkerToHC(final MapView map, final Marker marker, final GeoPoint finalPosition, final GeoPointInterpolator GeoPointInterpolator) {
        final GeoPoint startPosition = marker.getPosition();

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = animation.getAnimatedFraction();
                GeoPoint newPosition = GeoPointInterpolator.interpolate(v, startPosition, finalPosition);
                marker.setPosition(newPosition);
                map.invalidate();

            }
        });
        valueAnimator.setFloatValues(0, 1); // Ignored.
        valueAnimator.setDuration(3000);
        valueAnimator.start();
        return valueAnimator;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static ObjectAnimator animateMarkerToICS(final MapView map, Marker marker, GeoPoint finalPosition, final GeoPointInterpolator GeoPointInterpolator) {
        TypeEvaluator<GeoPoint> typeEvaluator = new TypeEvaluator<GeoPoint>() {
            @Override
            public GeoPoint evaluate(float fraction, GeoPoint startValue, GeoPoint endValue) {
                return GeoPointInterpolator.interpolate(fraction, startValue, endValue);
            }
        };
        Property<Marker, GeoPoint> property = Property.of(Marker.class, GeoPoint.class, "position");
        ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
        animator.setDuration(3000);
        animator.start();
        return animator;
    }
}

