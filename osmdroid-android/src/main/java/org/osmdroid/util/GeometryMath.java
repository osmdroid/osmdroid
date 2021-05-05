package org.osmdroid.util;

import android.graphics.Point;
import android.graphics.Rect;

import org.osmdroid.views.util.constants.MathConstants;

/**
 * @author Marc Kurtz
 */
public class GeometryMath {
    @Deprecated
    public static final double DEG2RAD = MathConstants.DEG2RAD;
    @Deprecated
    public static final double RAD2DEG = MathConstants.RAD2DEG;

    public static final Rect getBoundingBoxForRotatatedRectangle(Rect rect, float angle, Rect reuse) {
        return getBoundingBoxForRotatatedRectangle(rect, rect.centerX(), rect.centerY(), angle,
                reuse);
    }

    public static final Rect getBoundingBoxForRotatatedRectangle(Rect rect, Point centerPoint,
                                                                 float angle, Rect reuse) {
        return getBoundingBoxForRotatatedRectangle(rect, centerPoint.x, centerPoint.y, angle, reuse);
    }

    public static final Rect getBoundingBoxForRotatatedRectangle(Rect rect, int centerX,
                                                                 int centerY, float angle, Rect reuse) {
        if (reuse == null)
            reuse = new Rect();

        double theta = angle * MathConstants.DEG2RAD;
        double sinTheta = Math.sin(theta);
        double cosTheta = Math.cos(theta);
        double dx1 = rect.left - centerX;
        double dy1 = rect.top - centerY;
        double newX1 = centerX - dx1 * cosTheta + dy1 * sinTheta;
        double newY1 = centerY - dx1 * sinTheta - dy1 * cosTheta;
        double dx2 = rect.right - centerX;
        double dy2 = rect.top - centerY;
        double newX2 = centerX - dx2 * cosTheta + dy2 * sinTheta;
        double newY2 = centerY - dx2 * sinTheta - dy2 * cosTheta;
        double dx3 = rect.left - centerX;
        double dy3 = rect.bottom - centerY;
        double newX3 = centerX - dx3 * cosTheta + dy3 * sinTheta;
        double newY3 = centerY - dx3 * sinTheta - dy3 * cosTheta;
        double dx4 = rect.right - centerX;
        double dy4 = rect.bottom - centerY;
        double newX4 = centerX - dx4 * cosTheta + dy4 * sinTheta;
        double newY4 = centerY - dx4 * sinTheta - dy4 * cosTheta;
        reuse.left = MyMath.floorToInt(Min4(newX1, newX2, newX3, newX4));
        reuse.top = MyMath.floorToInt(Min4(newY1, newY2, newY3, newY4));
        reuse.right = MyMath.floorToInt(Max4(newX1, newX2, newX3, newX4));
        reuse.bottom = MyMath.floorToInt(Max4(newY1, newY2, newY3, newY4));

        return reuse;
    }

    private static double Min4(double a, double b, double c, double d) {
        return Math.floor(Math.min(Math.min(a, b), Math.min(c, d)));
    }

    private static double Max4(double a, double b, double c, double d) {
        return Math.ceil(Math.max(Math.max(a, b), Math.max(c, d)));
    }
}
