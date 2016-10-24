
---
# "Hello osmdroid World"
osmdroid's MapView is basically a replacement for Google's MapView class. 
First of all, create your Android project, and follow [HowToMaven](How-to-include-OsmDroid-in-a-Maven-Android-project) if you're using Maven, or follow [HowToGradle](How-to-add-the-osmdroid-library-via-Gradle) if you're using Gradle/Android Studio. This will help you get the binaries for osmdroid included in your project. 

## Manifest
In most cases, you will have to set the following authorizations in your AndroidManifest.xml:
```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/> 
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

**Android 6.0+ devices require you have to check for "dangerous" permissions at runtime.**  
osmdroid requires the following dangerous permissions:  
`WRITE_EXTERNAL_STORAGE and ACCESS_COARSE_LOCATION/ACCESS_FINE_LOCATION.`  
See [OpenStreetMapViewer's implementation](https://github.com/osmdroid/osmdroid/blob/master/OpenStreetMapViewer/src/main/java/org/osmdroid/MainActivity.java#L83)
or [Google Documentation on Permissions](https://developer.android.com/training/permissions/requesting.html)


## Layout
Create a "src/main/res/layouts/main.xml" layout like this one. With Android Studio, it probably created one already called. The default is "src/main/res/layouts/activity_main.xml":
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        android:orientation="vertical" 
        android:layout_width="fill_parent"
        android:layout_height="fill_parent">
        <org.osmdroid.views.MapView android:id="@+id/map"
                android:layout_width="fill_parent" 
                android:layout_height="fill_parent" />
</LinearLayout>
```

## Main Activity
We now create the main activity (MainActivity.java):

```java
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //important! set your user agent to prevent getting banned from the osm servers  
    org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);

        MapView map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
    }
}
```

And that's enough to give it a try, and see the world map. 

Then we add default zoom buttons, and ability to zoom with 2 fingers (multi-touch)
```java
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
```

We can move the map on a default view point. For this, we need access to the map controller:
```java
        IMapController mapController = map.getController();
        mapController.setZoom(9);
        GeoPoint startPoint = new GeoPoint(48.8583, 2.2944);
        mapController.setCenter(startPoint);
```


# Advanced tutorial
The best example of how to use the osmdroid library is our [OpenStreetMapViewer sample project](https://github.com/osmdroid/osmdroid/tree/master/OpenStreetMapViewer). It contains a basic osmdroid application plus a few special-use examples. It is recommended you use this project as an example for building your application.

# Adding a MapView

You can add a `MapView` to your xml layout using:

```xml
<org.osmdroid.views.MapView
    android:id="@+id/mapview"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tilesource="Mapnik" />
```

This will allow you to configure the tile source imagery for your `MapView` but not much else.

However, for more control over your `MapView`, you will want to create a `MapView` programmatically.

```java
@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mMapView = new MapView(inflater.getContext(), 256, getContext());
    return mMapView;
}
```

# Images for Buttons and whatnot
For osmdroid 4.3 and older, there's a number of resources that the map uses for various user interface helpers, such as zoom in/out buttons, the device's current location when GPS is available and more. These resources are loaded via the "ResourceProxy". The idea is that you can either bring your own images or borrow the ones from osmdroid. If you're borrowing, then you'll want to grab the files located [here](https://github.com/osmdroid/osmdroid/tree/master/OpenStreetMapViewer/src/main/res/drawable) and add them to your project "src/main/res/drawable".

For osmdroid 5.0 and 5.1, the drawables are included with the AAR package. The resource proxy is still present and used so you can override values and images as needed.

For osmdroid 5.2 and up, the resource proxy is removed from the API set and replaced with Android context.

# Create a custom Resource Proxy

Applies only to versions prior to 5.2

As mentioned above, the Resource Proxy is a bit of a strange animal that osmdroid uses to load some images for user interface controls. If you're using any of the built-in controls that need images (zoom in/out, person icon, etc) you'll either need to provide your own images, borrow the images from osmdroid's example app, or provide your own implementation of Resource Proxy.

The example osmdroid app includes an example of this called CustomResourceProxy (included with > 4.3 osmdroid). All it does is change the my location drawable (person) to an alternate image. The example is below.

````
public class CustomResourceProxy extends DefaultResourceProxyImpl {

     private final Context mContext;
     public CustomResourceProxy(Context pContext) {
          super(pContext);
		mContext = pContext;
     }
     
     @Override
	public Bitmap getBitmap(final bitmap pResId) {
		switch (pResId){
               case person:
                    //your image goes here!!!
                    return BitmapFactory.decodeResource(mContext.getResources(),org.osmdroid.example.R.drawable.sfgpuci);
          }
          return super.getBitmap(pResId);
	}

	@Override
	public Drawable getDrawable(final bitmap pResId) {
		switch (pResId){
               case person:
                    return mContext.getResources().getDrawable(org.osmdroid.example.R.drawable.sfgpuci);
          }
          return super.getDrawable(pResId);
	}
}
````

Then you can use your instance using the following snippet.

````
mResourceProxy = new CustomResourceProxy(getApplicationContext());
final RelativeLayout rl = new RelativeLayout(this);
this.mOsmv = new MapView(this,mResourceProxy);
````

In order to see any difference with our example (changes the person icon), we'll need to get a location fix and add it to the map layers.

````
this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mOsmv, mResourceProxy);
this.mLocationOverlay.enableMyLocation();
this.mOsmv.getOverlays().add(mLocationOverlay);
this.mOsmv.setMultiTouchControls(true);
````




# Map Overlays

## How to add the My Location overlay

````
this.mLocationOverlay = new MyLocationNewOverlay(context, new GpsMyLocationProvider(context),mMapView);
mMapView.getOverlays().add(this.mLocationOverlay);
````

## How to add a compass overlay

````
this.mCompassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context), mMapView);
mMapView.getOverlays().add(this.mCompassOverlay);
````

## How to enable rotation gestures

````
mRotationGestureOverlay = new RotationGestureOverlay(context, mMapView);
mRotationGestureOverlay.setEnabled(true);
mMapView.setMultiTouchControls(true);
mMapView.getOverlays().add(this.mRotationGestureOverlay);
````

## How to add Map Scale bar overlay

````
mScaleBarOverlay = new ScaleBarOverlay(context);
mScaleBarOverlay.setCentred(true);
//play around with these values to get the location on screen in the right place for your applicatio
mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
mMapView.getOverlays().add(this.mScaleBarOverlay);
````

## How to add the built-in Minimap

Note: do not use when rotation is enabled! (Keep reading for a work around)

````
mMinimapOverlay = new MinimapOverlay(context, mMapView.getTileRequestCompleteHandler());
mMinimapOverlay.setWidth(dm.widthPixels / 5);
mMinimapOverlay.setHeight(dm.heightPixels / 5);
//optionally, you can set the minimap to a different tile source
//mMinimapOverlay.setTileSource(....);
mMapView.getOverlays().add(this.mMinimapOverlay);
````

Pro tip: If you want the minimap to stay put when rotation is enabled, create a second map view in your layout file, then wire up a change listener on the main map and use that to set the location on the minimap. For the reverse, you need to do the same process, however you have to filter map motion events to prevent infinite looping. There's an example on how to sync the views within the example application.




## How do I place icons on the map with a click listener?

````
//your items
ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
items.add(new OverlayItem("Title", "Description", new GeoPoint(0.0d,0.0d))); // Lat/Lon decimal degrees

//the overlay
ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(items,
	new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
	@Override
	public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
	//do something
	    return true;
	}
	@Override
	public boolean onItemLongPress(final int index, final OverlayItem item) {
		return false;
	}
}, mResourceProxy);
mOverlay.setFocusItemsOnTap(true);

mMapView.getOverlays().add(mOverlay);
````

## How many icons can I put on the map?

The answer is greatly dependent on what hardware the osmdroid based app is ran on. A Samsung S5 (no endorsement intended) ran just fine at 3k icons and was noticeably choppy at 6k icons. Your mileage may vary. X86 Android running on modern hardware will perform great at even higher numbers. However it's recommended to limit the amount of stuff you're rendering, if at all possible.

If you're also drawing paths, lines, polygons, etc, then this also changes the equation. Drawing multipoint graphics is computationally more expensive and thus negatively affects performance under higher loads. To mitigate performance issues with multipoint graphics, one strategy would be to reduce the amount of points handed off to the map engine when at a higher zoom level (numerically lower), then increase the fidelity as the user zoom's in. In effect, you would be clipping the visible data at the map view bounds so that the map view only "knows" about what's in screen and doesn't have to loop through all 10k icons that you want on the map. Although you can give the map view all 10k objects, but every time the map moves or zooms, it will iterate over all 10k items to calculate where to draw them (if at all). Using this mechanism paired with map motion listeners and a database query that supports geographic bounds, you can support a rich experience for users with lots of data and still have reasonable performance.

Reusing drawables for icons will help with memory usage too. 

## Map Sources, Imagery and Tile sets.

See https://github.com/osmdroid/osmdroid/wiki/Map-Sources