# Tile Caching

osmdroid provides a few mechanisms for tile caching (for when you're internet connected and tiles need to be downloaded). 

 - TileWriter - uses the file system as the tile store, default is /sdcard/osmdroid/tiles/source/z/x/y.png
 - SqlTileWriter - uses a Sqlite database similar to the archival database structure that is used for offline archives. It adds expiration time.

# What do I need to know about tile caching

When osmdroid downloads tiles, if the tile cannot be stored to disk, it will not be displayed to the user. Osmdroid needs a location that's writable for storing the tile cache. The location is determined by OpenStreetMapsTileProviderConstants.TILE_BASE_PATH. Android is strange in terms of storage locations.

## Downloaded tiles expire

Per the usage policy of just about every tile source, we do our best to honor the "expires" flag set on all downloaded tiles. This is handled differently for both tile writers.

 - TileWriter (disk storage) - not supported
 - SqlTileWriter (database storage) - at start up, database size is checked against OpenStreetMapTileProviderConstants. Tiles that are expired are the first to removed from the database. If needed (we are past the maximum size), additional tiles are removed (sorted by the tiles that will expire first).

The default cache expiration date is now + 1 week's time.

## Handling low disk space

Version 5.6 adds a number of new features to help you manage free disk space. IFilesystemCache now provides methods to remove a specific tile, check the existence of a tile, purge the entire cache, get the current row count, and some methods to help convert old file system caches into the newer sqlite cache.

## Changes in versions of osmdroid

| Version | Behavioral Changes
| ---     | ---
| 5.6     | On first startup of the map, the mount point points are discovered and it auto selects the first storage location that's writable that has the largest amount of free space. You'll probably want your application's users to be able to change this. In addition, if there are no writable mount points on the device, you can also use your application's private storage (/data/data/package/....). See the sample application use example usage patterns.
| 5.5     | Default is now Sqlite database for API10+, API8-9 still uses TileWriter due to android strangeness. Default location is Environment.getExternalStorageDir/osmdroid/tiles
| older than 5.5 | TileWriter, default location is Environment.getExternalStorageDir/osmdroid/tiles


# Tile Cache Settings

osmdroid caches tiles using a number of settings. This is how to tweak the settings if needed. The defaults work for most applications. Use with caution

## In memory LRU Cache

````
//initial setting
org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants#CACHE_MAPTILECOUNT_DEFAULT
//programmatic construction
org.osmdroid.tileprovider.MapTileCache(int size)
````

## In memory TileOverlay overshoot cache

````
TilesOverlay x=this.mMapView.getOverlayManager().getTilesOverlay();
x.setOvershootTileCache(x.getOvershootTileCache() * 2);
````


## On disk cache (sqlite and file system storage)
````
OpenStreetMapTileProviderConstants.setCacheSizes(max, trim);
````

## Set the cache location (both SqlTileWriter and TileWriter)
````
OpenStreetMapTileProviderConstants.setCachePath
````