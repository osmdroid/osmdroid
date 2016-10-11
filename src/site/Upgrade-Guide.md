# A guide for upgrading your app 

## From version 5.4 to x.y (future placeholder)

## From version 5.4 to 5.4.1 (current release from 2016-09-13)
No APIs changes, just bug fixes

## From version 5.2 to 5.4  

### Overlay constructor change
Overlays no longer require a Context constructor

### Switch to Doubles (API Change)

The "Doubles" branch was finally merged. This means that osmdroid uses doubles internally now for higher precision as lower zoom levels (higher numerically). All 1E6 based APIs are deprecated but still accessible. This does not resolve issues with zoom > 20 due to Android using an integer based coordinate system.

### Data Storage

osmdroid has always stored map tiles to the local file system. When upgrading to 5.1 and from now on, downloaded tiles will be stored in a database named tiles.db which contains the tile source name, the tile id (x/y/z), and an expiration date. When osmdroid (or apps that use it) starts up, it will automatically prune old tiles from the database. This only happens when the map is created and it happens asynchronously.

Tiles that were already loaded in device....

TBD do we leave it or delete it? Or run an import routine, then delete from local storage....

### MapBox/MapQuest manifest access tokens

Mapquest now apparently uses MapBox for map tiles. Go to MapBox's website to get an access token, then use that access token for both MapQuest and MapBox tile sources.

The MapBox tile source formerly used the "ACCESS_TOKEN" meta-data entry in the manifest. It now uses "MAPBOX_ACCESS_TOKEN". 

MapQuest uses "MAPQUEST_ACCESS_TOKEN" and "MAPQUEST_MAPID" (default is "mapquest.streets-mb" if not defined)

### Overlay API Changes
Everything based on overlay has had it's constructor simplified in order to support the [creation of overlays in an async task](https://github.com/osmdroid/osmdroid/pull/373). As such, you may have some work to do for updating overlays. There was one field removed from the base Overlay class, mScale, which wasn't used anywhere except for the ScaleBarOverlay. The old api with Context parameters is still there but set to depricated.

## From version 5.1 to 5.2

### Partial Merger of OSM Bonus Pack

If you were using OSMBonusPack with osmdroid, there's been some significant changes recently. Namely, everything but the KML/GeoJson parsing and online routing tools have been migrated (with Git history). As a consequence, several packages were shuffled around and renamed in order to prevent collisions and to better align with osmdroid's package structure. We also made some minor, hopefully non-breaking changes to the CacheManager and to Marker (both from OSMBonusPack).

* CacheManager - org.osmdroid.tileprovider.cachemanager
* Marker and Overlays from OSMBonusPack - org.osmdroid.views.overlay, org.osmdroid.views.overlay.infowindow

### Mapsforge Adapter and sample app

Included with the migration of OSMBonusPack's capabilities is the Mapsforge Adapter, which lets you use on device rendered tiles with Mapsforge's rendering engine. Since Mapsforge is LGPL based license, so is this adapter.
 
### Gridline overlay

Ever wanted Lat/Lon gridlines? We have to covered.

* org.osmdroid.views.overlay.gridlines

### Tilepackager

The command line and swing GUI tile packagers also received some attention and are now included with the distribution. This adds a number of bug fixes.

### Refactoring (moving classes)

All my location type overlays were consolidated into it's own package

### Resource Proxy and older classes

* Resource Proxy and all derived classes were deleted and removed from all resource paths and constructors
* MapControllerOld was deleted (use MapController)
* MyLocationOverlay was deleted (use MyNewLocationOverlay)

## From version 5.0.1 to 5.1

### Night Mode changes

If you were previously using IMapController.isNightMode or setNightMode or extended IMapController, these functions were removed and migrated directly into the TilesOverlay. The same functionality can be implemented using the following
````
this.mMapView.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
````

### Scale Bar

If you were using the ScaleBarOverlay, the constructor has changed from passing in `Context` to `MapView`.


### Changed to the Resource Proxy

There were some changes to the resource proxy interface. If you inherit from one of the provided implementations, you're probably good to go.


## From version v4.3 to v5.0.1
Output target is now AAR and the default osmdroid resources are now included (my location icon and whatnot). If you 'borrowed' those icons from osmdroid, you now will no longer need to include them.

If you use any custom map tile sources, a parameter was removed from the constructor so you'll need to adjust accordingly.

If you used a custom overlay manager, that class was interfaced and refactored. Please now extend from DefaultOverlayManager or implement the interface as appropriate.