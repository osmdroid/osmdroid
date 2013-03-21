package org.osmdroid.views.safecanvas;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Canvas.EdgeType;
import android.graphics.Canvas.VertexMode;
import android.graphics.DrawFilter;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;

/**
 * The ISafeCanvas interface is designed to work Android's issues with large canvases.<br />
 * <br />
 * The internal representation of canvas coordinates in the Skia graphics library is float. Canvas
 * sizes are specified as integers. At high zoom levels, the canvas sizes get to the high end of the
 * integer data type which subsequently get rounded off when represented as floats in Skia. This
 * causes drawing anomalies such as jagged edges or distorted shapes that progressively get worse as
 * the zoom level increases. The issue becomes visibly noticeable around zoom level 18 which is
 * commonly available amongst most online map tile providers.<br />
 * <br />
 * To prevent this issue we can't pass large values to the native Android methods. To accomplish
 * that, we must intercept all values being passed to the canvas draw methods and translate them to
 * a coordinate system where the origin (0,0) is the center of the screen. We then draw them to a
 * canvas that has the same coordinate system. We also prevent passing coordinate parameter values
 * as floats, instead accepting doubles.<br />
 * 
 * @see {@link SafeTranslatedCanvas}, {@link SafeTranslatedPath}, {@link SafePaint}
 * 
 * @author Marc Kurtz
 * 
 */
public interface ISafeCanvas {

	/**
	 * Allows access to the original unsafe canvas.
	 */
	public interface UnsafeCanvasHandler {
		void onUnsafeCanvas(Canvas canvas);
	}

	/**
	 * Gets the x-offset that will be used to adjust all drawing values.
	 */
	public int getXOffset();

	/**
	 * Gets the y-offset that will be used to adjust all drawing values.
	 */
	public int getYOffset();

	/**
	 * Allows access to the original unsafe canvas through an {@link UnsafeCanvasHandler}.
	 */
	public void getUnsafeCanvas(UnsafeCanvasHandler handler);

	/**
	 * Gets the wrapped canvas. This canvas will have a coordinate system where the origin is at the
	 * center of the screen, but will not automatically adjust values passed to its drawing methods.
	 */
	public Canvas getWrappedCanvas();

	/**
	 * Gets this safe canvas as an Android {@link Native} class. This canvas will have a coordinate
	 * system where the origin is at the center of the screen, and will automatically adjust values
	 * passed to its drawing methods by {@link #getXOffset()} and {@link #getYOffset()}.
	 */
	public Canvas getSafeCanvas();

	/**
	 * Specify a bitmap for the canvas to draw into. As a side-effect, also updates the canvas's
	 * target density to match that of the bitmap.
	 * 
	 * @param bitmap
	 *            Specifies a mutable bitmap for the canvas to draw into.
	 * 
	 * @see #setDensity(int)
	 * @see #getDensity()
	 */
	public abstract void setBitmap(Bitmap bitmap);

	/**
	 * Return true if the device that the current layer draws into is opaque (i.e. does not support
	 * per-pixel alpha).
	 * 
	 * @return true if the device that the current layer draws into is opaque
	 */
	public abstract boolean isOpaque();

	/**
	 * Returns the width of the current drawing layer
	 * 
	 * @return the width of the current drawing layer
	 */
	public abstract int getWidth();

	/**
	 * Returns the height of the current drawing layer
	 * 
	 * @return the height of the current drawing layer
	 */
	public abstract int getHeight();

	/**
	 * <p>
	 * Returns the target density of the canvas. The default density is derived from the density of
	 * its backing bitmap, or {@link Bitmap#DENSITY_NONE} if there is not one.
	 * </p>
	 * 
	 * @return Returns the current target density of the canvas, which is used to determine the
	 *         scaling factor when drawing a bitmap into it.
	 * 
	 * @see #setDensity(int)
	 * @see Bitmap#getDensity()
	 */
	public abstract int getDensity();

	/**
	 * <p>
	 * Specifies the density for this Canvas' backing bitmap. This modifies the target density of
	 * the canvas itself, as well as the density of its backing bitmap via
	 * {@link Bitmap#setDensity(int) Bitmap.setDensity(int)}.
	 * 
	 * @param density
	 *            The new target density of the canvas, which is used to determine the scaling
	 *            factor when drawing a bitmap into it. Use {@link Bitmap#DENSITY_NONE} to disable
	 *            bitmap scaling.
	 * 
	 * @see #getDensity()
	 * @see Bitmap#setDensity(int)
	 */
	public abstract void setDensity(int density);

	/**
	 * Saves the current matrix and clip onto a private stack. Subsequent calls to
	 * translate,scale,rotate,skew,concat or clipRect,clipPath will all operate as usual, but when
	 * the balancing call to restore() is made, those calls will be forgotten, and the settings that
	 * existed before the save() will be reinstated.
	 * 
	 * @return The value to pass to restoreToCount() to balance this save()
	 */
	public abstract int save();

	/**
	 * Based on saveFlags, can save the current matrix and clip onto a private stack. Subsequent
	 * calls to translate,scale,rotate,skew,concat or clipRect,clipPath will all operate as usual,
	 * but when the balancing call to restore() is made, those calls will be forgotten, and the
	 * settings that existed before the save() will be reinstated.
	 * 
	 * @param saveFlags
	 *            flag bits that specify which parts of the Canvas state to save/restore
	 * @return The value to pass to restoreToCount() to balance this save()
	 */
	public abstract int save(int saveFlags);

	/**
	 * This behaves the same as save(), but in addition it allocates an offscreen bitmap. All
	 * drawing calls are directed there, and only when the balancing call to restore() is made is
	 * that offscreen transfered to the canvas (or the previous layer). Subsequent calls to
	 * translate, scale, rotate, skew, concat or clipRect, clipPath all operate on this copy. When
	 * the balancing call to restore() is made, this copy is deleted and the previous matrix/clip
	 * state is restored.
	 * 
	 * @param bounds
	 *            May be null. The maximum size the offscreen bitmap needs to be (in local
	 *            coordinates)
	 * @param paint
	 *            This is copied, and is applied to the offscreen when restore() is called.
	 * @param saveFlags
	 *            see _SAVE_FLAG constants
	 * @return value to pass to restoreToCount() to balance this save()
	 */
	public abstract int saveLayer(Rect bounds, SafePaint paint, int saveFlags);

	/**
	 * Helper version of saveLayer() that takes 4 values rather than a RectF.
	 */
	public abstract int saveLayer(double left, double top, double right, double bottom,
			SafePaint paint, int saveFlags);

	/**
	 * This behaves the same as save(), but in addition it allocates an offscreen bitmap. All
	 * drawing calls are directed there, and only when the balancing call to restore() is made is
	 * that offscreen transfered to the canvas (or the previous layer). Subsequent calls to
	 * translate, scale, rotate, skew, concat or clipRect, clipPath all operate on this copy. When
	 * the balancing call to restore() is made, this copy is deleted and the previous matrix/clip
	 * state is restored.
	 * 
	 * @param bounds
	 *            The maximum size the offscreen bitmap needs to be (in local coordinates)
	 * @param alpha
	 *            The alpha to apply to the offscreen when when it is drawn during restore()
	 * @param saveFlags
	 *            see _SAVE_FLAG constants
	 * @return value to pass to restoreToCount() to balance this call
	 */
	public abstract int saveLayerAlpha(Rect bounds, int alpha, int saveFlags);

	/**
	 * Helper for saveLayerAlpha() that takes 4 values instead of a RectF.
	 */
	public abstract int saveLayerAlpha(double left, double top, double right, double bottom,
			int alpha, int saveFlags);

	/**
	 * This call balances a previous call to save(), and is used to remove all modifications to the
	 * matrix/clip state since the last save call. It is an error to call restore() more times than
	 * save() was called.
	 */
	public abstract void restore();

	/**
	 * Returns the number of matrix/clip states on the Canvas' private stack. This will equal #
	 * save() calls - # restore() calls.
	 */
	public abstract int getSaveCount();

	/**
	 * Efficient way to pop any calls to save() that happened after the save count reached
	 * saveCount. It is an error for saveCount to be less than 1.
	 * 
	 * Example: int count = canvas.save(); ... // more calls potentially to save()
	 * canvas.restoreToCount(count); // now the canvas is back in the same state it was before the
	 * initial // call to save().
	 * 
	 * @param saveCount
	 *            The save level to restore to.
	 */
	public abstract void restoreToCount(int saveCount);

	/**
	 * Preconcat the current matrix with the specified translation
	 * 
	 * @param dx
	 *            The distance to translate in X
	 * @param dy
	 *            The distance to translate in Y
	 */
	public abstract void translate(float dx, float dy);

	/**
	 * Preconcat the current matrix with the specified scale.
	 * 
	 * @param sx
	 *            The amount to scale in X
	 * @param sy
	 *            The amount to scale in Y
	 */
	public abstract void scale(float sx, float sy);

	/**
	 * Preconcat the current matrix with the specified scale.
	 * 
	 * @param sx
	 *            The amount to scale in X
	 * @param sy
	 *            The amount to scale in Y
	 * @param px
	 *            The x-coord for the pivot point (unchanged by the scale)
	 * @param py
	 *            The y-coord for the pivot point (unchanged by the scale)
	 */
	public abstract void scale(float sx, float sy, double px, double py);

	/**
	 * Preconcat the current matrix with the specified rotation.
	 * 
	 * @param degrees
	 *            The amount to rotate, in degrees
	 */
	public abstract void rotate(float degrees);

	/**
	 * Preconcat the current matrix with the specified rotation.
	 * 
	 * @param degrees
	 *            The amount to rotate, in degrees
	 * @param px
	 *            The x-coord for the pivot point (unchanged by the rotation)
	 * @param py
	 *            The y-coord for the pivot point (unchanged by the rotation)
	 */
	public abstract void rotate(float degrees, double px, double py);

	/**
	 * Preconcat the current matrix with the specified skew.
	 * 
	 * @param sx
	 *            The amount to skew in X
	 * @param sy
	 *            The amount to skew in Y
	 */
	public abstract void skew(float sx, float sy);

	/**
	 * Preconcat the current matrix with the specified matrix.
	 * 
	 * @param matrix
	 *            The matrix to preconcatenate with the current matrix
	 */
	public abstract void concat(Matrix matrix);

	/**
	 * Completely replace the current matrix with the specified matrix. If the matrix parameter is
	 * null, then the current matrix is reset to identity.
	 * 
	 * @param matrix
	 *            The matrix to replace the current matrix with. If it is null, set the current
	 *            matrix to identity.
	 */
	public abstract void setMatrix(Matrix matrix);

	/**
	 * Return, in ctm, the current transformation matrix. This does not alter the matrix in the
	 * canvas, but just returns a copy of it.
	 */
	public abstract void getMatrix(Matrix ctm);

	/**
	 * Return a new matrix with a copy of the canvas' current transformation matrix.
	 */
	public abstract Matrix getMatrix();

	/**
	 * Modify the current clip with the specified rectangle, which is expressed in local
	 * coordinates.
	 * 
	 * @param rect
	 *            The rectangle to intersect with the current clip.
	 * @param op
	 *            How the clip is modified
	 * @return true if the resulting clip is non-empty
	 */
	public abstract boolean clipRect(Rect rect, Region.Op op);

	/**
	 * Intersect the current clip with the specified rectangle, which is expressed in local
	 * coordinates.
	 * 
	 * @param rect
	 *            The rectangle to intersect with the current clip.
	 * @return true if the resulting clip is non-empty
	 */
	public abstract boolean clipRect(Rect rect);

	/**
	 * Modify the current clip with the specified rectangle, which is expressed in local
	 * coordinates.
	 * 
	 * @param left
	 *            The left side of the rectangle to intersect with the current clip
	 * @param top
	 *            The top of the rectangle to intersect with the current clip
	 * @param right
	 *            The right side of the rectangle to intersect with the current clip
	 * @param bottom
	 *            The bottom of the rectangle to intersect with the current clip
	 * @param op
	 *            How the clip is modified
	 * @return true if the resulting clip is non-empty
	 */
	public abstract boolean clipRect(double left, double top, double right, double bottom,
			Region.Op op);

	/**
	 * Intersect the current clip with the specified rectangle, which is expressed in local
	 * coordinates.
	 * 
	 * @param left
	 *            The left side of the rectangle to intersect with the current clip
	 * @param top
	 *            The top of the rectangle to intersect with the current clip
	 * @param right
	 *            The right side of the rectangle to intersect with the current clip
	 * @param bottom
	 *            The bottom of the rectangle to intersect with the current clip
	 * @return true if the resulting clip is non-empty
	 */
	public abstract boolean clipRect(double left, double top, double right, double bottom);

	/**
	 * Intersect the current clip with the specified rectangle, which is expressed in local
	 * coordinates.
	 * 
	 * @param left
	 *            The left side of the rectangle to intersect with the current clip
	 * @param top
	 *            The top of the rectangle to intersect with the current clip
	 * @param right
	 *            The right side of the rectangle to intersect with the current clip
	 * @param bottom
	 *            The bottom of the rectangle to intersect with the current clip
	 * @return true if the resulting clip is non-empty
	 */
	public abstract boolean clipRect(int left, int top, int right, int bottom);

	/**
	 * Modify the current clip with the specified path.
	 * 
	 * @param path
	 *            The path to operate on the current clip
	 * @param op
	 *            How the clip is modified
	 * @return true if the resulting is non-empty
	 */
	public abstract boolean clipPath(SafeTranslatedPath path, Region.Op op);

	/**
	 * Intersect the current clip with the specified path.
	 * 
	 * @param path
	 *            The path to intersect with the current clip
	 * @return true if the resulting is non-empty
	 */
	public abstract boolean clipPath(SafeTranslatedPath path);

	/**
	 * Modify the current clip with the specified region. Note that unlike clipRect() and clipPath()
	 * which transform their arguments by the current matrix, clipRegion() assumes its argument is
	 * already in the coordinate system of the current layer's bitmap, and so not transformation is
	 * performed.
	 * 
	 * @param region
	 *            The region to operate on the current clip, based on op
	 * @param op
	 *            How the clip is modified
	 * @return true if the resulting is non-empty
	 */
	public abstract boolean clipRegion(Region region, Region.Op op);

	/**
	 * Intersect the current clip with the specified region. Note that unlike clipRect() and
	 * clipPath() which transform their arguments by the current matrix, clipRegion() assumes its
	 * argument is already in the coordinate system of the current layer's bitmap, and so not
	 * transformation is performed.
	 * 
	 * @param region
	 *            The region to operate on the current clip, based on op
	 * @return true if the resulting is non-empty
	 */
	public abstract boolean clipRegion(Region region);

	public abstract DrawFilter getDrawFilter();

	public abstract void setDrawFilter(DrawFilter filter);

	/**
	 * Return true if the specified rectangle, after being transformed by the current matrix, would
	 * lie completely outside of the current clip. Call this to check if an area you intend to draw
	 * into is clipped out (and therefore you can skip making the draw calls).
	 * 
	 * @param rect
	 *            the rect to compare with the current clip
	 * @param type
	 *            specifies how to treat the edges (BW or antialiased)
	 * @return true if the rect (transformed by the canvas' matrix) does not intersect with the
	 *         canvas' clip
	 */
	public abstract boolean quickReject(Rect rect, EdgeType type);

	/**
	 * Return true if the specified path, after being transformed by the current matrix, would lie
	 * completely outside of the current clip. Call this to check if an area you intend to draw into
	 * is clipped out (and therefore you can skip making the draw calls). Note: for speed it may
	 * return false even if the path itself might not intersect the clip (i.e. the bounds of the
	 * path intersects, but the path does not).
	 * 
	 * @param path
	 *            The path to compare with the current clip
	 * @param type
	 *            true if the path should be considered antialiased, since that means it may affect
	 *            a larger area (more pixels) than non-antialiased.
	 * @return true if the path (transformed by the canvas' matrix) does not intersect with the
	 *         canvas' clip
	 */
	public abstract boolean quickReject(SafeTranslatedPath path, EdgeType type);

	/**
	 * Return true if the specified rectangle, after being transformed by the current matrix, would
	 * lie completely outside of the current clip. Call this to check if an area you intend to draw
	 * into is clipped out (and therefore you can skip making the draw calls).
	 * 
	 * @param left
	 *            The left side of the rectangle to compare with the current clip
	 * @param top
	 *            The top of the rectangle to compare with the current clip
	 * @param right
	 *            The right side of the rectangle to compare with the current clip
	 * @param bottom
	 *            The bottom of the rectangle to compare with the current clip
	 * @param type
	 *            true if the rect should be considered antialiased, since that means it may affect
	 *            a larger area (more pixels) than non-antialiased.
	 * @return true if the rect (transformed by the canvas' matrix) does not intersect with the
	 *         canvas' clip
	 */
	public abstract boolean quickReject(double left, double top, double right, double bottom,
			EdgeType type);

	/**
	 * Retrieve the clip bounds, returning true if they are non-empty.
	 * 
	 * @param bounds
	 *            Return the clip bounds here. If it is null, ignore it but still return true if the
	 *            current clip is non-empty.
	 * @return true if the current clip is non-empty.
	 */
	public abstract boolean getClipBounds(Rect bounds);

	/**
	 * Retrieve the clip bounds.
	 * 
	 * @return the clip bounds, or [0, 0, 0, 0] if the clip is empty.
	 */
	public abstract Rect getClipBounds();

	/**
	 * Fill the entire canvas' bitmap (restricted to the current clip) with the specified RGB color,
	 * using srcover porterduff mode.
	 * 
	 * @param r
	 *            red component (0..255) of the color to draw onto the canvas
	 * @param g
	 *            green component (0..255) of the color to draw onto the canvas
	 * @param b
	 *            blue component (0..255) of the color to draw onto the canvas
	 */
	public abstract void drawRGB(int r, int g, int b);

	/**
	 * Fill the entire canvas' bitmap (restricted to the current clip) with the specified ARGB
	 * color, using srcover porterduff mode.
	 * 
	 * @param a
	 *            alpha component (0..255) of the color to draw onto the canvas
	 * @param r
	 *            red component (0..255) of the color to draw onto the canvas
	 * @param g
	 *            green component (0..255) of the color to draw onto the canvas
	 * @param b
	 *            blue component (0..255) of the color to draw onto the canvas
	 */
	public abstract void drawARGB(int a, int r, int g, int b);

	/**
	 * Fill the entire canvas' bitmap (restricted to the current clip) with the specified color,
	 * using srcover porterduff mode.
	 * 
	 * @param color
	 *            the color to draw onto the canvas
	 */
	public abstract void drawColor(int color);

	/**
	 * Fill the entire canvas' bitmap (restricted to the current clip) with the specified color and
	 * porter-duff xfermode.
	 * 
	 * @param color
	 *            the color to draw with
	 * @param mode
	 *            the porter-duff mode to apply to the color
	 */
	public abstract void drawColor(int color, PorterDuff.Mode mode);

	/**
	 * Fill the entire canvas' bitmap (restricted to the current clip) with the specified paint.
	 * This is equivalent (but faster) to drawing an infinitely large rectangle with the specified
	 * paint.
	 * 
	 * @param paint
	 *            The paint used to draw onto the canvas
	 */
	public abstract void drawPaint(SafePaint paint);

	/**
	 * Draw a series of points. Each point is centered at the coordinate specified by pts[], and its
	 * diameter is specified by the paint's stroke width (as transformed by the canvas' CTM), with
	 * special treatment for a stroke width of 0, which always draws exactly 1 pixel (or at most 4
	 * if antialiasing is enabled). The shape of the point is controlled by the paint's Cap type.
	 * The shape is a square, unless the cap type is Round, in which case the shape is a circle.
	 * 
	 * @param pts
	 *            Array of points to draw [x0 y0 x1 y1 x2 y2 ...]
	 * @param offset
	 *            Number of values to skip before starting to draw.
	 * @param count
	 *            The number of values to process, after skipping offset of them. Since one point
	 *            uses two values, the number of "points" that are drawn is really (count >> 1).
	 * @param paint
	 *            The paint used to draw the points
	 */
	public abstract void drawPoints(double[] pts, int offset, int count, SafePaint paint);

	/**
	 * Helper for drawPoints() that assumes you want to draw the entire array
	 */
	public abstract void drawPoints(double[] pts, SafePaint paint);

	/**
	 * Helper for drawPoints() for drawing a single point.
	 */
	public abstract void drawPoint(double x, double y, SafePaint paint);

	/**
	 * Draw a line segment with the specified start and stop x,y coordinates, using the specified
	 * paint. NOTE: since a line is always "framed", the Style is ignored in the paint.
	 * 
	 * @param startX
	 *            The x-coordinate of the start point of the line
	 * @param startY
	 *            The y-coordinate of the start point of the line
	 * @param paint
	 *            The paint used to draw the line
	 */
	public abstract void drawLine(double startX, double startY, double stopX, double stopY,
			SafePaint paint);

	/**
	 * Draw a series of lines. Each line is taken from 4 consecutive values in the pts array. Thus
	 * to draw 1 line, the array must contain at least 4 values. This is logically the same as
	 * drawing the array as follows: drawLine(pts[0], pts[1], pts[2], pts[3]) followed by
	 * drawLine(pts[4], pts[5], pts[6], pts[7]) and so on.
	 * 
	 * @param pts
	 *            Array of points to draw [x0 y0 x1 y1 x2 y2 ...]
	 * @param offset
	 *            Number of values in the array to skip before drawing.
	 * @param count
	 *            The number of values in the array to process, after skipping "offset" of them.
	 *            Since each line uses 4 values, the number of "lines" that are drawn is really
	 *            (count >> 2).
	 * @param paint
	 *            The paint used to draw the points
	 */
	public abstract void drawLines(double[] pts, int offset, int count, SafePaint paint);

	public abstract void drawLines(double[] pts, SafePaint paint);

	/**
	 * Draw the specified Rect using the specified Paint. The rectangle will be filled or framed
	 * based on the Style in the paint.
	 * 
	 * @param r
	 *            The rectangle to be drawn.
	 * @param paint
	 *            The paint used to draw the rectangle
	 */
	public abstract void drawRect(Rect r, SafePaint paint);

	/**
	 * Draw the specified Rect using the specified paint. The rectangle will be filled or framed
	 * based on the Style in the paint.
	 * 
	 * @param left
	 *            The left side of the rectangle to be drawn
	 * @param top
	 *            The top side of the rectangle to be drawn
	 * @param right
	 *            The right side of the rectangle to be drawn
	 * @param bottom
	 *            The bottom side of the rectangle to be drawn
	 * @param paint
	 *            The paint used to draw the rect
	 */
	public abstract void drawRect(double left, double top, double right, double bottom,
			SafePaint paint);

	/**
	 * Draw the specified oval using the specified paint. The oval will be filled or framed based on
	 * the Style in the paint.
	 * 
	 * @param oval
	 *            The rectangle bounds of the oval to be drawn
	 */
	public abstract void drawOval(Rect oval, SafePaint paint);

	/**
	 * Draw the specified circle using the specified paint. If radius is <= 0, then nothing will be
	 * drawn. The circle will be filled or framed based on the Style in the paint.
	 * 
	 * @param cx
	 *            The x-coordinate of the center of the cirle to be drawn
	 * @param cy
	 *            The y-coordinate of the center of the cirle to be drawn
	 * @param radius
	 *            The radius of the cirle to be drawn
	 * @param paint
	 *            The paint used to draw the circle
	 */
	public abstract void drawCircle(double cx, double cy, float radius, SafePaint paint);

	/**
	 * <p>
	 * Draw the specified arc, which will be scaled to fit inside the specified oval.
	 * </p>
	 * 
	 * <p>
	 * If the start angle is negative or >= 360, the start angle is treated as start angle modulo
	 * 360.
	 * </p>
	 * 
	 * <p>
	 * If the sweep angle is >= 360, then the oval is drawn completely. Note that this differs
	 * slightly from SkPath::arcTo, which treats the sweep angle modulo 360. If the sweep angle is
	 * negative, the sweep angle is treated as sweep angle modulo 360
	 * </p>
	 * 
	 * <p>
	 * The arc is drawn clockwise. An angle of 0 degrees correspond to the geometric angle of 0
	 * degrees (3 o'clock on a watch.)
	 * </p>
	 * 
	 * @param oval
	 *            The bounds of oval used to define the shape and size of the arc
	 * @param startAngle
	 *            Starting angle (in degrees) where the arc begins
	 * @param sweepAngle
	 *            Sweep angle (in degrees) measured clockwise
	 * @param useCenter
	 *            If true, include the center of the oval in the arc, and close it if it is being
	 *            stroked. This will draw a wedge
	 * @param paint
	 *            The paint used to draw the arc
	 */
	public abstract void drawArc(Rect oval, float startAngle, float sweepAngle, boolean useCenter,
			SafePaint paint);

	/**
	 * Draw the specified round-rect using the specified paint. The roundrect will be filled or
	 * framed based on the Style in the paint.
	 * 
	 * @param rect
	 *            The rectangular bounds of the roundRect to be drawn
	 * @param rx
	 *            The x-radius of the oval used to round the corners
	 * @param ry
	 *            The y-radius of the oval used to round the corners
	 * @param paint
	 *            The paint used to draw the roundRect
	 */
	public abstract void drawRoundRect(Rect rect, float rx, float ry, SafePaint paint);

	/**
	 * Draw the specified path using the specified paint. The path will be filled or framed based on
	 * the Style in the paint.
	 * 
	 * @param path
	 *            The path to be drawn
	 * @param paint
	 *            The paint used to draw the path
	 */
	public abstract void drawPath(SafeTranslatedPath path, SafePaint paint);

	/**
	 * Draw the specified bitmap, with its top/left corner at (x,y), using the specified paint,
	 * transformed by the current matrix.
	 * 
	 * <p>
	 * Note: if the paint contains a maskfilter that generates a mask which extends beyond the
	 * bitmap's original width/height (e.g. BlurMaskFilter), then the bitmap will be drawn as if it
	 * were in a Shader with CLAMP mode. Thus the color outside of the original width/height will be
	 * the edge color replicated.
	 * 
	 * <p>
	 * If the bitmap and canvas have different densities, this function will take care of
	 * automatically scaling the bitmap to draw at the same density as the canvas.
	 * 
	 * @param bitmap
	 *            The bitmap to be drawn
	 * @param left
	 *            The position of the left side of the bitmap being drawn
	 * @param top
	 *            The position of the top side of the bitmap being drawn
	 * @param paint
	 *            The paint used to draw the bitmap (may be null)
	 */
	public abstract void drawBitmap(Bitmap bitmap, double left, double top, SafePaint paint);

	/**
	 * Draw the specified bitmap, scaling/translating automatically to fill the destination
	 * rectangle. If the source rectangle is not null, it specifies the subset of the bitmap to
	 * draw.
	 * 
	 * <p>
	 * Note: if the paint contains a maskfilter that generates a mask which extends beyond the
	 * bitmap's original width/height (e.g. BlurMaskFilter), then the bitmap will be drawn as if it
	 * were in a Shader with CLAMP mode. Thus the color outside of the original width/height will be
	 * the edge color replicated.
	 * 
	 * <p>
	 * This function <em>ignores the density associated with the bitmap</em>. This is because the
	 * source and destination rectangle coordinate spaces are in their respective densities, so must
	 * already have the appropriate scaling factor applied.
	 * 
	 * @param bitmap
	 *            The bitmap to be drawn
	 * @param src
	 *            May be null. The subset of the bitmap to be drawn
	 * @param dst
	 *            The rectangle that the bitmap will be scaled/translated to fit into
	 * @param paint
	 *            May be null. The paint used to draw the bitmap
	 */
	public abstract void drawBitmap(Bitmap bitmap, Rect src, Rect dst, SafePaint paint);

	/**
	 * Treat the specified array of colors as a bitmap, and draw it. This gives the same result as
	 * first creating a bitmap from the array, and then drawing it, but this method avoids
	 * explicitly creating a bitmap object which can be more efficient if the colors are changing
	 * often.
	 * 
	 * @param colors
	 *            Array of colors representing the pixels of the bitmap
	 * @param offset
	 *            Offset into the array of colors for the first pixel
	 * @param stride
	 *            The number of colors in the array between rows (must be >= width or <= -width).
	 * @param x
	 *            The X coordinate for where to draw the bitmap
	 * @param y
	 *            The Y coordinate for where to draw the bitmap
	 * @param width
	 *            The width of the bitmap
	 * @param height
	 *            The height of the bitmap
	 * @param hasAlpha
	 *            True if the alpha channel of the colors contains valid values. If false, the alpha
	 *            byte is ignored (assumed to be 0xFF for every pixel).
	 * @param paint
	 *            May be null. The paint used to draw the bitmap
	 */
	public abstract void drawBitmap(int[] colors, int offset, int stride, double x, double y,
			int width, int height, boolean hasAlpha, SafePaint paint);

	/**
	 * Legacy version of drawBitmap(int[] colors, ...) that took ints for x,y
	 */
	public abstract void drawBitmap(int[] colors, int offset, int stride, int x, int y, int width,
			int height, boolean hasAlpha, SafePaint paint);

	/**
	 * Draw the bitmap using the specified matrix.
	 * 
	 * @param bitmap
	 *            The bitmap to draw
	 * @param matrix
	 *            The matrix used to transform the bitmap when it is drawn
	 * @param paint
	 *            May be null. The paint used to draw the bitmap
	 */
	public abstract void drawBitmap(Bitmap bitmap, Matrix matrix, SafePaint paint);

	/**
	 * Draw the bitmap through the mesh, where mesh vertices are evenly distributed across the
	 * bitmap. There are meshWidth+1 vertices across, and meshHeight+1 vertices down. The verts
	 * array is accessed in row-major order, so that the first meshWidth+1 vertices are distributed
	 * across the top of the bitmap from left to right. A more general version of this methid is
	 * drawVertices().
	 * 
	 * @param bitmap
	 *            The bitmap to draw using the mesh
	 * @param meshWidth
	 *            The number of columns in the mesh. Nothing is drawn if this is 0
	 * @param meshHeight
	 *            The number of rows in the mesh. Nothing is drawn if this is 0
	 * @param verts
	 *            Array of x,y pairs, specifying where the mesh should be drawn. There must be at
	 *            least (meshWidth+1) * (meshHeight+1) * 2 + meshOffset values in the array
	 * @param vertOffset
	 *            Number of verts elements to skip before drawing
	 * @param colors
	 *            May be null. Specifies a color at each vertex, which is interpolated across the
	 *            cell, and whose values are multiplied by the corresponding bitmap colors. If not
	 *            null, there must be at least (meshWidth+1) * (meshHeight+1) + colorOffset values
	 *            in the array.
	 * @param colorOffset
	 *            Number of color elements to skip before drawing
	 * @param paint
	 *            May be null. The paint used to draw the bitmap
	 */
	public abstract void drawBitmapMesh(Bitmap bitmap, int meshWidth, int meshHeight,
			double[] verts, int vertOffset, int[] colors, int colorOffset, SafePaint paint);

	/**
	 * Draw the array of vertices, interpreted as triangles (based on mode). The verts array is
	 * required, and specifies the x,y pairs for each vertex. If texs is non-null, then it is used
	 * to specify the coordinate in shader coordinates to use at each vertex (the paint must have a
	 * shader in this case). If there is no texs array, but there is a color array, then each color
	 * is interpolated across its corresponding triangle in a gradient. If both texs and colors
	 * arrays are present, then they behave as before, but the resulting color at each pixels is the
	 * result of multiplying the colors from the shader and the color-gradient together. The indices
	 * array is optional, but if it is present, then it is used to specify the index of each
	 * triangle, rather than just walking through the arrays in order.
	 * 
	 * @param mode
	 *            How to interpret the array of vertices
	 * @param vertexCount
	 *            The number of values in the vertices array (and corresponding texs and colors
	 *            arrays if non-null). Each logical vertex is two values (x, y), vertexCount must be
	 *            a multiple of 2.
	 * @param verts
	 *            Array of vertices for the mesh
	 * @param vertOffset
	 *            Number of values in the verts to skip before drawing.
	 * @param texs
	 *            May be null. If not null, specifies the coordinates to sample into the current
	 *            shader (e.g. bitmap tile or gradient)
	 * @param texOffset
	 *            Number of values in texs to skip before drawing.
	 * @param colors
	 *            May be null. If not null, specifies a color for each vertex, to be interpolated
	 *            across the triangle.
	 * @param colorOffset
	 *            Number of values in colors to skip before drawing.
	 * @param indices
	 *            If not null, array of indices to reference into the vertex (texs, colors) array.
	 * @param indexCount
	 *            number of entries in the indices array (if not null).
	 * @param paint
	 *            Specifies the shader to use if the texs array is non-null.
	 */
	public abstract void drawVertices(VertexMode mode, int vertexCount, double[] verts,
			int vertOffset, float[] texs, int texOffset, int[] colors, int colorOffset,
			short[] indices, int indexOffset, int indexCount, SafePaint paint);

	/**
	 * Draw the text, with origin at (x,y), using the specified paint. The origin is interpreted
	 * based on the Align setting in the paint.
	 * 
	 * @param text
	 *            The text to be drawn
	 * @param x
	 *            The x-coordinate of the origin of the text being drawn
	 * @param y
	 * @param paint
	 *            The paint used for the text (e.g. color, size, style)
	 */
	public abstract void drawText(char[] text, int index, int count, double x, double y,
			SafePaint paint);

	/**
	 * Draw the text, with origin at (x,y), using the specified paint. The origin is interpreted
	 * based on the Align setting in the paint.
	 * 
	 * @param text
	 *            The text to be drawn
	 * @param x
	 *            The x-coordinate of the origin of the text being drawn
	 * @param y
	 *            The y-coordinate of the origin of the text being drawn
	 * @param paint
	 *            The paint used for the text (e.g. color, size, style)
	 */
	public abstract void drawText(String text, double x, double y, SafePaint paint);

	/**
	 * Draw the text, with origin at (x,y), using the specified paint. The origin is interpreted
	 * based on the Align setting in the paint.
	 * 
	 * @param text
	 *            The text to be drawn
	 * @param start
	 *            The index of the first character in text to draw
	 * @param end
	 *            (end - 1) is the index of the last character in text to draw
	 * @param x
	 *            The x-coordinate of the origin of the text being drawn
	 * @param y
	 *            The y-coordinate of the origin of the text being drawn
	 * @param paint
	 *            The paint used for the text (e.g. color, size, style)
	 */
	public abstract void drawText(String text, int start, int end, double x, double y,
			SafePaint paint);

	/**
	 * Draw the specified range of text, specified by start/end, with its origin at (x,y), in the
	 * specified Paint. The origin is interpreted based on the Align setting in the Paint.
	 * 
	 * @param text
	 *            The text to be drawn
	 * @param start
	 *            The index of the first character in text to draw
	 * @param end
	 *            (end - 1) is the index of the last character in text to draw
	 * @param x
	 *            The x-coordinate of origin for where to draw the text
	 * @param y
	 *            The y-coordinate of origin for where to draw the text
	 * @param paint
	 *            The paint used for the text (e.g. color, size, style)
	 */
	public abstract void drawText(CharSequence text, int start, int end, double x, double y,
			SafePaint paint);

	/**
	 * Draw the text in the array, with each character's origin specified by the pos array.
	 * 
	 * @param text
	 *            The text to be drawn
	 * @param index
	 *            The index of the first character to draw
	 * @param count
	 *            The number of characters to draw, starting from index.
	 * @param pos
	 *            Array of [x,y] positions, used to position each character
	 * @param paint
	 *            The paint used for the text (e.g. color, size, style)
	 */
	public abstract void drawPosText(char[] text, int index, int count, double[] pos,
			SafePaint paint);

	/**
	 * Draw the text in the array, with each character's origin specified by the pos array.
	 * 
	 * @param text
	 *            The text to be drawn
	 * @param pos
	 *            Array of [x,y] positions, used to position each character
	 * @param paint
	 *            The paint used for the text (e.g. color, size, style)
	 */
	public abstract void drawPosText(String text, double[] pos, SafePaint paint);

	/**
	 * Draw the text, with origin at (x,y), using the specified paint, along the specified path. The
	 * paint's Align setting determins where along the path to start the text.
	 * 
	 * @param text
	 *            The text to be drawn
	 * @param path
	 *            The path the text should follow for its baseline
	 * @param hOffset
	 *            The distance along the path to add to the text's starting position
	 * @param vOffset
	 *            The distance above(-) or below(+) the path to position the text
	 * @param paint
	 *            The paint used for the text (e.g. color, size, style)
	 */
	public abstract void drawTextOnPath(char[] text, int index, int count, SafeTranslatedPath path,
			float hOffset, float vOffset, SafePaint paint);

	/**
	 * Draw the text, with origin at (x,y), using the specified paint, along the specified path. The
	 * paint's Align setting determins where along the path to start the text.
	 * 
	 * @param text
	 *            The text to be drawn
	 * @param path
	 *            The path the text should follow for its baseline
	 * @param hOffset
	 *            The distance along the path to add to the text's starting position
	 * @param vOffset
	 *            The distance above(-) or below(+) the path to position the text
	 * @param paint
	 *            The paint used for the text (e.g. color, size, style)
	 */
	public abstract void drawTextOnPath(String text, SafeTranslatedPath path, float hOffset,
			float vOffset,
 SafePaint paint);

	/**
	 * Save the canvas state, draw the picture, and restore the canvas state. This differs from
	 * picture.draw(canvas), which does not perform any save/restore.
	 * 
	 * @param picture
	 *            The picture to be drawn
	 */
	public abstract void drawPicture(Picture picture);

	/**
	 * Draw the picture, stretched to fit into the dst rectangle.
	 */
	public abstract void drawPicture(Picture picture, Rect dst);

}