This document has the definitive guide for using osmdroid without a network connection.

# What are the supported storage formats?

osmdroid provides out of the box support for several different types of offline map tile caches.

 * osmdroid's flavor of a sqlite database (recommended)
 * osmdroid ZIP
 * MBTiles
 * GEMF
 * GeoPackage (still under development)

# How can I create an offline storage archive?

There are a few different options.

## OSM Map Tile Packager

osmdroid comes with a tool to enable you to download tiles and store them for offline use called the `OSM Map Tile Packager`. See the [readme.md for more information](https://github.com/osmdroid/osmdroid/blob/master/OSMMapTilePackager/readme.md).

## Mobile Atlas Creator (MOBAC)

MOBAC is the easiest to use tool that there is and supports a huge array input map sources and output formats (and is open source).

General guidance

 1. Create a new atlas using of the supported types (osmdroid SQLite)
 2. Select your map source
 3. Select a bounding box for the area you want and the zoom levels you want to acquire
 4. Click 'Add Selection'
 5. Click 'Create Atlas'
 6. Once it's done, copy the atlas file (.sqlite, .zip, etc) onto your osmdroid powered device to /sdcard/osmdroid/
 7. Tell osmdroid to not use a network connection and set the map tile source. **

## Maperitive.net

[Maperitive](http://maperitive.net/) supports generating tile images from OSM data API and can be used as a replacement for MOBAC which has been [blocked by openstreetmap.org](http://wiki.openstreetmap.org/wiki/Blocked_applications) for overusing traffic.

## Use osmdroid's Cache Manager on device ( > version 5.2)

The Cache Manager provides programmatic access to the `cache of downloaded tiles`. It can also be used to create `tile archives`. Two key points here:
 - `cache of downloaded tiles` is an expiring tile cache of tiles that osmdroid has downloaded from an online tile source. It expires. The default expiration is a based off of what the online tile source has specified or two weeks. The `cache` also has limits as far as how much storage space it is allowed to consume on user's devices. Both of these settings can be altered and overridden via OpenStreetMapTileProvideConstants for version < 5.6. 5.6 and newer uses the Configuration.getInstance() structure.
 - `Tile Archives` are persistent, never expiring tile stores. This is great for offline users however care must be taken to understand what the online tile source's usage rights are on tiles. Many online sources explicitly ban you from creating a permanent tile store. Read the usage rights carefully.

The cache manager can perform a number of tasks, including
 - Downloading all tiles in a given bounding box and zoom levels
 - Download all tiles that intersect a given set of points (like a route from point A to B) and zoom levels
 - Clear all tiles for a given bounds, zoom level and tile source
 - Clear all tiles for a given set of points (again, point A to B)

The cache manager needs two things to operate. An online tile source and a tile writer. For tile writers, the default for API 8 to 9 is the file system based cache (writes to the zip archive style format using the file system). API10 and newer will default to SqlTileWriter, which uses a sqlite database to store the tiles (which is faster and avoids a number of issues with the maximum number of tiles per folder).

### Cache Manager examples

[Sample application sources](https://github.com/osmdroid/osmdroid/tree/master/OpenStreetMapViewer/src/main/java/org/osmdroid/samplefragments/cache)
 - [Import from file system cache into the new sqlite based cache](https://github.com/osmdroid/osmdroid/blob/master/OpenStreetMapViewer/src/main/java/org/osmdroid/samplefragments/cache/CacheImport.java)
 - [Purge the entire cache](https://github.com/osmdroid/osmdroid/blob/master/OpenStreetMapViewer/src/main/java/org/osmdroid/samplefragments/cache/CachePurge.java)
 - [Purge a specific tile source](https://github.com/osmdroid/osmdroid/blob/master/OpenStreetMapViewer/src/main/java/org/osmdroid/debug/CacheAnalyzerActivity.java#L110)
 - [Make a tile archive](https://github.com/osmdroid/osmdroid/blob/master/OpenStreetMapViewer/src/main/java/org/osmdroid/samplefragments/cache/SampleCacheDownloaderArchive.java)
 - [Cache tiles for a given bounds](https://github.com/osmdroid/osmdroid/blob/master/OpenStreetMapViewer/src/main/java/org/osmdroid/samplefragments/cache/SampleCacheDownloader.java)

### Important note on tile source names.

When downloading map sources using MOBAC and using one of the following formats
 * osmdroid SQLite
 * osmdroid ZIP

In both of these cases the name of the map source becomes encoded in the database or zip file format and is used to preserve uniqueness (more than one tile source per archive). Therefore you MUST tell osmdroid exactly the name of the map source when requesting offline tiles.


````
@Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    map = (MapView) findViewById(R.id.map);
    map.setTileSource(new XYTileSource("YOUR MAP SOURCE", 0, 18, 256, ".jpg", new String[] {}));
    //....
    map.setUseDataConnection(false); //optional, but a good way to prevent loading from the network and test your zip loading. 
    IMapController mapController = map.getController();
    mapController.setZoom(_A ZOOM LEVEL YOU HAVE IN YOUR ZIP_);
    GeoPoint startPoint = new GeoPoint(_POSITION SOMEWHERE INSIDE YOUR MAP_);
    mapController.setCenter(startPoint);
}
````

### Gotchas

MOBAC imposes a limit on the number of tiles to download in one shot. This can be overcome by altering the source code. 

MOBAC, especially with huge tile sets, can be a memory hog. It's algorithms from at least 1.8 to at least the 1.9.x releases caches as much as possible in memory until it hits the JVM ceiling, then does batch inserts into sqlite. While this is the way to make SQLite inserts as fast as possible, it can lead to stability issues with MOBAC.

## OSMBonusPack

[OSMBonusPack](https://github.com/MKergall/osmbonuspack) is an addon Android library for osmdroid that has the ability to download and cache map tiles on device. See the [CacheManager](https://github.com/MKergall/osmbonuspack/blob/master/OSMBonusPack/src/main/java/org/osmdroid/bonuspack/cachemanager/CacheManager.java)

# What legal things do I need to know?

Many map sources have disclaimers and legal statements that specifically state to NOT cache and rehost their map imagery. Some may have wording to disallows you to use their map imagery offline or to download large portions of the world.  Make sure you read the fine print! osmdroid takes no responsibility for map imagery misuse. That's between you and the imagery owner.

# Can you support tile archive format `x`?

We'll happily take pull requests. That said, it's fairly simple to make a new tile source for osmdroid. Here are the basic steps:
 1. Create a class that implements IArchiveProvider. At the most basic level, the IArchiveProvider needs to turn an OSM tile coordinates (Zoom, X and Y coordinates) into an InputStream representing the tile if it's available.
 2. Register the IArchiveProvider with `ArchiveFileFactory`
 3. Build, deploy and start up osmdroid


# Can I change the location osmdroid looks for map tiles archives?

Yes! However the answer depends on what version of osmdroid you're using.

Versions older than 5.6: https://github.com/osmdroid/osmdroid/blob/master/osmdroid-android/src/main/java/org/osmdroid/tileprovider/constants/OpenStreetMapTileProviderConstants.java

See `setCachePath` and `TILE_PATH_BASE`. Even though the class is called `Constants` many of the fields are writable. You can use this to tweak a number of settings for your needs.

Versions 5.6 and newer: https://github.com/osmdroid/osmdroid/blob/master/osmdroid-android/src/main/java/org/osmdroid/config/Configuration.java

`Configuration.getInstance().set...`

# What other mechanisms exists for loading tiles?

Out of the box, we have the following (and they are checked in this order)

 - Assets, you can place exploded zip archives in the assets folder of your app
 - Local file system (archives like zip, sqlite, etc)
 - Network sources cache - by default /sdcard/osmdroid/tiles/cache.db
 - Network sources - download what you need, when you need it

# Using offline tile archives

## Storage location

This part is fairly simple. Copy your tiles into the osmdroid base path (which is by default /sdcard/osmdroid). This location can be changed programmatically (note this behavior will change with v5.6). You should have the following director structure

````
/sdcard/osmdroid/
                 myZippedArchive.zip
                 myOsmdroidDatabase.sqlite
                 myMBTiles.mbtile
                 myGEMF.gemf
/sdcard/osmdroid/tiles/
                       cache.db (this is used for downloaded and cached tiles)
````

So now that your archives are there, there's a few options a few mechanisms that can be used to help you on your way towards disconnected bliss. The next step is to tell osmdroid's `map tile provider` about the map tiles.

## Map Tile Provider options

The default Map Tile Provider for osmdroid will automatically scan the osmdroid base path (again /sdcard/osmdroid/) for tile archives that it knows about, as well as search in your APK's Assets folder (exploded tiles only) then finally online sources.

If this doesn't scratch your itch, there's also the `OfflineOnlyTileProvider`. See Javadoc for more info.

### But I have my own tile archive format that I was to use!

Great, implement the IArchiveFile interface, then register your implementation with the `ArchiveFileFactory`

## Set the tile source

You now need to tell osmdroid about what tile source name to use. This is needed because file archives can have more than one tile source and tile sources can be spanned across multiple archives. If you know the source name ahead of time, you can use the following.
````
mapView.setTileSource(new XYTileSource(
        "MAP TILE SOURCE HERE",
        MINZOOM,
        MAXZOOM,
        TILESIZE (256 is the normal one),
        ".jpg",
        new String[]{}
));
````

If you don't know the tile source name ahead or plan on changing it frequently, you can discover the available tile source names at runtime. The IArchiveFile interface has a method that can be used to query an archive for the "tile source name". See `getTileSources`. See the `SampleOfflineOnly` example for usage.

## Caveats and exemptions

MBTiles does not store a tile source name in the database. Calling IArchiveFile.getTileSources will return an empty set in this case. On the flip side, MBTiles files will display to on the map regardless of what tile source the mapView is set to.

It is possible to remove all checks for tile source name comparisons for offline tiles, which would in effect, create a composite tile source. This could work out great if your archives don't overlap. To do this, extend the existing IArchiveFile providers to remove the checks, then register them as the with the `ArchiveFileFactory`