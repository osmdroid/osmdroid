# FAQ

## Map tiles dont seem to load in my application

Make sure your AndroidManifest.xml file has the android.permission.INTERNET permission. See Prerequisites.

Aug 2016 - someone managed to get the default user agent used by osmdroid banned from Open Street Maps tile servers. This can also be a reason for tiles failing to load (usually with an access denied, bad request or other similar HTTP error message). To fix, set your user agent to something unique for your app. The User Agent is set via `org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants` class.

If you're on an API23+ device, make sure the app has sufficient runtime permissions (user granted).

Also, if for some reason Environment.getExternalStorage() returns a read only mount point, maps tiles will also not be loaded. To overcome this, set the ` OpenStreetMapTileProviderConstants.setCachePath(new File("/sdcard/osmdroid2/").getAbsolutePath());` or to some path that you know has write access. This will be revised with osmdroid 5.6 to automatically select the best available path on start up.

## The map will not scroll when I drag my finger on the screen

Make sure you don't set the MapView to be "clickable". If you are using xml layouts make sure you don't have android:clickable="true" or if you are creating your MapView programmatically make sure you don't have mMapView.setClickable(true);. This should no longer be necessary starting with osmdroid-3.0.11.

## The map is drawing incorrectly/misaligned

Turn off hardware acceleration in your manifest. See Prerequisites.

Update, turning off hardware acceleration is generally not required unless you have specific problems with a given device

This condition can also occur if you somehow manage to change the screen resolution of your android device or virtual machine and do not reboot before launching osmdroid.


## Night Mode

osmdroid, since v5.0, has support for inverting the color scheme of map tiles to have a more user friendly experience at night. Your mileage may vary based on map sources. Lighter map sources, such as TOPO or road maps,  will appear darker at night. Satellite based tile sources may actually appear brighter at night.

````
this.mMapView.getController().setInvertedTiles(true);
````

Starting with 5.1, the API has changed a bit for this. See the change log.

## Debugging tile loading issues

[Moved](Debugging-osmdroid)

## Does my device need an sd card or some kind of storage medium?

Yes. osmdroid downloads and caches map data on device and it needs to be stored on some writable medium. We use `Environment.getExternalStorage` and if that doesn't return a writable location (which happens some devices) then the cache won't be available, which well lead to significantly increased data usage (or osmdroid just won't work at all). The location can be overridden to use application private storage or whatever.

As of 5.6, this was updated to automatically select the largest writable storage directory on start up. The sample application has some logic that you'll definitely want to look at it.

## What's the Dex count for osmdroid?

Rather small, at just over 2000 methods.

## Can I change where osmdroid looks for tile archives and the location of the tile cache?

Yes! Both of these settings are in the following class

````
        OpenStreetMapTileProviderConstants.setCachePath(...)
        OpenStreetMapTileProviderConstants.setCacheSizes(...)
        OpenStreetMapTileProviderConstants.setOfflineMapsPath(...)
        OpenStreetMapTileProviderConstants.setUserAgentValue(...)
````

## I zoomed past level 20 and the everything disappeared!

UPDATE! This should be fixed for version 5.6 (fixed meaning the icons and graphics won't disappear at zoom 20-22.

Yup, it's a known issue. See the following issues.

https://github.com/osmdroid/osmdroid/issues/416

https://github.com/osmdroid/osmdroid/issues/329

https://github.com/osmdroid/osmdroid/issues/230

https://github.com/osmdroid/osmdroid/issues/114

https://github.com/osmdroid/osmdroid/issues/46

The root cause is that we're using Android's "View" class to map pixel x,y integer coordinates to lat,long (double). As you zoom in, the size of world in pixels increases exponentially and causes integer overflows at zoom levels above 20 towards the extremes of the planet and nearly everywhere by zoom 22. As such, zoom 20 is really the highest that's supported, although osmdroid doesn't restrict you from going to the extremes. 

Behaviors to expect when at zoom > 20
 - double tapping to zoom in can cause the map to fling towards the other side of the planet
 - lines and polygons can disappear


# If you get 'resource not found' error

This is because osmdroid uses a number of graphics (Android drawables) that represent things like current device location, zoom in/out buttons, etc. These are not included with osmdroid because it's distributed as a JAR file (versions =< 4.3). You have two options:

1. Implement your own version of "ResourceProxy"
2. Pull in the osmdroid example application's drawable files into your own application.

This process is detailed here [How-to-use-the-osmdroid-library#create-a-custom-resource-proxy](How-to-use-the-osmdroid-library#create-a-custom-resource-proxy)
