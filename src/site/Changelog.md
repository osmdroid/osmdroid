# Changelog

This list only includes major highlights or breaking changes. Check [here](https://github.com/osmdroid/osmdroid/commits/master) for full commit logs.

[Upgrading?](https://github.com/osmdroid/osmdroid/wiki/Upgrade-Guide)

## 5.x (some future version)
 * WMS Map Source Client

## 5.5 (not yet released)
 * osmdroid JDK and ADK raster tile server
 * IMap, IMapView API clean up

## 5.4  (current release from 2016-09-07)
 * Use SQLite database for tile cache instead of file system
 * Removal of MapControllerOld (been deprecated for a long time)
 * Cache manager improvements, can download tiles from a path, customizable UIs and can create MOBAC like tile archives
 * Fix for zoom out/in animation on < 10 APIs
 * Fix for pinch to zoom sensitivity
 * Many memory leak fixes
 * Using doubles internally for points instead of ints (increased precision)
 * MapBox samples updated
 * MapQuest tile source fixed to meet the newer access token requirements
 * HERE We Go map tile source
 * Primary build system switched to gradle (with gradle-fury)
 * Customizable loading and file not found image placeholders

## 5.3
5.3 is the same source base as 5.4, but the version was skipped due to a release snafu.

## 5.2
 * Mapsforge as a tile source
 * Refactor of the 3rd party sample app and support library
 * Death of the resource proxy
 * Partial merger of osmbonus pack
 * Lat/Lon gridline overlay

## 5.1 
 * Fixes
   * OSM Tile Packager updated and resolved issues with full paths being included in zip files
   * Fix for classes that extend IconOverlays not calling onDraw 
   * API change for MapBox, you can now set access tokens and map id sources per tile source (instead of a static variable). You can also set them programmatically.
   * Fix for offline zip/sqlite files not being detected in /sdcard/osmdroid/
   * Added .nomedia files to tile download cache to prevent the gallery from picking it up
   * ItemizedOverlayWithFocus is not scaled for screen resolution #214
   * Fix for #214, non-scaling itemized icon popups
     * potentially breaking change, adds new methods to the ResourceProxy
 * New features
   * Downloaded tiles are now stored in a database instead of on the raw file system.
    * potentially breaking change, adds new methods to the ResourceProxy
   * Potentially breaking change. Scalebar can now moved to different parts of the screen. Only the constructor is different
   * Potentially breaking change. Thread pool size for map tile downloading is now adjustable.
   * Potentially breaking change. NightMode was removed from IMapController and moved to the TilesOverlay. You can now set your own color filters too.
   * Added Animated Zoom To feature
 * Other
   * Refactoring of the sample app to make things easier to find
   * Java 7 APIs
 
## 5.0.1
 * Target output and publication to maven central is AAR which includes the default resource files (person icon, etc)
 * Tile inversion for a rudimentary night mode
 * Better support for Android x86 with online map sources
 * Better support for application specific control of map tile loading
   * Can now use offline tiles from just a single zip/sqlite/etc which don't have to be in /sdcard/osmdroid
 * Ability to change the cache folder and offline zip/database location
 * Itemized icon overlay icons no longer scale while zooming
 * My location icon can now be changed at runtime
 * Read/write permissions change for sqlite databases (now is read only)
 * Fling can be disabled programmatically
 * Better handling of map motion events for listeners
 * Updated build tech. Gradle build files also updated to reflect the changes
 * SLF4J dependency dropped. All logging now goes to the standard logcat
 * New Map source added for USGS maps
 * Fix for MapBox data sources
 * Many additional examples and samples on the demo app.
 * Android 6.0 support and the removal of Apache Http Client as a dependency
 * Rotation gestures now has reduced jitter
 * Can now set the HTTP User Agent for all HTTP connections

### BREAKING CHANGES!!

 * The API structure for IArchiveFile has changed. If you have a custom one, it can now be registered by file extension via ArchiveFileFactory.registerArchiveFileProvier(Class, file extension). IArchiveFile should also provide a list of named tile sources.
 * The API structure ITileSource and everything related to it has changed. All methods that required a ResourceProxy.string value were modified to no longer require the ResourceProxy.string value. It wasn't really used anywhere, was unnecessary and only added confusion. 
 * Apache Http Client removed
 * Rotation Gesture detector is now included in osmdroid-android@aar and is in a new name space


## 4.3
 * Fixing issue #22: MapController.setCenter not centering when used in onCreate (and more generally: all actions on mapView positionning done in onCreate). 
 * Fixing issues related to high density screens. 

## 4.2 

#### Note - breaking changes
We have made a number of changes to the maps that may require some changes for users that write their own Overlays.

* The Projection class is the singular authority for lat/long <-> pixel conversions. Do NOT use TileSystem unless you really know what you're doing.
* The Projection methods have been simplified and now strictly follow the Google Maps v1 API. One major change is that toMapPixels() is now correctly called toPixels(). Redundant and confusing methods were removed.
* The Projection class is broken out into its own class. It can be extended to add additional functionality by users. Override MapView.getProjection() to pass back your own instance.
* The "Safe" canvas classes have been eliminated. All "Safe" classes can be reverted back to their normal counterparts - so SafePaint can be reverted to Paint, SafeCanvas to Canvas, etc...
* We have implemented an OsmPath which behaves similarly to the old SafePath class. By calling onDrawCycle() in the draw() method of your overlay, it will adjust the location of the path so as to avoid having to recalculate the points on every draw.

#### Other changes

* Added experimental HW-acceleration support. See issue 413.
* Issue 426, issue 479, issue 489, issue 520, issue 521.

## 4.1

* Fix issue with OSM tile servers rejecting GETs due to missing user-agent. See issue 515.
* Issue 417, issue 477, issue 489, issue 491, issue 498, issue 500, issue 507.

## 4.0

* Added compatibility layer for Google Maps API v2.
* Added !BitmapPool to reuse !MapTiles and prevent constant Bitmap allocation during scrolling.
* Added fix to ignore !MapView 'clickable' setting that can prevent scrolling.
* Added http client factory to !MapTileDownloader.
* Some changes to clean up the !MapController. See issue 471.
* Changed zoom animations to match pinch-to-zoom animations for consistency and to allow overlays to prevent their contents from scrolling. See issue 453.
* Issue 298, issue 408, issue 427, issue 437, issue 438, issue 441, issue 442, issue 447, issue 450, issue 451, issue 483.

## 3.0.10

* Added limited scrolling area
* Added simple setable min/max zoomlevel on the !MapView that overrides the values returned by the tile provider.
* Pinch-to-zoom is relative to the pinch point, not the center of the screen.
* New samples project with modern Fragments.