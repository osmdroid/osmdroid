# All about osmdroid-thirdparty

This library provides the following features as an add on for osmdroild

* Show OSM tiles on Google Maps
* Wrappers for using osmdroid API's with the Google Maps engine
* Tile source for using Bing Maps with osmdroid


## Google Wrapper

The osmdroid Google wrapper allows you to easily switch between a Google MapView and an osmdroid MapView by letting the application refer to the IMapView interface instead of the specific implementation.

### Details

In your code you could get the map view preference and then instantiate a view of the preferred type. This will be assigned to an IMapView variable. From then on the rest of the code can use the map view without regard to what type it is.

````
// member variable 
IMapView mMapView;

// create the required type of 
MapView mMapView = googleView ? new com.google.android.maps.MapView(...) : new org.osmdroid.views.MapView(...);

// you can then use it regardless of which type it is, for example 
mMapView.getController().zoomIn();
````

For some operations you may need to do more than simply instantiation, for example adding overlays.

````
if (googleView) {
            com.google.android.maps.MapView mapView = new com.google.android.maps.MapView(...);
            com.google.android.maps.MyLocationOverlay myLocationOverlay = new com.google.android.maps.MyLocationOverlay(...)
            ;
            mapView.getOverlays().add(myLocationOverlay);
            mMapView = mapView;
        } else {
            org.osmdroid.views.MapView mapView = new org.osmdroid.views.MapView(...);
            org.osmdroid.views.overlay.MyLocationOverlay myLocationOverlay = org.osmdroid.views.overlay.MyLocationOverlay(...)
            ;
            mapView.getOverlays().add(myLocationOverlay);
            mMapView = mapView;
        }
````

There's also more ways of instantiating the MapView, for example 

````
mMapView = (IMapView) findViewById(R.id.map);
````

Hopefully these samples make the idea of the Google wrapper clear