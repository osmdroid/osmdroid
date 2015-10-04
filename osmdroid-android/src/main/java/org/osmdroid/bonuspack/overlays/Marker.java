package org.osmdroid.bonuspack.overlays;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.ResourceProxy.bitmap;
import org.osmdroid.bonuspack.R;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

/**
 * An icon placed at a particular point on the map's surface. 
 * Mimics the Marker class from Google Maps Android API v2 as much as possible. Main differences:<br/>
 * - Doesn't support Z-Index: as other osmdroid overlays, Marker is drawn in the order of appearance. <br/>
 * - The icon can be any standard Android Drawable, instead of the BitmapDescriptor introduced in Google Maps API v2. <br/>
 * - The icon can be changed at any time. <br/>
 * - The InfoWindow hosts a standard Android View. It can handle Android widgets like buttons and so on. <br/>
 * - Supports a "sub-description", to be displayed in the InfoWindow, under the snippet, in a smaller text font. <br/>
 * - Supports an image, to be displayed in the InfoWindow. <br/>
 * - Supports "panning to view" on/off option (when touching a marker, center the map on marker position). <br/>
 * - Opening a Marker InfoWindow automatically close others only if it's the same InfoWindow shared between Markers. <br/>
 * - Events listeners are set per marker, not per map. <br/>
 * 
 * TODO: <br/>
 * Impact of marker rotation on hitTest<br/>
 * When map is rotated, when panning the map, bug on the InfoWindow positioning (osmdroid issue #524)<br/>
 * 
 * @see MarkerInfoWindow
 * @see <a href="http://developer.android.com/reference/com/google/android/gms/maps/model/Marker.html">Google Maps Marker</a>
 * 
 * @author M.Kergall
 *
 */
public class Marker extends OverlayWithIW {

	/*attributes for standard features:*/
	protected Drawable mIcon;
	protected GeoPoint mPosition;
	protected float mBearing;
	protected float mAnchorU, mAnchorV;
	protected float mIWAnchorU, mIWAnchorV;
	protected float mAlpha;
	protected boolean mDraggable, mIsDragged;
	protected boolean mFlat;
	protected OnMarkerClickListener mOnMarkerClickListener;
	protected OnMarkerDragListener mOnMarkerDragListener;
	
	/*attributes for non-standard features:*/
	protected Drawable mImage;
	protected boolean mPanToView;

	/*internals*/
	protected Point mPositionPixels;
	protected static MarkerInfoWindow mDefaultInfoWindow = null;
	protected static Drawable mDefaultIcon = null; //cache for default icon (resourceProxy.getDrawable being slow)
	
	/** Usual values in the (U,V) coordinates system of the icon image */
	public static final float ANCHOR_CENTER=0.5f, ANCHOR_LEFT=0.0f, ANCHOR_TOP=0.0f, ANCHOR_RIGHT=1.0f, ANCHOR_BOTTOM=1.0f;
	
	public Marker(MapView mapView) {
		this(mapView, new DefaultResourceProxyImpl(mapView.getContext()));
	}

	public Marker(MapView mapView, final ResourceProxy resourceProxy) {
		super(resourceProxy);
		mBearing = 0.0f;
		mAlpha = 1.0f; //opaque
		mPosition = new GeoPoint(0.0, 0.0);
		mAnchorU = ANCHOR_CENTER;
		mAnchorV = ANCHOR_CENTER;
		mIWAnchorU = ANCHOR_CENTER;
		mIWAnchorV = ANCHOR_TOP;
		mDraggable = false;
		mIsDragged = false;
		mPositionPixels = new Point();
		mPanToView = true;
		mFlat = false; //billboard
		mOnMarkerClickListener = null;
		mOnMarkerDragListener = null;
		if (mDefaultIcon == null)
			mDefaultIcon = resourceProxy.getDrawable(bitmap.marker_default);
		mIcon = mDefaultIcon;
		if (mDefaultInfoWindow == null || mDefaultInfoWindow.mMapView != mapView){
			//build default bubble, that will be shared between all markers using the default one:
			/* pre-aar version
			Context context = mapView.getContext();
			String packageName = context.getPackageName();
			int defaultLayoutResId = context.getResources().getIdentifier("bonuspack_bubble", "layout", packageName);
			if (defaultLayoutResId == 0)
				Log.e(BonusPackHelper.LOG_TAG, "Marker: layout/bonuspack_bubble not found in "+packageName);
			else
				mDefaultInfoWindow = new MarkerInfoWindow(defaultLayoutResId, mapView);
			*/
			//get the default layout now included in the aar library
			mDefaultInfoWindow = new MarkerInfoWindow(R.layout.bonuspack_bubble, mapView);
		}
		setInfoWindow(mDefaultInfoWindow);
	}

	/** Sets the icon for the marker. Can be changed at any time. 
	 * @param icon if null, the default osmdroid marker is used. 
	 */
	public void setIcon(Drawable icon){
		if (icon != null)
			mIcon = icon;
		else 
			mIcon = mDefaultIcon;
	}
	
	public GeoPoint getPosition(){
		return mPosition;
	}
	
	public void setPosition(GeoPoint position){
		mPosition = position.clone();
	}

	public float getRotation(){
		return mBearing;
	}
	
	public void setRotation(float rotation){
		mBearing = rotation;
	}
	
	public void setAnchor(float anchorU, float anchorV){
		mAnchorU = anchorU;
		mAnchorV= anchorV;
	}
	
	public void setInfoWindowAnchor(float anchorU, float anchorV){
		mIWAnchorU = anchorU;
		mIWAnchorV= anchorV;
	}
	
	public void setAlpha(float alpha){
		mAlpha = alpha;
	}
	
	public float getAlpha(){
		return mAlpha;
	}
	
	public void setDraggable(boolean draggable){
		mDraggable = draggable;
	}
	
	public boolean isDraggable(){
		return mDraggable;
	}

	public void setFlat(boolean flat){
		mFlat = flat;
	}
	
	public boolean isFlat(){
		return mFlat;
	}
	
	/** 
	 * Removes this Marker from the MapView. 
	 * Note that this method will operate only if the Marker is in the MapView overlays 
	 * (it should not be included in a container like a FolderOverlay). 
	 * @param mapView
	 */
	public void remove(MapView mapView){
		mapView.getOverlays().remove(this);
	}

	public void setOnMarkerClickListener(OnMarkerClickListener listener){
		mOnMarkerClickListener = listener;
	}
	
	public void setOnMarkerDragListener(OnMarkerDragListener listener){
		mOnMarkerDragListener = listener;
	}
	
	/** set an image to be shown in the InfoWindow  - this is not the marker icon */
	public void setImage(Drawable image){
		mImage = image;
	}

	/** get the image to be shown in the InfoWindow - this is not the marker icon */
	public Drawable getImage(){
		return mImage;
	}

	/** Set the InfoWindow to be used. 
	 * Default is a MarkerInfoWindow, with the layout named "bonuspack_bubble". 
	 * You can use this method either to use your own layout, or to use your own sub-class of InfoWindow. 
	 * Note that this InfoWindow will receive the Marker object as an input, so it MUST be able to handle Marker attributes. 
	 * If you don't want any InfoWindow to open, you can set it to null. */
	public void setInfoWindow(MarkerInfoWindow infoWindow){
		mInfoWindow = infoWindow;
	}

	/** If set to true, when clicking the marker, the map will be centered on the marker position. 
	 * Default is true. */
	public void setPanToView(boolean panToView){
		mPanToView = panToView;
	}
	
	public void showInfoWindow(){
		if (mInfoWindow == null)
			return;
		int markerWidth = 0, markerHeight = 0;
		markerWidth = mIcon.getIntrinsicWidth(); 
		markerHeight = mIcon.getIntrinsicHeight();
		
		int offsetX = (int)(mIWAnchorU*markerWidth) - (int)(mAnchorU*markerWidth);
		int offsetY = (int)(mIWAnchorV*markerHeight) - (int)(mAnchorV*markerHeight);
		
		mInfoWindow.open(this, mPosition, offsetX, offsetY);
	}
	
	public boolean isInfoWindowShown(){
		if (mInfoWindow instanceof MarkerInfoWindow){
			MarkerInfoWindow iw = (MarkerInfoWindow)mInfoWindow;
			return (iw != null) && iw.isOpen() && (iw.mMarkerRef==this);
		} else
			return super.isInfoWindowOpen();
	}
	
	@Override public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow)
			return;
		if (mIcon == null)
			return;
		
		final Projection pj = mapView.getProjection();
		
		pj.toPixels(mPosition, mPositionPixels);
		int width = mIcon.getIntrinsicWidth();
		int height = mIcon.getIntrinsicHeight();
		Rect rect = new Rect(0, 0, width, height);
		rect.offset(-(int)(mAnchorU*width), -(int)(mAnchorV*height));
		mIcon.setBounds(rect);
		
		mIcon.setAlpha((int)(mAlpha*255));
		
		float rotationOnScreen = (mFlat ? -mBearing : mapView.getMapOrientation()-mBearing);
		drawAt(canvas, mIcon, mPositionPixels.x, mPositionPixels.y, false, rotationOnScreen);
	}

    /** Null out the static references when the MapView is detached to prevent memory leaks. */
	@Override
	public void onDetach(MapView mapView) {
		mDefaultIcon = null;
        mDefaultInfoWindow = null;
        super.onDetach(mapView);
    }

	public boolean hitTest(final MotionEvent event, final MapView mapView){
		final Projection pj = mapView.getProjection();
		pj.toPixels(mPosition, mPositionPixels);
		final Rect screenRect = pj.getIntrinsicScreenRect();
		int x = -mPositionPixels.x + screenRect.left + (int) event.getX();
		int y = -mPositionPixels.y + screenRect.top + (int) event.getY();
		boolean hit = mIcon.getBounds().contains(x, y);
		return hit;
	}
	
	@Override public boolean onSingleTapConfirmed(final MotionEvent event, final MapView mapView){
		boolean touched = hitTest(event, mapView);
		if (touched){
			if (mOnMarkerClickListener == null){
				return onMarkerClickDefault(this, mapView);
			} else {
				return mOnMarkerClickListener.onMarkerClick(this, mapView);
			}
		} else
			return touched;
	}

	public void moveToEventPosition(final MotionEvent event, final MapView mapView){
		final Projection pj = mapView.getProjection();
		mPosition = (GeoPoint) pj.fromPixels((int)event.getX(), (int)event.getY());
		mapView.invalidate();
	}
	
	@Override public boolean onLongPress(final MotionEvent event, final MapView mapView) {
		boolean touched = hitTest(event, mapView);
		if (touched){
			if (mDraggable){
				//starts dragging mode:
				mIsDragged = true;
				closeInfoWindow();
				if (mOnMarkerDragListener != null)
					mOnMarkerDragListener.onMarkerDragStart(this);
				moveToEventPosition(event, mapView);
			}
		}
		return touched;
	}
	
	@Override public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
		if (mDraggable && mIsDragged){
			if (event.getAction() == MotionEvent.ACTION_UP) {
				mIsDragged = false;
				if (mOnMarkerDragListener != null)
					mOnMarkerDragListener.onMarkerDragEnd(this);
				return true;
			} else if (event.getAction() == MotionEvent.ACTION_MOVE){
				moveToEventPosition(event, mapView);
				if (mOnMarkerDragListener != null)
						mOnMarkerDragListener.onMarkerDrag(this);
				return true;
			} else 
				return false;
		} else 
			return false;
	}
	
	//-- Marker events listener interfaces ------------------------------------
	
	public interface OnMarkerClickListener{
		abstract boolean onMarkerClick(Marker marker, MapView mapView); 
	}
	
	public interface OnMarkerDragListener{
		abstract void onMarkerDrag(Marker marker);
		abstract void onMarkerDragEnd(Marker marker);
		abstract void onMarkerDragStart(Marker marker);
	}
	
	/** default behaviour when no click listener is set */
	protected boolean onMarkerClickDefault(Marker marker, MapView mapView) {
		marker.showInfoWindow();
		if (marker.mPanToView)
			mapView.getController().animateTo(marker.getPosition());
		return true;
	}
	
}
