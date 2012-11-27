package org.osmdroid.views.safecanvas;

import android.graphics.Path;
import android.graphics.PathDashPathEffect;

public class SafeDashPathEffect extends PathDashPathEffect
{
	public SafeDashPathEffect(float[] intervals, float phase, float strokeWidth)
    {
		super(createSafeDashedPath(intervals, phase, strokeWidth, null), floatSum(intervals),
				phase, PathDashPathEffect.Style.MORPH);
    }

	public static Path createSafeDashedPath(float[] intervals, float phase, float strokeWidth,
            Path reuse)
    {
        if (reuse == null)
            reuse = new Path();

        reuse.reset();
		reuse.moveTo(0, 0);
		for (int a = 0; a < intervals.length; a++) {
			if (a % 2 == 0) {
				reuse.rMoveTo(0, strokeWidth / 2);
				reuse.rLineTo(intervals[a], 0);
				reuse.rLineTo(0, -strokeWidth);
				reuse.rLineTo(-intervals[a], 0);
				reuse.rLineTo(0, strokeWidth / 2);
				reuse.rMoveTo(intervals[a], 0);
			} else {
				reuse.rMoveTo(intervals[a], 0);
			}
		}
        return reuse;
    }

	private static float floatSum(float[] array) {
		float result = 0;
		for (int a = 0; a < array.length; a++)
			result += array[a];
		return result;
	}
}
