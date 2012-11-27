package org.osmdroid.views.safecanvas;

import org.osmdroid.views.overlay.Overlay;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * The SafeTranslatedPath class is designed to work in conjunction with {@link SafeTranslatedCanvas}
 * to work around various Android issues with large canvases. For the two classes to work together,
 * call {@link #onDrawCycleStart} at the start of the {@link Overlay#drawSafe} method of your
 * {@link Overlay}. This will set the adjustment needed to draw your Path safely on the canvas
 * without any drawing distortion at high zoom levels. Methods of the {@link Path} class that use
 * unsafe float types have been deprecated in favor of replacement methods that use doubles.
 * 
 * @see {@link ISafeCanvas}
 * 
 * @author Marc Kurtz
 * 
 */
public class SafeTranslatedPath extends Path {

	private final static RectF sRectF = new RectF();

	public int xOffset = 0;
	public int yOffset = 0;

	/**
	 * This method <b>must</b> be called at the start of the {@link Overlay#drawSafe} draw cycle
	 * method. This will adjust the Path to the current state of the {@link ISafeCanvas} passed to
	 * it.
	 */
	public void onDrawCycleStart(ISafeCanvas canvas) {
		// Adjust the current position of the path
		int deltaX = canvas.getXOffset() - xOffset;
		int deltaY = canvas.getYOffset() - yOffset;
		super.offset(deltaX, deltaY);

		// Record the new offset
		xOffset = canvas.getXOffset();
		yOffset = canvas.getYOffset();
	}

	@Override
	public void reset() {
		super.reset();
	}

	@Override
	public void rewind() {
		super.rewind();
	}

	@Override
	public void set(Path src) {
		super.set(src);
	}

	@Override
	public FillType getFillType() {
		return super.getFillType();
	}

	@Override
	public void setFillType(FillType ft) {
		super.setFillType(ft);
	}

	@Override
	public boolean isInverseFillType() {
		return super.isInverseFillType();
	}

	@Override
	public void toggleInverseFillType() {
		super.toggleInverseFillType();
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty();
	}

	/**
	 * @deprecated Use {@link #isRect(Rect)} instead.
	 */
	@Override
	public boolean isRect(RectF rect) {
		// Should we offset here?
		rect.offset(xOffset, yOffset);
		boolean result = super.isRect(rect);
		rect.offset(-xOffset, -yOffset);
		return result;
	}

	/**
	 * @see {@link #isRect(RectF)}
	 */
	public boolean isRect(Rect rect) {
		// Should we offset here?
		rect.offset(xOffset, yOffset);
		boolean result = super.isRect(this.toOffsetRectF(rect, sRectF));
		rect.offset(-xOffset, -yOffset);
		return result;
	}

	/**
	 * @deprecated Use {@link #computeBounds(Rect, boolean)} instead.
	 */
	@Override
	public void computeBounds(RectF bounds, boolean exact) {
		super.computeBounds(bounds, exact);
		bounds.offset(-xOffset, -yOffset);
	}

	/**
	 * @see {@link #computeBounds(RectF, boolean)}
	 */
	public void computeBounds(Rect bounds, boolean exact) {
		super.computeBounds(sRectF, exact);
		bounds.set((int) sRectF.left, (int) sRectF.top, (int) sRectF.right, (int) sRectF.bottom);
		bounds.offset(-xOffset, -yOffset);
	}

	@Override
	public void incReserve(int extraPtCount) {
		super.incReserve(extraPtCount);
	}

	/**
	 * @deprecated Use {@link #moveTo(double, double)} instead.
	 */
	@Override
	public void moveTo(float x, float y) {
		super.moveTo(x + xOffset, y + yOffset);
	}

	/**
	 * @see {@link #moveTo(float, float)}
	 */
	public void moveTo(double x, double y) {
		super.moveTo((float) (x + xOffset), (float) (y + yOffset));
	}

	@Override
	public void rMoveTo(float dx, float dy) {
		super.rMoveTo(dx, dy);
	}

	/**
	 * @deprecated Use {@link #lineTo(double, double)} instead.
	 */
	@Override
	public void lineTo(float x, float y) {
		super.lineTo(x + xOffset, y + yOffset);
	}

	/**
	 * @see {@link #lineTo(float, float)}
	 */
	public void lineTo(double x, double y) {
		super.lineTo((float) (x + xOffset), (float) (y + yOffset));
	}

	@Override
	public void rLineTo(float dx, float dy) {
		super.rLineTo(dx, dy);
	}

	/**
	 * @deprecated Use {@link #quadTo(double, double, double, double)} instead.
	 */
	@Override
	public void quadTo(float x1, float y1, float x2, float y2) {
		super.quadTo(x1 + xOffset, y1 + yOffset, x2 + xOffset, y2 + yOffset);
	}

	/**
	 * @see {@link #quadTo(float, float, float, float)}
	 */
	public void quadTo(double x1, double y1, double x2, double y2) {
		super.quadTo((float) (x1 + xOffset), (float) (y1 + yOffset), (float) (x2 + xOffset),
				(float) (y2 + yOffset));
	}

	@Override
	public void rQuadTo(float dx1, float dy1, float dx2, float dy2) {
		super.rQuadTo(dx1, dy1, dx2, dy2);
	}

	/**
	 * @deprecated Use {@link #cubicTo(double, double, double, double, double, double)} instead.
	 */
	@Override
	public void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {
		super.cubicTo(x1 + xOffset, y1 + yOffset, x2 + xOffset, y2 + yOffset, x3 + xOffset, y3
				+ yOffset);
	}

	/**
	 * @see {@link #cubicTo(float, float, float, float, float, float)}
	 */
	public void cubicTo(double x1, double y1, double x2, double y2, double x3, double y3) {
		super.cubicTo((float) (x1 + xOffset), (float) (y1 + yOffset), (float) (x2 + xOffset),
				(float) (y2 + yOffset), (float) (x3 + xOffset), (float) (y3 + yOffset));
	}

	@Override
	public void rCubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {
		super.rCubicTo(x1, y1, x2, y2, x3, y3);
	}

	/**
	 * @deprecated use {@link #arcTo(Rect, float, float, boolean)}
	 */
	@Override
	public void arcTo(RectF oval, float startAngle, float sweepAngle, boolean forceMoveTo) {
		oval.offset(xOffset, yOffset);
		super.arcTo(oval, startAngle, sweepAngle, forceMoveTo);
		oval.offset(-xOffset, -yOffset);
	}

	/**
	 * @see {@link #arcTo(RectF, float, float, boolean)}
	 */
	public void arcTo(Rect oval, float startAngle, float sweepAngle, boolean forceMoveTo) {
		oval.offset(xOffset, yOffset);
		super.arcTo(this.toOffsetRectF(oval, sRectF), startAngle, sweepAngle, forceMoveTo);
		oval.offset(-xOffset, -yOffset);
	}

	/**
	 * @deprecated use {@link #arcTo(Rect, float, float)}
	 */
	@Override
	public void arcTo(RectF oval, float startAngle, float sweepAngle) {
		oval.offset(xOffset, yOffset);
		super.arcTo(oval, startAngle, sweepAngle);
		oval.offset(-xOffset, -yOffset);
	}

	/**
	 * @see {@link #arcTo(RectF, float, float)}
	 */
	public void arcTo(Rect oval, float startAngle, float sweepAngle) {
		oval.offset(xOffset, yOffset);
		super.arcTo(this.toOffsetRectF(oval, sRectF), startAngle, sweepAngle);
		oval.offset(-xOffset, -yOffset);
	}

	@Override
	public void close() {
		super.close();
	}

	/**
	 * @deprecated use {@link #addRect(Rect, Direction)}
	 */
	@Override
	public void addRect(RectF rect, Direction dir) {
		rect.offset(xOffset, yOffset);
		super.addRect(rect, dir);
		rect.offset(-xOffset, -yOffset);
	}

	/**
	 * @see {@link #addRect(RectF, Direction)}
	 */
	public void addRect(Rect rect, Direction dir) {
		rect.offset(xOffset, yOffset);
		super.addRect(this.toOffsetRectF(rect, sRectF), dir);
		rect.offset(-xOffset, -yOffset);
	}

	/**
	 * @deprecated use {@link #addRect(double, double, double, double, Direction)}
	 */
	@Override
	public void addRect(float left, float top, float right, float bottom, Direction dir) {
		super.addRect(left + xOffset, top + yOffset, right + xOffset, bottom + yOffset, dir);
	}

	/**
	 * @see {@link #addRect(float, float, float, float, Direction)}
	 */
	public void addRect(double left, double top, double right, double bottom, Direction dir) {
		super.addRect((float) (left + xOffset), (float) (top + yOffset), (float) (right + xOffset),
				(float) (bottom + yOffset), dir);
	}

	/**
	 * @deprecated use {@link #addOval(Rect, Direction)
	 */
	@Override
	public void addOval(RectF oval, Direction dir) {
		oval.offset(xOffset, yOffset);
		super.addOval(oval, dir);
		oval.offset(-xOffset, -yOffset);
	}

	/**
	 * @see {@link #addOval(RectF, Direction)
	 */
	public void addOval(Rect oval, Direction dir) {
		oval.offset(xOffset, yOffset);
		super.addOval(this.toOffsetRectF(oval, sRectF), dir);
		oval.offset(-xOffset, -yOffset);
	}

	/**
	 * @deprecated use {@link #addCircle(double, double, double, Direction)}
	 */
	@Override
	public void addCircle(float x, float y, float radius, Direction dir) {
		super.addCircle(x + xOffset, y + yOffset, radius, dir);
	}

	/**
	 * @see {@link #addCircle(float, float, float, Direction)}
	 */
	public void addCircle(double x, double y, float radius, Direction dir) {
		super.addCircle((float) (x + xOffset), (float) (y + yOffset), radius, dir);
	}

	/**
	 * @deprecated use {@link #addArc(Rect, float, float)}
	 */
	@Override
	public void addArc(RectF oval, float startAngle, float sweepAngle) {
		oval.offset(xOffset, yOffset);
		super.addArc(oval, startAngle, sweepAngle);
		oval.offset(-xOffset, -yOffset);
	}

	/**
	 * @see {@link #addArc(RectF, float, float)}
	 */
	public void addArc(Rect oval, float startAngle, float sweepAngle) {
		oval.offset(xOffset, yOffset);
		super.addArc(this.toOffsetRectF(oval, sRectF), startAngle, sweepAngle);
		oval.offset(-xOffset, -yOffset);
	}

	/**
	 * @deprecated use {@link #addRoundRect(Rect, float, float)}
	 */
	@Override
	public void addRoundRect(RectF rect, float rx, float ry, Direction dir) {
		rect.offset(xOffset, yOffset);
		super.addRoundRect(rect, rx, ry, dir);
		rect.offset(-xOffset, -yOffset);
	}

	/**
	 * @see {@link #addRoundRect(RectF, float, float)}
	 */
	public void addRoundRect(Rect rect, float rx, float ry, Direction dir) {
		rect.offset(xOffset, yOffset);
		super.addRoundRect(this.toOffsetRectF(rect, sRectF), rx, ry, dir);
		rect.offset(-xOffset, -yOffset);
	}

	/**
	 * @deprecated use {@link #addRoundRect(Rect, float, Direction)}
	 */
	public void addRoundRect(RectF rect, float[] radii, Direction dir) {
		rect.offset(xOffset, yOffset);
		super.addRoundRect(rect, radii, dir);
		rect.offset(-xOffset, -yOffset);
	}

	/**
	 * @see {@link #addRoundRect(RectF, float, Direction)}
	 */
	public void addRoundRect(Rect rect, float[] radii, Direction dir) {
		rect.offset(xOffset, yOffset);
		super.addRoundRect(this.toOffsetRectF(rect, sRectF), radii, dir);
		rect.offset(-xOffset, -yOffset);
	}

	@Override
	public void addPath(Path src, float dx, float dy) {
		boolean safePath = src instanceof SafeTranslatedPath;
		if (!safePath)
			src.offset(xOffset, yOffset);
		super.addPath(src, dx, dy);
		if (!safePath)
			src.offset(-xOffset, -yOffset);
	}

	@Override
	public void addPath(Path src) {
		boolean safePath = src instanceof SafeTranslatedPath;
		if (!safePath)
			src.offset(xOffset, yOffset);
		super.addPath(src);
		if (!safePath)
			src.offset(-xOffset, -yOffset);
	}

	@Override
	public void addPath(Path src, Matrix matrix) {
		boolean safePath = src instanceof SafeTranslatedPath;
		if (!safePath)
			matrix.preTranslate(xOffset, yOffset);
		super.addPath(src, matrix);
		if (!safePath)
			matrix.preTranslate(-xOffset, -yOffset);
	}

	@Override
	public void offset(float dx, float dy, Path dst) {
		super.offset(dx, dy, dst);
	}

	@Override
	public void offset(float dx, float dy) {
		super.offset(dx, dy);
	}

	/**
	 * @deprecated use {@link #setLastPoint(double, double)}
	 */
	@Override
	public void setLastPoint(float dx, float dy) {
		super.setLastPoint(dx + xOffset, dy + yOffset);
	}

	/**
	 * @see {@link #setLastPoint(float, float)}
	 */
	public void setLastPoint(double dx, double dy) {
		super.setLastPoint((float) (dx + xOffset), (float) (dy + yOffset));
	}

	@Override
	public void transform(Matrix matrix, Path dst) {
		// Should we offset here?
		super.transform(matrix, dst);
	}

	@Override
	public void transform(Matrix matrix) {
		// Should we offset here?
		super.transform(matrix);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	/**
	 * Helper function to convert a Rect to RectF and adjust the values of the Rect by the offsets.
	 */
	protected final RectF toOffsetRectF(Rect rect, RectF reuse) {
		if (reuse == null)
			reuse = new RectF();

		reuse.set(rect.left + xOffset, rect.top + yOffset, rect.right + xOffset, rect.bottom
				+ yOffset);
		return reuse;
	}
}
