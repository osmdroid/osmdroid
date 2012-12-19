package org.osmdroid.views.safecanvas;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Region.Op;

/**
 * An implementation of {@link ISafeCanvas} that wraps a {@link Canvas} and adjusts drawing calls to
 * the wrapped Canvas so that they are relative to an origin that is always at the center of the
 * screen.<br />
 * <br />
 * See {@link ISafeCanvas} for details<br />
 * 
 * @author Marc Kurtz
 * 
 */
public class SafeTranslatedCanvas extends Canvas implements ISafeCanvas {
	private final static Matrix sMatrix = new Matrix();
	private final static RectF sRectF = new RectF();
	private static float[] sFloatAry = new float[0];
	private Canvas mCanvas;
	private final Matrix mMatrix = new Matrix();
	public int xOffset;
	public int yOffset;

	public SafeTranslatedCanvas() {
		//
	}

	public SafeTranslatedCanvas(Canvas canvas) {
		this.setCanvas(canvas);
	}

	@Override
	public Canvas getSafeCanvas() {
		return this;
	}

	@Override
	public int getXOffset() {
		return xOffset;
	}

	@Override
	public int getYOffset() {
		return yOffset;
	}

	public void setCanvas(Canvas canvas) {
		mCanvas = canvas;
		canvas.getMatrix(mMatrix);
	}

	public Canvas getWrappedCanvas() {
		return mCanvas;
	}

	public Matrix getOriginalMatrix() {
		return mMatrix;
	}

	@Override
	public boolean clipPath(SafeTranslatedPath path, Op op) {
		return getWrappedCanvas().clipPath(path, op);
	}

	@Override
	public boolean clipPath(SafeTranslatedPath path) {
		return getWrappedCanvas().clipPath(path);
	}

	@Override
	public boolean clipRect(double left, double top, double right, double bottom, Op op) {
		return getWrappedCanvas().clipRect((float) (left + xOffset), (float) (top + yOffset),
				(float) (right + xOffset), (float) (bottom + yOffset), op);
	}

	@Override
	public boolean clipRect(double left, double top, double right, double bottom) {
		return getWrappedCanvas().clipRect((float) (left + xOffset), (float) (top + yOffset),
				(float) (right + xOffset), (float) (bottom + yOffset));
	}

	@Override
	public boolean clipRect(int left, int top, int right, int bottom) {
		return getWrappedCanvas().clipRect(left + xOffset, top + yOffset, right + xOffset,
				bottom + yOffset);
	}

	@Override
	public boolean clipRect(Rect rect, Op op) {
		rect.offset(xOffset, yOffset);
		return getWrappedCanvas().clipRect(rect, op);
	}

	@Override
	public boolean clipRect(Rect rect) {
		rect.offset(xOffset, yOffset);
		return getWrappedCanvas().clipRect(rect);
	}

	@Override
	public boolean clipRegion(Region region, Op op) {
		region.translate(xOffset, yOffset);
		return getWrappedCanvas().clipRegion(region, op);
	}

	@Override
	public boolean clipRegion(Region region) {
		region.translate(xOffset, yOffset);
		return getWrappedCanvas().clipRegion(region);
	}

	@Override
	public void concat(Matrix matrix) {
		getWrappedCanvas().concat(matrix);
	}

	@Override
	public void drawARGB(int a, int r, int g, int b) {
		getWrappedCanvas().drawARGB(a, r, g, b);
	}

	@Override
	public void drawArc(Rect oval, float startAngle, float sweepAngle, boolean useCenter,
			SafePaint paint) {
		getWrappedCanvas().drawArc(this.toOffsetRectF(oval, sRectF), startAngle, sweepAngle,
				useCenter, paint);
	}

	@Override
	public void drawBitmap(Bitmap bitmap, double left, double top, SafePaint paint) {
		getWrappedCanvas().drawBitmap(bitmap, (float) (left + xOffset), (float) (top + yOffset),
				paint);
	}

	@Override
	public void drawBitmap(Bitmap bitmap, Matrix matrix, SafePaint paint) {
		sMatrix.set(matrix);
		sMatrix.postTranslate(xOffset, yOffset);
		getWrappedCanvas().drawBitmap(bitmap, sMatrix, paint);
	}

	@Override
	public void drawBitmap(Bitmap bitmap, Rect src, Rect dst, SafePaint paint) {
		dst.offset(xOffset, yOffset);
		getWrappedCanvas().drawBitmap(bitmap, src, dst, paint);
		dst.offset(-xOffset, -yOffset);
	}

	/* This is used by Drawable.draw(Canvas), so also we adjust here */
	@Override
	public void drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint) {
		dst.offset(xOffset, yOffset);
		getWrappedCanvas().drawBitmap(bitmap, src, dst, paint);
		dst.offset(-xOffset, -yOffset);
	}

	@Override
	public void drawBitmap(int[] colors, int offset, int stride, double x, double y, int width,
			int height, boolean hasAlpha, SafePaint paint) {
		getWrappedCanvas().drawBitmap(colors, offset, stride, (float) (x + xOffset),
				(float) (y + yOffset), width, height, hasAlpha, paint);
	}

	@Override
	public void drawBitmap(int[] colors, int offset, int stride, int x, int y, int width,
			int height, boolean hasAlpha, SafePaint paint) {
		getWrappedCanvas().drawBitmap(colors, offset, stride, x + offset, y + offset, width,
				height, hasAlpha, paint);
	}

	@Override
	public void drawBitmapMesh(Bitmap bitmap, int meshWidth, int meshHeight, double[] verts,
			int vertOffset, int[] colors, int colorOffset, SafePaint paint) {
		getWrappedCanvas().drawBitmapMesh(bitmap, meshWidth, meshHeight,
				this.toOffsetFloatAry(verts, sFloatAry), vertOffset, colors, colorOffset, paint);
	}

	@Override
	public void drawCircle(double cx, double cy, float radius, SafePaint paint) {
		getWrappedCanvas()
				.drawCircle((float) (cx + xOffset), (float) (cy + yOffset), radius, paint);
	}

	@Override
	public void drawColor(int color, Mode mode) {

		getWrappedCanvas().drawColor(color, mode);
	}

	@Override
	public void drawColor(int color) {

		getWrappedCanvas().drawColor(color);
	}

	@Override
	public void drawLine(double startX, double startY, double stopX, double stopY, SafePaint paint) {
		startX += xOffset;
		startY += yOffset;
		stopX += xOffset;
		stopY += yOffset;
		getWrappedCanvas().drawLine((float) startX, (float) startY, (float) stopX, (float) stopY,
				paint);
	}

	@Override
	public void drawLines(double[] pts, int offset, int count, SafePaint paint) {
		getWrappedCanvas().drawLines(this.toOffsetFloatAry(pts, sFloatAry), offset, count, paint);
	}

	@Override
	public void drawLines(double[] pts, SafePaint paint) {
		getWrappedCanvas().drawLines(this.toOffsetFloatAry(pts, sFloatAry), paint);
	}

	@Override
	public void drawOval(Rect oval, SafePaint paint) {
		getWrappedCanvas().drawOval(this.toOffsetRectF(oval, sRectF), paint);
	}

	@Override
	public void drawPaint(SafePaint paint) {
		getWrappedCanvas().drawPaint(paint);
	}

	@Override
	public void drawPath(SafeTranslatedPath path, SafePaint paint) {
		getWrappedCanvas().drawPath(path, paint);
	}

	@Override
	public void drawPicture(Picture picture, Rect dst) {
		dst.offset(xOffset, yOffset);
		getWrappedCanvas().drawPicture(picture, dst);
		dst.offset(-xOffset, -yOffset);
	}

	@Override
	public void drawPicture(Picture picture) {
		getWrappedCanvas().drawPicture(picture);
	}

	@Override
	public void drawPoint(double x, double y, SafePaint paint) {
		x += xOffset;
		y += yOffset;
		getWrappedCanvas().drawPoint((float) x, (float) y, paint);
	}

	@Override
	public void drawPoints(double[] pts, int offset, int count, SafePaint paint) {
		getWrappedCanvas().drawPoints(this.toOffsetFloatAry(pts, sFloatAry), offset, count, paint);
	}

	@Override
	public void drawPoints(double[] pts, SafePaint paint) {
		getWrappedCanvas().drawPoints(this.toOffsetFloatAry(pts, sFloatAry), paint);
	}

	@Override
	public void drawPosText(char[] text, int index, int count, double[] pos, SafePaint paint) {
		getWrappedCanvas().drawPosText(text, index, count, this.toOffsetFloatAry(pos, sFloatAry),
				paint);
	}

	@Override
	public void drawPosText(String text, double[] pos, SafePaint paint) {
		getWrappedCanvas().drawPosText(text, this.toOffsetFloatAry(pos, sFloatAry), paint);
	}

	@Override
	public void drawRGB(int r, int g, int b) {
		getWrappedCanvas().drawRGB(r, g, b);
	}

	@Override
	public void drawRect(double left, double top, double right, double bottom, SafePaint paint) {
		left += xOffset;
		right += xOffset;
		top += yOffset;
		bottom += yOffset;
		getWrappedCanvas()
				.drawRect((float) left, (float) top, (float) right, (float) bottom, paint);
	}

	@Override
	public void drawRect(Rect r, SafePaint paint) {
		r.offset(xOffset, yOffset);
		getWrappedCanvas().drawRect(r, paint);
		r.offset(-xOffset, -yOffset);
	}

	@Override
	public void drawRoundRect(Rect rect, float rx, float ry, SafePaint paint) {
		getWrappedCanvas().drawRoundRect(this.toOffsetRectF(rect, sRectF), rx, ry, paint);
	}

	@Override
	public void drawText(String text, double x, double y, SafePaint paint) {
		getWrappedCanvas().drawText(text, (float) (x + xOffset), (float) (y + yOffset), paint);
	}

	@Override
	public void drawText(char[] text, int index, int count, double x, double y, SafePaint paint) {
		getWrappedCanvas().drawText(text, index, count, (float) (x + xOffset),
				(float) (y + yOffset), paint);
	}

	@Override
	public void drawText(CharSequence text, int start, int end, double x, double y, SafePaint paint) {
		getWrappedCanvas().drawText(text, start, end, (float) (x + xOffset), (float) (y + yOffset),
				paint);
	}

	@Override
	public void drawText(String text, int start, int end, double x, double y, SafePaint paint) {
		getWrappedCanvas().drawText(text, start, end, (float) (x + xOffset), (float) (y + yOffset),
				paint);
	}

	@Override
	public void drawTextOnPath(char[] text, int index, int count, SafeTranslatedPath path,
			float hOffset, float vOffset, SafePaint paint) {
		getWrappedCanvas().drawTextOnPath(text, index, count, path, hOffset, vOffset, paint);
	}

	@Override
	public void drawTextOnPath(String text, SafeTranslatedPath path, float hOffset, float vOffset,
			SafePaint paint) {
		getWrappedCanvas().drawTextOnPath(text, path, hOffset, vOffset, paint);
	}

	@Override
	public void drawVertices(VertexMode mode, int vertexCount, double[] verts, int vertOffset,
			float[] texs, int texOffset, int[] colors, int colorOffset, short[] indices,
			int indexOffset, int indexCount, SafePaint paint) {
		getWrappedCanvas().drawVertices(mode, vertexCount, this.toOffsetFloatAry(verts, sFloatAry),
				vertOffset, texs, texOffset, colors, colorOffset, indices, indexOffset, indexCount,
				paint);
	}

	@Override
	public boolean getClipBounds(Rect bounds) {
		boolean success = getWrappedCanvas().getClipBounds(bounds);
		if (bounds != null)
			bounds.offset(-xOffset, -yOffset);
		return success;
	}

	@Override
	public int getDensity() {

		return getWrappedCanvas().getDensity();
	}

	@Override
	public DrawFilter getDrawFilter() {

		return getWrappedCanvas().getDrawFilter();
	}

	@Override
	public int getHeight() {

		return getWrappedCanvas().getHeight();
	}

	@Override
	public void getMatrix(Matrix ctm) {

		getWrappedCanvas().getMatrix(ctm);
	}

	@Override
	public int getSaveCount() {

		return getWrappedCanvas().getSaveCount();
	}

	@Override
	public int getWidth() {

		return getWrappedCanvas().getWidth();
	}

	@Override
	public boolean isOpaque() {

		return getWrappedCanvas().isOpaque();
	}

	@Override
	public boolean quickReject(double left, double top, double right, double bottom, EdgeType type) {
		left += xOffset;
		right += xOffset;
		top += yOffset;
		bottom += yOffset;
		return getWrappedCanvas().quickReject((float) left, (float) top, (float) right,
				(float) bottom, type);
	}

	@Override
	public boolean quickReject(SafeTranslatedPath path, EdgeType type) {
		return getWrappedCanvas().quickReject(path, type);
	}

	@Override
	public boolean quickReject(Rect rect, EdgeType type) {

		return getWrappedCanvas().quickReject(this.toOffsetRectF(rect, sRectF), type);
	}

	@Override
	public void restore() {

		getWrappedCanvas().restore();
	}

	@Override
	public void restoreToCount(int saveCount) {

		getWrappedCanvas().restoreToCount(saveCount);
	}

	@Override
	public void rotate(float degrees) {
		getWrappedCanvas().translate(this.xOffset, this.yOffset);
		getWrappedCanvas().rotate(degrees);
		getWrappedCanvas().translate(-this.xOffset, -this.yOffset);
	}

	@Override
	public void rotate(float degrees, double px, double py) {
		getWrappedCanvas().translate(this.xOffset, this.yOffset);
		getWrappedCanvas().rotate(degrees, (float) px, (float) py);
		// getWrappedCanvas().rotate(degrees, (float) (px + xOffset), (float) (py + yOffset));
		getWrappedCanvas().translate(-this.xOffset, -this.yOffset);
	}

	@Override
	public int save() {

		return getWrappedCanvas().save();
	}

	@Override
	public int save(int saveFlags) {

		return getWrappedCanvas().save(saveFlags);
	}

	@Override
	public int saveLayer(double left, double top, double right, double bottom, SafePaint paint,
			int saveFlags) {
		return getWrappedCanvas().saveLayer((float) (left + xOffset), (float) (top + yOffset),
				(float) (right + xOffset), (float) (bottom + yOffset), paint, saveFlags);
	}

	@Override
	public int saveLayer(Rect bounds, SafePaint paint, int saveFlags) {
		int result = getWrappedCanvas().saveLayer(this.toOffsetRectF(bounds, sRectF), paint,
				saveFlags);
		return result;
	}

	@Override
	public int saveLayerAlpha(double left, double top, double right, double bottom, int alpha,
			int saveFlags) {
		return getWrappedCanvas().saveLayerAlpha((float) (left + xOffset), (float) (top + yOffset),
				(float) (right + xOffset), (float) (bottom + yOffset), alpha, saveFlags);
	}

	@Override
	public int saveLayerAlpha(Rect bounds, int alpha, int saveFlags) {
		return getWrappedCanvas().saveLayerAlpha(this.toOffsetRectF(bounds, sRectF), alpha,
				saveFlags);
	}

	@Override
	public void scale(float sx, float sy) {
		getWrappedCanvas().scale(sx, sy);
	}

	@Override
	public void scale(float sx, float sy, double px, double py) {
		getWrappedCanvas().scale(sx, sy, (float) (px + xOffset), (float) (py + yOffset));
	}

	@Override
	public void setBitmap(Bitmap bitmap) {
		getWrappedCanvas().setBitmap(bitmap);
	}

	@Override
	public void setDensity(int density) {
		getWrappedCanvas().setDensity(density);
	}

	@Override
	public void setDrawFilter(DrawFilter filter) {
		getWrappedCanvas().setDrawFilter(filter);
	}

	@Override
	public void setMatrix(Matrix matrix) {
		getWrappedCanvas().setMatrix(matrix);
	}

	@Override
	public void skew(float sx, float sy) {
		getWrappedCanvas().skew(sx, sy);
	}

	@Override
	public void translate(float dx, float dy) {
		getWrappedCanvas().translate(dx, dy);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		SafeTranslatedCanvas c = new SafeTranslatedCanvas();
		c.setCanvas(mCanvas);
		return c;
	}

	@Override
	public boolean equals(Object o) {
		return getWrappedCanvas().equals(o);
	}

	@Override
	public int hashCode() {
		return getWrappedCanvas().hashCode();
	}

	@Override
	public String toString() {
		return getWrappedCanvas().toString();
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

	/**
	 * Helper function to convert a Rect to RectF and adjust the values of the Rect by the offsets.
	 */
	protected final float[] toOffsetFloatAry(double[] rect, float[] reuse) {
		if (reuse == null || reuse.length < rect.length)
			reuse = new float[rect.length];

		for (int a = 0; a < rect.length; a++) {
			reuse[a] = (float) (rect[a] + (a % 2 == 0 ? xOffset : yOffset));
		}
		return reuse;
	}
}
